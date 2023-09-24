package com.example.schoolhard.API

import java.lang.Exception
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
) {
    val userType = UserType.Student
}



/**
 * User type ENUM
 *
 * Different types of users represented by a enum class
 * */
enum class UserType{
    Teacher, Student, Parent;

    companion object {

        /**
         * Convert ordinal to enum entry
         *
         * @param ordinal The ordinal representation
         *
         * @throws Exception Supplied ordinal was not one of the possible entries
         * @return enum entry
         * */
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




/**
 * Organization representation
 *
 * @param id unique identifier
 * @param orgId id from 1 and counting but only for the parent school
 * @param school parent School
 * @param name Organization name
 * */
open class Organization(
    val id: Int,
    val orgId: Int,
    val school: School,
    val name: String,
)





/*=== Subjects ===*/



/**
 * School representation
 *
 * Holds information about an entire subject, i.e Math, English, etc
 *
 * @param id Unique identifier
 * @param subjectId Subject identifier
 * @param name Subject name
 */
open class Subject(
    val id: Int,
    val subjectId: Int,
    val name: String,
)



/**
 * Occasion representation
 *
 * Stores information about a specific time of week that a lesson might occur at
 *
 * @param id Unique identifier
 * @param occasionId Occasion identifier. Unique to every occasion but is the same for multiple accounts
 * @param subject Parent subject
 * @param location Where the occasion is going to be happening at
 * @param startTime Time of day the occasion starts at
 * @param endTime Time of day the occasion ends at
 * @param dayOfWeek Day of the week the occasion occurs at
 * */
open class Occasion(
    val id: Int,
    val occasionId: Int,
    val subject: Subject,
    val location: Location,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val dayOfWeek: DayOfWeek,
)



/**
 * Lesson repsentation
 *
 * Stores information about a specific occurrence of a subject. Unlike [Occasion] witch
 * represents when in a week a lesson might be scheduled for. This class represents exactly one
 * schedule item.
 *
 * @param id Unique identifier
 * @param occasion Parent occasion
 * @param week Week this lesson is scheduled for
 * @param date Date this lesson is scheduled for
 * */
open class Lesson (
    val id: Int,
    val occasion: Occasion,
    val week: Int,
    val date: LocalDate,
)



/**
 * Location representation
 *
 * A location that a occasion is scheduled at. I.E a classroom
 *
 * @param id Unique identifier
 * @param name What the place is called
 * */
open class Location (
    val id: Int,
    val name: String,
)