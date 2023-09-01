package com.example.schoolhard.API

import android.util.Log
import com.example.schoolhard.data.Login
import okhttp3.Response
import java.lang.Exception
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class UserType{
    Parent, Student, Teacher;

    companion object {
        fun from(ordinal: Int): UserType{
            when (ordinal) {
                Parent.ordinal -> return Parent
                Student.ordinal -> return Student
                Teacher.ordinal -> return Teacher
            }

            throw Exception("Ordinal outside range")
        }
    }
}

open class LoginMethods(
    val parents: List<Int>,
    val students: List<Int>,
    val teacher: List<Int>,
)

open class School(
    val name: String,
    val url: String,
    val loginMethods: LoginMethods,
) {
}

open class Organization(
    val name: String,
    val className: String,
    val id: Int,
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

class SuccessfulSchoolsResponse(
    val schools: List<School>
): APIResponse(APIResponseType.Success, null, null)

class SuccessfulLoginResponse(
    val user: User,
    override val response: Response,
    override val body: String
): APIResponse(APIResponseType.Success, response, body)

class FailedAPIResponse(
    val reason: APIResponseFailureReason,
    val message: String,
    response: Response?,
    body: String?
): APIResponse(APIResponseType.Failed, response, body)

open class API {
    var status = APIStatus()
    var userId: Int = 1
    var orgId: Int = 1

    open fun login(
        identification: String,
        password: String,
        school: School,
        type: UserType,
        failureCallback: (FailedAPIResponse)->(Unit) = { response -> Log.e("API", response.message)},
        successCallback: (SuccessfulLoginResponse)->(Unit),
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
        successCallback: (SuccessfulSchoolsResponse)->(Unit),
    ){}

    open fun loginWithSaved(login: Login) {}
}
