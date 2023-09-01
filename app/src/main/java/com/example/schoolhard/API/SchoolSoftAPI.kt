package com.example.schoolhard.API

import android.nfc.FormatException
import android.util.Log
import com.example.schoolhard.data.Login
import com.example.schoolhard.utils.MillisInMin
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.IsoFields
import java.util.Date
import java.util.Locale

const val BASE_URL = "https://sms.schoolsoft.se"
const val app_version = "2.3.2"
const val app_os = "android"
const val device_id = ""
val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.S")

class SchoolSoftSchool(name: String, url: String, loginMethods: LoginMethods): School(name, url, loginMethods){
    companion object {
        private fun parseLoginMethod(loginMethod: String): List<Int>{
            if (!loginMethod.contains(",")){return listOf()}
            return loginMethod.split(",").map { it.toInt() }
        }
        fun parse(school: JSONObject): School {
            Log.d("SchoolSoftAPI - SchoolParse", "SchoolObject: $school")
            return School(
                school.getString("name"),
                school.getString("url"),
                LoginMethods(
                    parseLoginMethod(school.getString("parentLoginMethods")),
                    parseLoginMethod(school.getString("studentLoginMethods")),
                    parseLoginMethod(school.getString("teacherLoginMethods")),
                )
            )
        }
    }

}

class SuccessfulTokenResponse(
    val token: String,
    val expiry: Long,
): APIResponse(APIResponseType.Success, null, null)

class SchoolSoftAPI:API() {


    var appKey: String? = null
    private var token: String? = null
    private var tokenExpiry: Long? = null
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

                Log.v("SchoolSoftAPI", "ResponseBody: ${apiResponse.body}")

