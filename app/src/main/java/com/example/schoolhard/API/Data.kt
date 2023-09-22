package com.example.schoolhard.API

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/*=== USER ===*/



/**
 * User representation.
 *
 * @param id This users unique id
 * @param username The display name for this user
 * @param school The school this user is logged in to
 * @param organization The organization this user is in
 * */
open class User(
    val id: Int,
    val username: String,
    val school: School,
    val organization: Organization,
)



/**
 * School representation
 *
 * @param id Unique id
 * @param name Name as provided by the api (not modifiable)
 * @param loginUrl Url used to login with user information
 * */
open class School(
    val id: Int,
    val name: String,
    val loginUrl: String,
)




open class Organization(
    val id: Int,
    val orgId: Int,
    val user: User,
    val name: String,
)





/*=== Subjects ===*/

open class Subject(
    val id: Int,
    val name: String,
)



open class Occasion(
    val id: Int,
    val subject: Subject,
    val room: Room,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val week: Int,
    val dayOfWeek: DayOfWeek,
    val date: LocalDate,
)



open class Lesson (
    val id: Int,
    val occasion: Occasion
)



open class Room (
    val id: Int,
    val name: String,
)