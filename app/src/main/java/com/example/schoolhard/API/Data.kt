package com.example.schoolhard.API

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/*=== USER ===*/

open class User(
    val id: Int,
    val school: School,
    val organizations: List<Organization>,
    val username: String,
    val key: String,
)



open class School(
    val id: Int,
    val name: String,
    val url: String,
)



open class Organization(
    val id: Int, // always unique
    val orgId: Int, // schoolsoft id
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