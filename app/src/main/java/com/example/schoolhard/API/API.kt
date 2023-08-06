package com.example.schoolhard.API

import okhttp3.ResponseBody
import java.util.Date

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

enum class APIStatus{
    Loggedin, Loggedout, Disconnected
}

enum class APIResponseType{
    Failed, Success
}

enum class APIResponseFailureReason{
    InvalidAuth, NotLoggedIn, InvalidAPIClass
}

open class APIResponse(
    val type: APIResponseType,
    val body: ResponseBody?
)

class Filter(
    val from: Date,
    val to: Date,
)

class SuccessfulAPIResponse(
    body: ResponseBody
): APIResponse(APIResponseType.Success, body)

class FailedAPIResponse(
    val reason: APIResponseFailureReason,
    val message: String,
    body: ResponseBody?
): APIResponse(APIResponseType.Failed, body)

open class API() {
    var status: APIStatus = APIStatus.Disconnected
    open fun login(user: User): APIResponse{ return FailedAPIResponse(
        APIResponseFailureReason.InvalidAPIClass,
        "This function is from the base class and has no functionality",
        null) }
    open fun logout(): APIResponse{ return FailedAPIResponse(
        APIResponseFailureReason.InvalidAPIClass,
        "This function is from the base class and has no functionality",
        null) }

    open fun lessons(filter: Filter): APIResponse{
        return FailedAPIResponse(
            APIResponseFailureReason.InvalidAPIClass,
            "This function is from the base class and has no functionality",
            null) }

    open fun lunch(filter: Filter): APIResponse{
        return FailedAPIResponse(
            APIResponseFailureReason.InvalidAPIClass,
            "This function is from the base class and has no functionality",
            null) }

    open fun userInfo(): APIResponse{
        return FailedAPIResponse(
            APIResponseFailureReason.InvalidAPIClass,
            "This function is from the base class and has no functionality",
            null) }

    open fun schools(): APIResponse{
        return FailedAPIResponse(
            APIResponseFailureReason.InvalidAPIClass,
            "This function is from the base class and has no functionality",
            null) }
}
