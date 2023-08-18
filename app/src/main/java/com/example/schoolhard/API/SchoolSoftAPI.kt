package com.example.schoolhard.API

import android.util.Log
import com.example.schoolhard.utils.MillisInMin
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val BASE_URL = "https://sms.schoolsoft.se"
const val app_version = "2.3.2"
const val app_os = "android"
const val device_id = ""
class SuccessfulTokenResponse(
    val token: String,
    val expiry: Long,
): APIResponse(APIResponseType.Success, null, null)

class SchoolSoftAPI:API() {
    private var appKey: String? = null
    private var token: String? = null
    private var tokenExpiry: Long? = null
    private var orgId = 1 // TODO: extract org_id from api
    private val client = OkHttpClient()
    private var schoolUrl: String? = null

    private fun execute(
            request: Request,
            failureCallback: (FailedAPIResponse) -> Unit,
            successCallback: (SuccessfulAPIResponse) -> Unit
    ){
        val call = client.newCall(request)
        call.enqueue(object: Callback{

            override fun onFailure(call: Call, e: IOException) {
                // request failed
                Log.e("SchoolSoftAPI - Error", call.request().url.toString(), e)
                failureCallback(
                    FailedAPIResponse(
                        APIResponseFailureReason.ConnectionFailure,
                        "There was a problem trying to connect to schoolsoft",
                        null,
                        null
                    )
                )
                return
            }

            override fun onResponse(call: Call, response: Response) {
                // Request was completed successfully
                // this doesn't mean the response is valid

                val apiResponse = processResponse(response)

                if (apiResponse.type == APIResponseType.Failed) {
                    // Invalid response
                    failureCallback(apiResponse as FailedAPIResponse)
                    return
                }

                successCallback(apiResponse as SuccessfulAPIResponse)
            }
        })
    }

    private fun processResponse(response: Response): APIResponse{
        val body = response.body?.string()?.let { JSONObject(it) }

        if (response.code == 401) {
            Log.w("SchoolsoftAPI", "Access denied")
            return FailedAPIResponse(APIResponseFailureReason.InvalidAuth, "Incorrect login information", response, body)
        }
        if (response.code == 500) {
            Log.w("SchoolsoftAPI", "Internal server error")
            return FailedAPIResponse(APIResponseFailureReason.InternalServerError, "Unexpected error occurred", response, body)
        }
        if (body == null){
            Log.w("SchoolsoftAPI", "Response content was null")
            return FailedAPIResponse(APIResponseFailureReason.NullError, "Null Error", response, null)
        }

        return SuccessfulAPIResponse(response, body)
    }

    private fun getExpiryFromString(expiry: String): Long{
        val format = SimpleDateFormat("yyyy-mm-dd hh:MM:ss", Locale.ENGLISH)
        val date = format.parse(expiry)
        return date.time
    }

    private fun buildRequest(url: String, token: String): Request {
        Log.v("SchoolSoftAPI", "building request")
        return Request.Builder()
            .url(url)
            .addHeader("appversion", app_version)
            .addHeader("appos", app_os)
            .addHeader("token", token)
            .addHeader("deviceid", device_id)
            .build()
    }

    override fun login(
        user: User,
        failureCallback: (FailedAPIResponse) -> Unit,
        successCallback: (SuccessfulAPIResponse) -> Unit
    ) {
        schoolUrl = "$BASE_URL/${user.school}"

        val body = FormBody.Builder()
            .add("identification", user.username)
            .add("verification", user.password)
            .add("logintype", "4")
            .add("usertype", (user.userType.ordinal + 1).toString())
            .build()

        val request = Request.Builder()
            .url("$schoolUrl/rest/app/login")
            .post(body)
            .build()

        execute(request,
            failureCallback) {

            // get appKey
            appKey = it.body.getString("appKey")
            Log.v("SchoolSoftAPI - Login", "AppKey: $appKey")

            // run callback
            status.connected = true
            status.loggedin = true
            successCallback(it)
        }
    }

    fun getToken(
        failureCallback: (FailedAPIResponse) -> Unit,
        successCallback: (SuccessfulTokenResponse) -> Unit
    ) {
        // If the token is null the user is not logged in at that must happen first
        if (!status.loggedin) {
            failureCallback(
                FailedAPIResponse(
                    APIResponseFailureReason.NotLoggedIn,
                    "User not logged in",
                    null,
                    null)
            )

            return
        }

        // get token request
        val request = Request.Builder()
            .url("$schoolUrl/rest/app/token")
            .addHeader("appversion", "2.3.2")
            .addHeader("appos", "android")
            .addHeader("appkey", appKey!!)
            .addHeader("deviceid", "")
            .build()

        // execute request
        execute(
            request,
            failureCallback,
        ){

            // get token
            token = it.body.getString("token")
            Log.v("SchoolSoftAPI - Token", "Token: $token")

            // get expiration date
            val expiry = it.body.getString("expiryDate")
            tokenExpiry = getExpiryFromString(expiry)
            Log.v("SchoolSoftAPI - Token", "expiry: $tokenExpiry")

            successCallback(
                SuccessfulTokenResponse(
                    token!!,
                    tokenExpiry!!,
                )
            )
        }
    }

    fun smartToken(
        failureCallback: (FailedAPIResponse) -> Unit,
        successCallback: (SuccessfulTokenResponse) -> Unit
    ){
        if (tokenExpiry == null) {
            Log.w("SchoolSoftAPI - SmartToken", "No token, generating")
            getToken(failureCallback, successCallback)
            return
        }

        val now = Date()
        if (tokenExpiry!! < now.time + MillisInMin*10) {
            Log.w("SchoolSoftAPI - SmartToken", "Token to old, generating new")
            getToken(failureCallback, successCallback)
            return
        }

        // if token has at least 10 minutes of lifetime left
        // do nothing and return already stored token
        successCallback(
            SuccessfulTokenResponse(
                token!!,
                tokenExpiry!!
            )
        )
    }

    override fun logout(
        failureCallback: (FailedAPIResponse) -> Unit,
        successCallback: (SuccessfulAPIResponse) -> Unit
    ) {
        appKey = null
        token = null
        tokenExpiry = null
        status.loggedin = false
    }

}