                successCallback(apiResponse as SuccessfulAPIResponse)
            }
        })
    }

    private fun processResponse(response: Response): APIResponse{
        val body = response.body

        if (response.code == 401) {
            Log.w("SchoolsoftAPI", "Access denied")
            return FailedAPIResponse(APIResponseFailureReason.InvalidAuth, "Incorrect login information", response, body?.string())
        }
        if (response.code == 500) {
            Log.w("SchoolsoftAPI", "Internal server error")
            return FailedAPIResponse(APIResponseFailureReason.InternalServerError, "Unexpected error occurred", response, body?.string())
        }
        if (body == null){
            Log.w("SchoolsoftAPI", "Response content was null")
            return FailedAPIResponse(APIResponseFailureReason.NullError, "Null Error", response, null)
        }

        return SuccessfulAPIResponse(response, body.string())
    }

    private fun getExpiryFromString(expiry: String): Long{
        val format = SimpleDateFormat("yyyy-mm-dd hh:MM:ss", Locale.ENGLISH)
        val date = format.parse(expiry)
        if (date != null) {
            return date.time
        }

        throw FormatException("Invalid expiration string formatting")
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

    private fun getOrganizations(array: JSONArray): List<Organization>{
        val organizations = mutableListOf<Organization>()

        for (i in 0 until array.length()) {
            val org = array.getJSONObject(i)
            organizations.add(Organization(
                org.getString("name"),
                org.getString("class"),
                org.getInt("orgId"),
            ))
        }

        return organizations
    }

    private fun jsonArrayToList(jsonArray: JSONArray): List<JSONObject> {
        val list = mutableListOf<JSONObject>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getJSONObject(i))
        }
        return list
    }

    private fun getTime(raw: String): LocalTime{
        // "1970-01-01 08:20:00.0" -> "08:20:00.0"
        val time = raw.split(" ").last()
        return LocalTime.parse(time, timeFormat)
    }

    private fun getDate(week: Int, dayOfWeek: DayOfWeek): LocalDate {
        var date = LocalDate.now()

        if (week < 27) {date = date.with(ChronoField.YEAR, date.year+1L)}

        date = date.with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week.toLong())
        date = date.with(ChronoField.DAY_OF_WEEK, dayOfWeek.value.toLong())

        return date
    }

    private fun getWeeks(event: JSONObject): List<Int>{

        val periods = event.getString("weeksString").split(", ")
        val results: List<Int> = periods.flatMap {period ->
            val span = period.split("-")
            if (span.size == 1) {
                listOf(period.toInt())
            } else {
                val (start, end) = span.map { it.toInt() }
                start..end
            }
        }

        return results
    }

    private fun getOccasion(event: JSONObject, week: Int): Occasion{
        val dayOfWeek = DayOfWeek.of(event.getInt("dayId")+1)
        return Occasion(
            userId,
            orgId,
            Subject(
                event.getString("subjectName"),
                event.getString("subjectName").split(" - ").subList(1, event.getString("subjectName").split(" - ").size).joinToString(" "),
                event.getInt("id"),
                event.getString("externalId")
            ),
            week,
            dayOfWeek,
            event.getString("roomName"),
            "<placeholder>",
            getDate(week, dayOfWeek),
            getTime(event.getString("startTime")),
            getTime(event.getString("endTime")),
        )
    }

    private fun filterLessons(filter: Filter, occasions: List<Occasion>): List<Occasion>{
        return occasions
            .filter { occasion -> !filter.from.isAfter(occasion.date.atTime(occasion.startTime)) }
            .filter { occasion -> !filter.to.isBefore(occasion.date.atTime(occasion.endTime)) }
            .take(filter.maxCount)
    }

    private fun parseLessons(
        response: SuccessfulAPIResponse,
        filter: Filter?,
        successCallback: (SuccessfulLessonResponse) -> Unit
    ){
        val occasions = mutableMapOf<Int, MutableList<Occasion>>()
        val body = jsonArrayToList(JSONArray(response.body))

        body.flatMap { event ->
            getWeeks(event).map { week ->
                Pair(week, getOccasion(event, week))
            }

        }.forEach { (week, occasion) ->
            occasions.getOrPut(week){ mutableListOf() }
                .add(occasion)
        }

        // flatten dictionary values after sorting based on start time
        val results = occasions.flatMap { (_, occasions) -> occasions.sortedBy { occasion -> occasion.date.atTime(occasion.startTime) } }
        val filteredResults = if (filter != null) filterLessons(filter, results) else results
        Log.v("SchoolSoftAPI - Lessons", "Filtered from ${results.size} entries to ${filteredResults.size}")

        successCallback(SuccessfulLessonResponse(filteredResults))
    }



    fun setSchoolUrl(url: String) {
        schoolUrl = url
    }

    fun loginWithAppKey(newAppKey: String) {
        Log.i("SchoolHardAPI - AppKeyLogin", "Logging in with $newAppKey")
        appKey = newAppKey
        status.loggedin = true
        status.connected = true
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
            .url("${schoolUrl}rest/app/token")
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
            Log.d("SchoolSoftAPI - Token", it.body)
            val body = JSONObject(it.body)

            // get token
            token = body.getString("token")
            Log.v("SchoolSoftAPI - Token", "Token: $token")

            // get expiration date
            val expiry = body.getString("expiryDate")
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
        Log.v("SchoolSoftAPI - SmartToken", "Saved token is still valid")
        successCallback(
            SuccessfulTokenResponse(
                token!!,
                tokenExpiry!!
            )
        )
    }

    override fun login(
        identification: String,
        password: String,
        school: School,
        type: UserType,
        failureCallback: (FailedAPIResponse) -> Unit,
        successCallback: (SuccessfulLoginResponse) -> Unit
    ) {
        schoolUrl = school.url

        val body = FormBody.Builder()
            .add("identification", identification)
            .add("verification", password)
            .add("logintype", "4")
            .add("usertype", (type.ordinal).toString())
            .build()

        val request = Request.Builder()
            .url("${schoolUrl}rest/app/login")
            .post(body)
            .build()

        execute(request,
            failureCallback) {
            Log.d("SchoolSoftAPI - Login", it.body)
            val responseBody = JSONObject(it.body)

            // get appKey
            appKey = responseBody.getString("appKey")
            Log.v("SchoolSoftAPI - Login", "AppKey: $appKey")

            // update state
            status.connected = true
            status.loggedin = true

            val user = User(
                responseBody.getString("name"),
                appKey!!,
                responseBody.getInt("userId"),
                UserType.from(responseBody.getInt("type")),
                getOrganizations(responseBody.getJSONArray("orgs"))
            )

            successCallback(
                SuccessfulLoginResponse(
                    user,
                    it.response,
                    it.body
                )
            )
        }
    }

    override fun loginWithSaved(login: Login) {
        appKey = login.appKey
        schoolUrl = login.url
        status.loggedin = true
        status.connected = true
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

    override fun lessons(
        filter: Filter?,
        failureCallback: (FailedAPIResponse) -> Unit,
        successCallback: (SuccessfulLessonResponse) -> Unit
    ) {smartToken(failureCallback){token ->
        val request = buildRequest("${schoolUrl}api/lessons/student/$orgId", token.token)
        execute(request, failureCallback){
            parseLessons(it, filter, successCallback)
        }
    }}

    override fun schools(
        failureCallback: (FailedAPIResponse) -> Unit,
        successCallback: (SuccessfulSchoolsResponse) -> Unit
    ) {
        val request = Request.Builder().url("$BASE_URL/rest/app/schoollist/prod").build()
        execute(request, failureCallback) {
            val body = JSONArray(it.body)
            val schools = mutableListOf<School>()

            for (i in 0 until body.length()) {
                val school = body[i] as JSONObject

                schools.add(SchoolSoftSchool.parse(school))
            }

            Log.v("SchoolSoftAPI - Schools", "returned with ${schools.size} schools")

            successCallback(
                SuccessfulSchoolsResponse(
                    schools
                )
            )
        }
    }
}