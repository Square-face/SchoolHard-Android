package com.example.schoolhard.API

import java.lang.Exception
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import com.example.schoolhard.utils.getProgress
import java.time.Duration

/*=== USER ===*/



/**
 * User representation.
 *
 * @param id This users unique id
 * @param username The display name for this user
 * @param school The school this user is logged in to
 * @param organization The organization this user is in
 * */
data class User(
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
 * @param name Name as provided by the api (not modifiable)
 * @param url Url used to login with user information
 * */
data class School(
    val name: String,
    val url: String,
)




/**
 * Organization representation
 *
 * @param orgId id from 1 and counting but only for the parent school
 * @param school parent School
 * @param name Organization name
 * */
data class Organization(
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
 * @param subjectId Subject identifier
 * @param name Subject name
 *
 * @property id Unique identifier
 */
data class Subject(
    val subjectId: Int,
    val name: String,
    val id: UUID,
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
data class Occasion(
    val id: UUID,
    val occasionId: Int,
    val subject: Subject,
    val location: Location,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val dayOfWeek: DayOfWeek,
)



/**
 * Lesson representation
 *
 * Stores information about a specific occurrence of a subject. Unlike [Occasion] witch
 * represents when in a week a lesson might be scheduled for. This class represents exactly one
 * schedule item.
 *
 * @param occasion Parent occasion
 * @param week Week this lesson is scheduled for
 * @param date Date this lesson is scheduled for
 * @param uuid UUID to use, if null a new uuid will be generated
 *
 * @property id Unique identifier, if [uuid] is not null it will be used, otherwise a new is generated
 * @property subject Parent subject
 * @property name subject name
 * @property location Where the lesson is going to be taking place
 * @property dayOfWeek What day of the week the lesson happens on
 * @property startTime [LocalDateTime] object representing when the lesson starts
 * @property endTime [LocalDateTime] object representing when the lesson ends
 *
 * @property progress Current progress as a float between 0 and 1. Regenerated every time requested
 * */
data class Lesson (
    val occasion: Occasion,
    val week: Int,
    val date: LocalDate,
    val uuid: UUID? = null,
) {



    // generate a new uuid if [uuid] is null
    val id: UUID = uuid.also { uuid }?: run { UUID.randomUUID() }

    val subject = occasion.subject
    val name = subject.name
    val location = occasion.location

    val dayOfWeek = occasion.dayOfWeek
    val startTime = occasion.startTime.atDate(date)
    val endTime = occasion.endTime.atDate(date)
    val duration = Duration.between(startTime, endTime).toMillis()

    // generated on request
    val progress: Float get() { return getProgress(startTime, LocalDateTime.now(), endTime) }
}



/**
 * Location representation
 *
 * A location that a occasion is scheduled at. I.E a classroom
 *
 * @param uuid UUID to use, if null a new UUID is generated
 * @param name What the place is called
 *
 * @property id Unique identifier
 * */
data class Location (
    val name: String,
    val uuid: UUID? = null,
) {
    val id = uuid.also { uuid }?:run { UUID.randomUUID() }
}