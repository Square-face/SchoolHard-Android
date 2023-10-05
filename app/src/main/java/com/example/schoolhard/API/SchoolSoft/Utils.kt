package com.example.schoolhard.API.SchoolSoft

/**
 * Utility functions for schoolsoft api routes.
 *
 * @author Linus Michelsson
 * */

import android.nfc.FormatException
import android.util.Log
import com.example.schoolhard.API.APIResponse
import com.example.schoolhard.API.APIResponseFailureReason
import com.example.schoolhard.API.APIResponseType
import com.example.schoolhard.API.FailedAPIResponse
import com.example.schoolhard.API.Lesson
import com.example.schoolhard.API.Location
import com.example.schoolhard.API.Occasion
import com.example.schoolhard.API.Organization
import com.example.schoolhard.API.School
import com.example.schoolhard.API.Subject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.IsoFields
import java.util.UUID


const val app_version = "2.3.2"
const val app_os = "android"
const val device_id = ""

val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.S")


/**
 * Utility class for schoolsoft API
 *
 * @author Linus Michelsson
 * */
class Utils{
    private val client = OkHttpClient()


    /**
     * Execute a http request
     *
     * @param request The request object to execute
     * @param failureCallback Lambda function to run in background if the request fails in any way.
     * @param successCallback Lambda function to run in background if the request succeeds.
     * */
    fun execute(
        request: Request,
        failureCallback: (FailedAPIResponse) -> Unit,
        successCallback: (RawAPIResponse) -> Unit
    ){
        val call = client.newCall(request)
        call.enqueue(object: Callback {

            override fun onFailure(call: Call, e: IOException) {

                Log.e("SchoolSoftAPI - Error", call.request().url.toString(), e)


                failureCallback(
                    FailedAPIResponse(
                        APIResponseFailureReason.ConnectionFailure,
                        "There was a problem trying to connect to schoolsoft",
                    )
                )
            }

            override fun onResponse(call: Call, response: Response) {
                // Request was completed successfully
                // this doesn't mean the response is valid

                val apiResponse = processResponse(response)

                if (apiResponse.type == APIResponseType.Failed) {

                    failureCallback(apiResponse as FailedAPIResponse)
                    return
                }

                apiResponse as RawAPIResponse

                Log.v("SchoolSoftAPI - Response", "ResponseBody: ${apiResponse.stringBody}")
                successCallback(apiResponse)
            }
        })
    }



    /**
     * Process a response object and turn it into a [APIResponse] object. Also check for unhappy
     * http status codes and set appropriate fail reason.
     *
     * @param response Response object to convert
     *
     * @return Either [RawAPIResponse] or [FailedAPIResponse] Depending on if the status codes are
     * ok or unhappy
     * */
    fun processResponse(
        response: Response
    ): APIResponse {
        val body = response.body

        when (response.code) {

            401 -> { /** Unauthorized */
                Log.w("SchoolSoftAPI - Request", "Unauthorized")
                return FailedAPIResponse(
                    APIResponseFailureReason.InvalidAuth,
                    "Incorrect login information",
                )
            }


            500 -> { /** Remote server error */
                Log.w("SchoolSoftAPI - Request", "Remote server error")
                return FailedAPIResponse(
                    APIResponseFailureReason.InternalServerError,
                    "Unexpected error occurred",
                )
            }
        }

        if (body == null){
            Log.w("SchoolsoftAPI", "Response content was null")
            return FailedAPIResponse(APIResponseFailureReason.NullError, "Null Error")
        }

        return RawAPIResponse(response, body.string())
    }



    /**
     * Construct a standard get request object
     *
     * @param url The request url
     * @param token Token to use for request
     *
     * @return Request object
     * */
    fun buildRequest(
        url: String,
        token: String
    ): Request {

        Log.d("SchoolSoftAPI", "Building request for url $url")
        return Request.Builder()
            .url(url)
            .addHeader("appversion", app_version)
            .addHeader("appos", app_os)
            .addHeader("token", token)
            .addHeader("deviceid", device_id)
            .build()
    }



    /**
     * Convert string to a expiration date
     *
     * @param expiry The string version of the expiration date
     *
     * @return expiration date as object
     * */
    fun getExpiryFromString(
        expiry: String
    ): LocalDateTime{

        var format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS")

        if (expiry.length == 23) {
            format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        }


        val date = LocalDateTime.from(format.parse(expiry))

        if (date != null) {
            return date
        }

        throw FormatException("Invalid expiration string formatting")
    }



    /**
     * Convert [JSONObject] representation of a school to a [School] object
     *
     * @param school Json representation
     *
     * @return School object
     * */
    fun parseSchool(
        school: JSONObject
    ): School {

        val name = school.getString("name")
        val url = school.getString("url")

        return School(name, url)
    }



    /**
     * Convert a json represented organization into a organization object
     *
     * @param organization The json object representing the organization
     * @param school Parent school
     *
     * @return Organization object
     * */
    fun parseOrganization(
        organization: JSONObject,
        school: School
    ): Organization {

        val orgId = organization.getInt("orgId")
        val name = organization.getString("name")

        return Organization(
            orgId = orgId,
            school = school,
            name = name
        )
    }



    /**
     * Convert a list of json represented organizations to a list of organization objects
     *
     * @param orgs Array of json represented organizations
     * @param school Parent school
     *
     * @return List of organization objects
     * */
    fun parseOrganizations(
        orgs: JSONArray,
        school: School
    ): List<Organization>{

        val organizations = mutableListOf<Organization>()

        for (i in 0 until orgs.length()) {

            val jsonOrganization = orgs.getJSONObject(i)
            val organization = parseOrganization(
                jsonOrganization,
                school
            )

            organizations.add(organization)
        }

        return organizations
    }



