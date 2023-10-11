package com.example.schoolhard.data

import android.content.SharedPreferences
import android.util.Log
import com.example.schoolhard.API.User
import com.example.schoolhard.API.UserType
import org.json.JSONObject
import java.lang.Exception

/**
 * Helper class for managing login information
 *
 * @param uuid user id
 * @param username Users human readable username
 * @param appKey Key for logging into schoolsoft (Not password)
 * @param userType The type of user
 * */
class Login(val uuid: String, private val username: String, val appKey: String, private val userType: UserType, val url: String) {

    fun toJson(): String {
        return JSONObject()
            .put("uuid", uuid)
            .put("username", username)
            .put("appkey", appKey)
            .put("usertype", userType.ordinal)
            .put("url", url)
            .toString()
    }

    companion object {
        fun fromJSON(loginString: String): Login {
            val login = JSONObject(loginString)

            return Login(
                login.getString("uuid"),
                login.getString("username"),
                login.getString("appkey"),
                UserType.from(login.getInt("usertype")),
                login.getString("url"),
            )
        }

        fun fromUser(user: User, url: String, key: String): Login {
            return Login(
                user.id.toString(),
                user.username,
                key,
                user.userType,
                url,
            )
        }
    }
}




/**
 * A helper class for managing persistent storage on the device
 *
 * @param store SharedPreference store to use when saving login info
 * @property login The currently active login
 * */
class Logins(private val store: SharedPreferences) {
    val login get() = getActiveLogin()





    /**
     * Get all currently saved logins
     *
     * @return set of all logins
     * */
    private fun getLogins(): Set<Login> {
        val uuids = getUUIDs()

        val logins = uuids.map { getLogin(it) }

        return logins.toSet()
    }





    /**
     * Get the currently active login
     *
     * @returns active login or null of no login is selected
     * */
    private fun getActiveLogin(): Login? {

        val uuid = getActiveUUID() ?: return null

        if (!store.contains(uuid)){ return null }

        return getLogin(uuid)
    }





    /**
     * Set the currently active login
     *
     * @param uuid The uuid of the login that will be considered active
     * */
    fun setActiveLogin(uuid: String) {
        setActiveUUID(uuid)
    }

    /**
     * Set the currently active login
     *
     * @param login Login object representing the to be active login
     * */
    fun setActiveLogin(login: Login) {
        setActiveLogin(login.uuid)
    }



    /**
     * Save a new set of login information
     *
     * @param url The url to login to
     * @param user User API object containing necessary user information
     * @param setActive If the new login should be set as the active login
     * @throws LoginExceptions.LoginAlreadyExists If there already exists a login with the same uuid
     * @return The UUID for this login
     * */
    fun saveLogin(url: String, user: User, key: String, setActive: Boolean = false): Login{
        val login = Login.fromUser(user, url, key)
        storeLoginString(login.toJson(), login.uuid)

        if (setActive) { setActiveLogin(login) }

        return login
    }



    /**
     * get a stored login from its uuid
     *
     * @param uuid UUID for the requested login
     * @throws LoginExceptions.LoginNotFound If the login doesn't exist
     * */
    fun getLogin(uuid: String): Login{

        // if null throw LoginNotFound
        val loginString = getString(uuid) ?: throw LoginExceptions.LoginNotFound(uuid)

        return Login.fromJSON(loginString)
    }





    /**
     * Store login in persistent storage
     *
     * @param login String representation of a login object
     * @param uuid The uuid to address the login as
     * @throws LoginExceptions.LoginAlreadyExists If there already exists a login with this uuid
     * */
    private fun storeLoginString(login: String, uuid: String) {

        if (getString(uuid) != null) { throw LoginExceptions.LoginAlreadyExists(uuid) }

        storeString(uuid, login)
        saveUUID(uuid)
    }





    /**
     * Get the uuid of the currently active login
     *
     * @returns the uuid
     * */
    private fun getActiveUUID(): String?{
        return getString("index")
    }





