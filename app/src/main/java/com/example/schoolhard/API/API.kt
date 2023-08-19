package com.example.schoolhard.API

import android.util.Log
import okhttp3.Response
import okhttp3.ResponseBody
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class userType{
    Student, Parent, Teacher
}

open class User(
    val username: String,
    val password: String,
    val school: String,
    val userType: userType
)

class Student(
    username: String,
    password: String,
    school: String
): User(userType = userType.Student, username = username, password = password, school = school)

class Occasion(
    val lesson: Lesson,
    val week: Int,
    val weekDay: DayOfWeek,
    val place: String,
    val teacher: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
)
class Lesson(
    val fullName: String,
    val name: String,
    val id: Int,
    val externalId: String,
)

class APIStatus{
    var connected = false
    var loggedin = false
}

enum class APIResponseType{
    Failed, Success
}

enum class APIResponseFailureReason{
    InvalidAuth, NotLoggedIn, InternalServerError, ConnectionFailure, NullError
}

class Filter(
    val from: LocalDateTime,
    val to: LocalDateTime,
    val maxCount: Int,
)

open class APIResponse(
    val type: APIResponseType,
    open val response: Response?,
    open val body: String?,
)

class SuccessfulAPIResponse(
    override val response: Response,
    override val body: String,
): APIResponse(APIResponseType.Success, response, body)

class SuccessfulLessonResponse(
    val lessons: List<Occasion>
): APIResponse(APIResponseType.Success, null, null)

class FailedAPIResponse(
    val reason: APIResponseFailureReason,
    val message: String,
    response: Response?,
    body: String?
): APIResponse(APIResponseType.Failed, response, body)

open class API() {
    var status = APIStatus()
    open fun login(
        user: User,
        failureCallback: (FailedAPIResponse)->(Unit) = {response -> Log.e("API", response.message)},
        successCallback: (SuccessfulAPIResponse)->(Unit),
    ){}

    open fun logout(
        failureCallback: (FailedAPIResponse)->(Unit) = {response -> Log.e("API", response.message)},
        successCallback: (SuccessfulAPIResponse)->(Unit),
    ){}

    open fun lessons(
        filter: Filter?,
        failureCallback: (FailedAPIResponse)->(Unit) = {response -> Log.e("API", response.message)},
        successCallback: (SuccessfulLessonResponse)->(Unit),
    ){}

    open fun lunch(
        filter: Filter?,
        failureCallback: (FailedAPIResponse)->(Unit) = {response -> Log.e("API", response.message)},
        successCallback: (SuccessfulAPIResponse)->(Unit),
    ){}

    open fun userInfo(
        failureCallback: (FailedAPIResponse)->(Unit) = {response -> Log.e("API", response.message)},
        successCallback: (SuccessfulAPIResponse)->(Unit),
    ){}

    open fun schools(
        failureCallback: (FailedAPIResponse)->(Unit) = {response -> Log.e("Request", response.message)},
        successCallback: (SuccessfulAPIResponse)->(Unit),
    ){}
}