    /**
     * Parse [Lesson] objects from api body string
     *
     * @param raw Raw api body as a string
     * @param successCallback Callback function to run when parsing is complete
     * */
    fun parseLessons(
        raw: String,
        successCallback: (List<Lesson>) -> Unit
    ) {
        val rawOccasions = jsonArrayToList(JSONArray(raw))

        val subjects = mutableMapOf<Int, Subject>()
        val lessons = mutableListOf<Lesson>()

        for (rawOccasion in rawOccasions){

            val subject: Subject = getSubject(rawOccasion, subjects)
            lessons.addAll(createLessons(rawOccasion, subject))
        }

        successCallback(lessons)
    }



    /**
     * Create all the [Lesson] objects for the weeks associated with a [Occasion]
     *
     * @param rawOccasion Json formatted occasion
     * @param subject parent subject
     *
     * @return list of lessons
     * */
    private fun createLessons(
        rawOccasion: JSONObject,
        subject: Subject
    ): Collection<Lesson> {

        val occasion = createOccasion(rawOccasion, subject)
        val weeks = parseWeeks(rawOccasion.getString("weeksString"))

        return weeks.map {week -> createLesson(occasion, week)}
    }



    /**
     * Get [Subject] if exists else create a subject
     *
     * Check if the subject associated with [rawOccasion] exists in [subjects].
     * If it exists, return it.
     * If it doesn't exist, create a new subject from [rawOccasion]
     *
     * @param rawOccasion [JSONObject] representing a occasion as provided by the api
     * @param subjects A map with [Subject.id] as the key and the [Subject] as the value
     *
     * @return A subject
     * */
    private fun getSubject(
        rawOccasion: JSONObject,
        subjects: MutableMap<Int, Subject>
    ): Subject {

        val subjectId = rawOccasion.getInt("subjectId")

        return subjects.getOrPut(subjectId) { createSubject(rawOccasion) }
    }


    /**
     * Create a new [Subject] from a [JSONObject]
     *
     * @param rawOccasion The raw json object representing a occasion
     *
     * @return A new [Subject]
     * */
    private fun createSubject(
        rawOccasion: JSONObject
    ): Subject {

        val subjectId = rawOccasion.getInt("subjectId")
        val subjectName = rawOccasion.getString("subjectName")

        return Subject(
            subjectId,
            subjectName,
            UUID.randomUUID(),
        )
    }



    /**
     * Create a new [Occasion] object from [rawOccasion] with [subject] as the parent
     *
     * @param rawOccasion Json object representing the occasion
     * @param subject parent [Subject]
     *
     * @return Occasion
     * */
    private fun createOccasion(
        rawOccasion: JSONObject,
        subject: Subject
    ): Occasion {

        return Occasion(
            id = UUID.fromString(rawOccasion.getString("guid")),
            occasionId = rawOccasion.getInt("id"),
            subject = subject,
            location = Location(rawOccasion.getString("roomName")),
            startTime = getTime(rawOccasion.getString("startTime")),
            endTime = getTime(rawOccasion.getString("endTime")),
            dayOfWeek = DayOfWeek.of(rawOccasion.getInt("dayId")+1)
        )
    }



    /**
     * Create a new [Lesson] object with [occasion] as the parent and [week] as the week,
     *
     * @param occasion Parent [Occasion]
     * @param week Week number to base the lesson date on
     *
     * @return Lesson
     * */
    private fun createLesson(
        occasion: Occasion,
        week: Int
    ): Lesson {

        return Lesson(
            occasion,
            week,
            getDate(week, occasion.dayOfWeek)
        )
    }



    /**
     * Convert [JSONArray] to [List] of [JSONObject]
     *
     * @param array array object
     *
     * @return List version
     * */
    private fun jsonArrayToList(
        array: JSONArray
    ): List<JSONObject> {

        val list = mutableListOf<JSONObject>()

        for (i in 0 until array.length()) {
            list.add(array.getJSONObject(i))
        }

        return list
    }



    /**
     * Convert string representation of date and time to [LocalTime]
     * The formatting for the string is assumed to be:
     * yyyy-MM-dd HH:mm:ss.S
     *
     * @param raw formatted string
     *
     * @return Time object
     * */
    private fun getTime(
        raw: String
    ): LocalTime {
        // "1970-01-01 08:20:00.0" -> "08:20:00.0"
        val time = raw.split(" ").last()
        return LocalTime.parse(time, timeFormat)
    }



    /**
     * Get Date from week and weekday
     *
     * @param week Week to get date for
     * @param dayOfWeek Weekday to get date for
     *
     * @return Date object
     * */
    private fun getDate(
        week: Int,
        dayOfWeek: DayOfWeek
    ): LocalDate {
        var date = LocalDate.now()

        if (week < 27) {date = date.with(ChronoField.YEAR, date.year+1L)}

        date = date.with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week.toLong())
        date = date.with(ChronoField.DAY_OF_WEEK, dayOfWeek.value.toLong())

        return date
    }



    /**
     * Get weeks from a week string
     * Week string is assumed to be formatted
     * "N, min-max, min-max, N, N, min-max" etc
     * Where N is a individual week and min-max represent a range of weeks
     *
     * @param raw Week string
     *
     * @return List of weeks
     * */
    private fun parseWeeks(
        raw: String
    ): List<Int>{

        val periods = raw.split(", ")
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
}





/**
 * Raw response
 *
 * @property response Request response object
 * @property stringBody Response body as string
 * */
class RawAPIResponse(
    val response: Response,
    val stringBody: String,
): APIResponse(APIResponseType.Success)