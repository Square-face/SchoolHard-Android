package com.example.schoolhard.API

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

const val BASE_URL = "https://sms.schoolsoft.se"

class SchoolSoftAPI():API() {
    var appKey: String? = null
    var token: String? = null
    var tokenExpiry: String? = null
    val client = OkHttpClient()
    var SCHOOL_URL: String? = null

    private fun processResponse(response: Response): APIResponse{
        val body = response.body?.string()?.let { JSONObject(it) }

        if (response.code == 401) {
            return FailedAPIResponse(APIResponseFailureReason.InvalidAuth, "Incorrect login information", response, body)
        }
        if (response.code == 500) {
            return FailedAPIResponse(APIResponseFailureReason.InternalServerError, "Unexpected error occurred", response, body)
        }

        return SuccessfulAPIResponse(response, body)
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
            .add("usertype", user.userType.ordinal.toString())
            .build()

        val request = Request.Builder()
            .url("$SCHOOL_URL/rest/app/login")
            .post(body)
            .build()

        val call = client.newCall(request)
        call.enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SchoolSoftAPI - login", call.request().url.toString(), e)
                return
            }

            override fun onResponse(call: Call, response: Response) {
                val apiResponse = processResponse(response)

                if (apiResponse.type == APIResponseType.Failed) {
                    // response had an error
                    failureCallback(apiResponse as FailedAPIResponse)
                    return

                }
                if (apiResponse.body == null){
                    // null body
                    failureCallback(FailedAPIResponse(APIResponseFailureReason.InternalServerError, "Null Error", response, null))
                    return
                }

                // get appKey
                appKey = apiResponse.body.getString("appKey")
                Log.v("SchoolSoftAPI - Login", "AppKey: $appKey")

                // run callback
                successCallback(apiResponse as SuccessfulAPIResponse)

                return
            }
        })
    }
}