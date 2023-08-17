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
    var appKey: String? = null
    var token: String? = null
    var tokenExpiry: Long? = null
    var org_id = 1 // TODO: extract org_id from api
    val client = OkHttpClient()
    var SCHOOL_URL: String? = null

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

    private fun get_expiry_from_string(expiry: String): Long{
        val format = SimpleDateFormat("yyyy-mm-dd hh:MM:ss", Locale.ENGLISH)
        val date = format.parse(expiry)
        return date.time
    }

    private fun build_request(url: String, token: String): Request {
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
        SCHOOL_URL = "$BASE_URL/${user.school}"

        val body = FormBody.Builder()
            .add("identification", user.username)
            .add("verification", user.password)
            .add("logintype", "4")
            .add("usertype", (user.userType.ordinal + 1).toString())
            .build()

        val request = Request.Builder()
            .url("$SCHOOL_URL/rest/app/login")
            .post(body)
            .build()

        execute(request,
            failureCallback) {
            it.body !!

            // get appKey
            appKey = it.body.getString("appKey")
            Log.v("SchoolSoftAPI - Login", "AppKey: $appKey")

            // run callback
            successCallback(it)
        }
    }

    override fun logout(
        failureCallback: (FailedAPIResponse) -> Unit,
        successCallback: (SuccessfulAPIResponse) -> Unit
    ) {
        appKey = null
        token = null
        tokenExpiry = null
    }

}