package com.example.schoolhard.API

import android.util.Log
import okhttp3.Response
import org.json.JSONObject
import java.util.Date

enum class userType{
    Unknown, Student, Parent, Teacher
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
    val week: Int,
    val room: String,
    val teacher: String,
    val startTime: Date,
    val endTime: Date,
)
class Lesson(
    val name: String,
    val id: String,
    val occasions: List<Occasion>,
)

enum class APIStatus{
    Loggedin, Loggedout, Disconnected
}

enum class APIResponseType{
    Failed, Success
}

enum class APIResponseFailureReason{
    InvalidAuth, NotLoggedIn, InvalidAPIClass, InternalServerError
}

class Filter(
    val from: Date,
    val to: Date,
)

open class APIResponse(
    val type: APIResponseType,
    val response: Response,
    val body: JSONObject?,
)

class SuccessfulAPIResponse(
    response: Response,
    body: JSONObject?,
): APIResponse(APIResponseType.Success, response, body)

class SuccessfulLessonResponse(
    response: Response,
    body: JSONObject,
    val lessons: List<Lesson>
): APIResponse(APIResponseType.Success, response, body)

class FailedAPIResponse(
    val reason: APIResponseFailureReason,
    val message: String,
    response: Response,
    body: JSONObject?
): APIResponse(APIResponseType.Failed, response, body)

open class API() {
    var status: APIStatus = APIStatus.Disconnected
    open fun login(
        user: User,
        failureCallback: (FailedAPIResponse)->(Unit) = {response -> Log.e("Request", response.message)},
        successCallback: (SuccessfulAPIResponse)->(Unit),
    ){}

    open fun logout(
        failureCallback: (FailedAPIResponse)->(Unit) = {response -> Log.e("Request", response.message)},
        successCallback: (SuccessfulAPIResponse)->(Unit),
    ){}

    open fun lessons(
        filter: Filter,
        failureCallback: (FailedAPIResponse)->(Unit) = {response -> Log.e("Request", response.message)},
        successCallback: (SuccessfulLessonResponse)->(Unit),
    ){}

    open fun lunch(
        filter: Filter,
        failureCallback: (FailedAPIResponse)->(Unit) = {response -> Log.e("Request", response.message)},
        successCallback: (SuccessfulAPIResponse)->(Unit),
    ){}

    open fun userInfo(
        failureCallback: (FailedAPIResponse)->(Unit) = {response -> Log.e("Request", response.message)},
        successCallback: (SuccessfulAPIResponse)->(Unit),
    ){}

    open fun schools(
        failureCallback: (FailedAPIResponse)->(Unit) = {response -> Log.e("Request", response.message)},
        successCallback: (SuccessfulAPIResponse)->(Unit),
    ){}
}