    /**
     * Set the uuid of the currently active login
     *
     * @param uuid the uuid to be considered active
     * */
    private fun setActiveUUID(uuid: String){
        storeString("index", uuid)
    }





    /**
     * Get a list of all the uuids in persistent storage
     *
     * @returns set of strings with all the saved uuids
     * */
    private fun getUUIDs(): Set<String> {
        return getSet("uuids") ?: emptySet()
    }





    /**
     * Save a new uuid to persistent storage
     *
     * @param uuid The uuid to save to persistent storage
     * */
    private fun saveUUID(uuid: String) {
        Log.v("Logins - saveUUID", "Saving new uuid ($uuid)")
        addStringToSet("uuids", uuid)
    }





    /**
     * Add a string to a set in persistent storage
     *
     * @param address The address where the set to modify is at
     * @param value The string to be added to the set
     * */
    private fun addStringToSet(address: String, value: String) {
        Log.d("Logins - addStringToSet", "Adding $value to $address")

        val set = getSet(address) ?: emptySet()

        storeSet(address, set.plus(value))
    }





    /**
     * Get a string stored in persistent storage
     *
     * @param address The address for where to get the string from
     * @returns The stored string ot null if there is no string at the address
     * */
    private fun getString(address: String): String? {

        val result = store.getString(address, null)
        Log.d("Logins - readString", "Reading $address data: $result")

        return result
    }





    /**
     * Get a integer stored in persistent storage
     *
     * @param address The address for where to get the integer from
     * @returns The stored integer ot null if there is no integer at the address
     * */
    private fun getInt(address: String): Int? {

        // manual check and null return bcs SharedPreferences.getInt can't return null as default
        if (!store.contains(address)) {
            Log.d("Logins - readInt", "Reading $address data: no data")
            return null
        }

        val result = store.getInt(address, 0)
        Log.d("Logins - readInt", "Reading $address data: $result")

        return result
    }





    /**
     * Get a set of strings from persistent storage
     *
     * @param address The address for where to get the set from
     * @returns The stored set or null if there is no set stored at the address
     * */
    private fun getSet(address: String): Set<String>?{

        val result = store.getStringSet(address, null)
        Log.d("Logins - readSet", "Reading $address data: $result")

        return result
    }





    /**
     * Save string to persistent storage
     * IMPORTANT: This function does not take into account if there already exists data.
     * Any data stored on the same address will be overwritten
     *
     * @param address The address to store the string at
     * @param value The to be stored string
     * */
    private fun storeString(address: String, value: String) {
        Log.d("Logins - writeString", "Writing to $address data: $value")
        val editor = store.edit()
        editor.putString(address, value)
        editor.commit()
    }





    /**
     * Save integer to persistent storage
     * IMPORTANT: This function does not take into account if there already exists data.
     * Any data stored on the same address will be overwritten
     *
     * @param address The address to store the integer at
     * @param value The to be stored integer
     * */
    private fun storeInt(address: String, value: Int) {
        Log.d("Logins - writeInt", "Writing to $address data: $value")
        val editor = store.edit()
        editor.putInt(address, value)
        editor.commit()
    }





    /**
     * Save set to persistent storage
     * IMPORTANT: This function does not take into account if there already exists data.
     * Any data stored on the same address will be overwritten
     *
     * @param address The address to store the set at
     * @param value The to be stored set
     * */
    private fun storeSet(address: String, value: Set<String>) {
        Log.d("Logins - writeSet", "Writing to $address data: $value")
        val editor = store.edit()
        editor.putStringSet(address, value)
        editor.commit()
    }
}

class LoginExceptions {
    class LoginNotFound(uuid: String): Exception("Login with uuid: $uuid does not exist")
    class LoginAlreadyExists(uuid: String): Exception("Login with uuid: $uuid already exists")
}
