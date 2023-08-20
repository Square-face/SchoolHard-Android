package com.example.schoolhard.API

import android.util.Log
import okhttp3.Response
import java.io.InputStream
import java.io.OutputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class UserType{
    Student, Parent, Teacher
}

open class Organization(
    val name: String,
    val className: String,
    val id: Int,
    val userId: Int,
)

open class User(
    val username: String,
    val appKey: String,
    val userId: Int,
    val userType: UserType,
    val organizations: List<Organization>,
)

class Student(
    username: String,
    appKey: String,
    userId: Int,
    organizations: List<Organization>,
): User(
    userType = UserType.Student,
    username = username,
    appKey = appKey,
    userId = userId,
    organizations = organizations,
)

class Occasion(
    val userid: Int,
    val orgId: Int,
    val subject: Subject,
    val week: Int,
    val weekDay: DayOfWeek,
    val place: String,
    val teacher: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
)

class Subject(
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

open class API {
    var status = APIStatus()
    var userId: Int = 0
    var orgId: Int = 0

    open fun login(
        identification: String,
        password: String,
        school: String,
        type: UserType,
        failureCallback: (FailedAPIResponse)->(Unit) = {response -> Log.e("API", response.message)},
        successCallback: (SuccessfulAPIResponse)->(Unit),
    ){}

    open fun loginSaved(
        user: User,
        failureCallback: (FailedAPIResponse) -> Unit = {response -> Log.e("API", response.message)},
        successCallback: (SuccessfulAPIResponse) -> Unit
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
