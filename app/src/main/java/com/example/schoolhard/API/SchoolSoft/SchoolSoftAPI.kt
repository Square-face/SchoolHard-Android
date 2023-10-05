package com.example.schoolhard.API.SchoolSoft

import android.util.Log
import com.example.schoolhard.API.API
import com.example.schoolhard.API.APIResponse
import com.example.schoolhard.API.APIResponseFailureReason
import com.example.schoolhard.API.APIResponseType
import com.example.schoolhard.API.APIStatus
import com.example.schoolhard.API.FailedAPIResponse
import com.example.schoolhard.API.Lesson
import com.example.schoolhard.API.Organization
import com.example.schoolhard.API.School
import com.example.schoolhard.API.User
import com.example.schoolhard.API.UserType
import com.example.schoolhard.data.Logins
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime

const val BASE_URL = "https://sms.schoolsoft.se"


/**
 * Response from a token request
 *
 * @property token Token response.
 * @property expiry Expiration date.
 *
 * @author Linus Michelsson
 * */
data class TokenResponse(
    val token: String,
    val expiry: LocalDateTime,
): APIResponse(APIResponseType.Success)



/**
 * Schoolsoft api wrapper
 *
 * Implements api routes matching the schema defined in [API]
 *
 * @property status Current api status
 *
 * @author Linus Michelsson
 * */
class SchoolSoftAPI: API {
    override val status = APIStatus()

    private var appKey: String? = null
    private var token: String? = null

    private var tokenExpiry: LocalDateTime? = null

    private var school: School? = null
    private var user: User? = null

    private var utils = Utils()



    override fun login(
        username: String,
        password: String,
        school: School,
        type: UserType,
        failureCallback: (FailedAPIResponse) -> Unit,
        successCallback: (User) -> Unit
    ) {

        val body = FormBody.Builder()
            .add("identification", username)
            .add("verification", password)
            .add("logintype", "4")
            .add("usertype", type.ordinal.toString())
            .build()

        val request = Request.Builder()
            .url("${school.url}/rest/app/login")
            .post(body)
            .build()

        utils.execute(request, failureCallback) {

            Log.d("SchoolSoftAPI - Login", it.stringBody)
            val responseBody = JSONObject(it.stringBody)

            // get appKey
            appKey = responseBody.getString("appKey")
            Log.v("SchoolSoftAPI - Login", "AppKey: $appKey")

            // update state
            status.connected = true
            status.loggedin = true

            val orgs = utils.parseOrganizations(
                responseBody.getJSONArray("orgs"),
                school
            )

            this.school = school
            user = User(
                responseBody.getInt("userId"),
                responseBody.getString("name"),
                school,
                orgs.first(),
            )

            successCallback(user!!)
        }
    }





    override fun logout(failureCallback: (FailedAPIResponse) -> Unit, successCallback: () -> Unit) {
        appKey = null
        token = null
        tokenExpiry = null
        status.loggedin = false

        Thread(successCallback)
    }



    override fun loadLogin(
        logins: Logins,
        failureCallback: (FailedAPIResponse) -> Unit,
        successCallback: () -> Unit
    ) {

        if (logins.login == null) {
            failureCallback(
                FailedAPIResponse(
                reason = APIResponseFailureReason.LoginNotSaved,
                message = "No previous login saved"
            )
            )
            return
        }

        // TODO: save and load school and user
        appKey = logins.login!!.appKey
        school = School(0, "Unknown", logins.login!!.url)
        user = User(
            0,
            "Unknown",
            school!!,
            Organization(
                0,
                1,
                school!!,
                "Unknown"
            )
        )
        status.loggedin = true

        successCallback()
    }


    override fun saveLogin(
        logins: Logins,
        failureCallback: (FailedAPIResponse) -> Unit,
        successCallback: () -> Unit
    ) {
        if (!status.loggedin) {
            failureCallback(FailedAPIResponse(
                reason = APIResponseFailureReason.NotLoggedIn,
                message = "User is not logged in"
            ))
        }

        logins.saveLogin(school!!.url, user!!, appKey!!, setActive = true)

        successCallback()
    }





    override fun lessons(
        failureCallback: (FailedAPIResponse) -> Unit,
        successCallback: (List<Lesson>) -> Unit
    ) {
        smartToken(failureCallback){ token ->

            val url = "${user!!.school.url}api/lessons/student/${user!!.organization.orgId}"

            val request = utils.buildRequest(url, token.token)

            utils.execute(request, failureCallback){
                utils.parseLessons(it.stringBody, successCallback)
            }
        }
    }

    override fun schools(
        failureCallback: (FailedAPIResponse) -> Unit,
        successCallback: (List<School>) -> Unit
    ) {

        val url = "$BASE_URL/rest/app/schoollist/prod"
        val request = Request.Builder().url(url).build()

        utils.execute(request, failureCallback) {
            val body = JSONArray(it.stringBody)
            val schools = mutableListOf<School>()

            for (i in 0 until body.length()) {
                val school = body[i] as JSONObject

                schools.add(utils.parseSchool(school))
            }

            Log.v("SchoolSoftAPI - Schools", "returned with ${schools.size} schools")

            successCallback(schools)
        }
    }


    private fun getToken(
        failureCallback: (FailedAPIResponse) -> Unit,
        successCallback: (TokenResponse) -> Unit
    ) {
        if (!status.loggedin) {

            return failureCallback(FailedAPIResponse(
                APIResponseFailureReason.NotLoggedIn,
                "User not logged in"
            ))
        }

        // get token request
        val request = Request.Builder()
            .url("${school!!.url}rest/app/token")
            .addHeader("appversion", "2.3.2")
            .addHeader("appos", "android")
            .addHeader("appkey", appKey!!)
            .addHeader("deviceid", "")
            .build()

        // execute request
        utils.execute(
            request,
            failureCallback,
        ){
            Log.d("SchoolSoftAPI - Token", it.stringBody)
            val body = JSONObject(it.stringBody)

            // get token
            token = body.getString("token")
            Log.v("SchoolSoftAPI - Token", "Token: $token")

            // get expiration date
            val expiry = body.getString("expiryDate")
            tokenExpiry = utils.getExpiryFromString(expiry)
            Log.v("SchoolSoftAPI - Token", "expiry: $tokenExpiry")

            successCallback(
                TokenResponse(
                    token!!,
                    tokenExpiry!!,
                )
            )
        }
    }

    private fun smartToken(
        failureCallback: (FailedAPIResponse) -> Unit,
        successCallback: (TokenResponse) -> Unit
    ){
        if (tokenExpiry == null) {
            Log.w("SchoolSoftAPI - SmartToken", "No token")
            getToken(failureCallback, successCallback)
            return
        }

        val now = LocalDateTime.now()
        if (tokenExpiry!!.isAfter( now.minusHours( 1 ) )) {
            Log.w("SchoolSoftAPI - SmartToken", "Token to old")
            getToken(failureCallback, successCallback)
            return
        }

        // if token has at least 10 minutes of lifetime left
        // do nothing and return already stored token
        Log.v("SchoolSoftAPI - SmartToken", "Saved token is still valid")
        successCallback(
            TokenResponse(
                token!!,
                tokenExpiry!!
            )
        )
    }
}