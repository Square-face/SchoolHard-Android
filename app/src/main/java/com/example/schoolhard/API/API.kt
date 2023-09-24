package com.example.schoolhard.API

/**
 * Define api routes and their return values
 *
 * This file is simply a schema definition and provides no functionality. Any API implementation should inherit the main API class to allow interop.
 *
 *
 * @author Linus Michelsson
 * */

import android.util.Log
import com.example.schoolhard.data.Login
import okhttp3.Response
import java.lang.Exception
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime



/**
 * API SCHEMA
 *
 * @property status Current api status
 *
 * @author Linus Michelsson
 * */
open class API {
    val status = APIStatus()

    /**
     * Attempt login using username, password, school and usertype. If the login is successful the
     * api can be used further
     *
     * @param username Username
     * @param password Password
     * @param school School
     * @param type Usertype
     * @param failureCallback Lambda function to run on separate thread if the request fails in any way.
     * When logging in this is usually due to invalid auth but could also be a result of a
     * connection error
     * @param successCallback Lambda function to run on a separate thread if the request succeeds.
     * */
    open fun login(
        username: String,
        password: String,
        school: School,
        type: UserType,
        failureCallback: (FailedAPIResponse)->(Unit) = { response -> Log.e("API", response.message) },
        successCallback: (User)->(Unit) = {},
    ){}



    /**
     * Logout a currently logged in user.
     *
     * @param failureCallback Lambda function to run on separate thread if the request fails in any way.
     * @param successCallback Lambda function to run on a separate thread if the request succeeds.
     * */
    open fun logout(
        failureCallback: (FailedAPIResponse)->(Unit) = { response -> Log.e("API", response.message) },
        successCallback: ()->(Unit) = {},
    ){}



    /**
     * Get the full list of lessons
     *
     * @param failureCallback Lambda function to run on separate thread if the request fails in any way.
     * @param successCallback Lambda function to run on a separate thread if the request succeeds.
     * */
    open fun lessons(
        failureCallback: (FailedAPIResponse)->(Unit) = { response -> Log.e("API", response.message) },
        successCallback: (List<Lesson>)->(Unit) = {},
    ){}



    /**
     * Get the currently logged in user as a object
     *
     * @param failureCallback Lambda function to run on separate thread if the request fails in any way.
     * @param successCallback Lambda function to run on a separate thread if the request succeeds.
     * */
    open fun userInfo(
        failureCallback: (FailedAPIResponse)->(Unit) = { response -> Log.e("API", response.message) },
        successCallback: (User)->(Unit) = {},
    ){}



    /**
     * Get list of available schools
     *
     * @param failureCallback Lambda function to run on separate thread if the request fails in any way.
     * @param successCallback Lambda function to run on a separate thread if the request succeeds.
     * */
    open fun schools(
        failureCallback: (FailedAPIResponse)->(Unit) = { response -> Log.e("API", response.message) },
        successCallback: (List<School>)->(Unit) = {},
    ){}



    /**
     * Save the currently active login information to disk to allow it to be used later
     *
     * @param failureCallback Lambda function to run on separate thread if the request fails in any way.
     * @param successCallback Lambda function to run on a separate thread if the request succeeds.
     * */
    open fun saveLogin(
        failureCallback: (FailedAPIResponse)->(Unit) = { response -> Log.e("API", response.message) },
        successCallback: ( )->(Unit) = {},
    ) {}



    /**
     * Load login configuration from disk
     *
     * @param failureCallback Lambda function to run on separate thread if the request fails in any way.
     * @param successCallback Lambda function to run on a separate thread if the request succeeds.
     * */
    open fun loadLogin(
        failureCallback: (FailedAPIResponse)->(Unit) = { response -> Log.e("API", response.message) },
        successCallback: ()->(Unit) = {},
    ) {}
}




/**
 * Current api connection status
 *
 * @property connected If the latest request was successful in getting a response of any kind
 * @property loggedin If there is currently a valid login configuration active
 *
 * @author Linus Michelsson
 * */
class APIStatus{
    var connected = false
    var loggedin = false
}







/* === Response Definitions === */

/**
 * Generic api response
 *
 * @property type What type of api response this is
 *
 * @author Linus Michelsson
 * */
open class APIResponse(
    val type: APIResponseType,
)



/**
 * Generic response failure
 *
 * @property reason Why this response is a failure
 * @property message Message explaining the failure
 *
 * @author Linus Michelsson
 * */
class FailedAPIResponse(
    val reason: APIResponseFailureReason,
    val message: String,
): APIResponse(APIResponseType.Failed)



/**
 * Response modes
 *
 * @author Linus Michelsson
 * */
enum class APIResponseType{
    Failed, Success
}



/**
 * Possible reasons for an api failure
 *
 * @author Linus Michelsson
 * */
enum class APIResponseFailureReason{
    /**
     * Authentication failed
     * */
    InvalidAuth,

    /**
     * User is not currently logged in
     * */
    NotLoggedIn,

    /**
     * Remote error
     * */
    InternalServerError,

    /**
     * Failed to connect to service
     * */
    ConnectionFailure,

    /**
     * Null response body
     * */
    NullError,
}