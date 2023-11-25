package com.example.schoolhard.data

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolhard.ui.components.lesson.Meta
import com.example.schoolhard.ui.components.lesson.Progress
import com.example.schoolhard.ui.components.lesson.Time
import java.lang.Exception
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import com.example.schoolhard.utils.getProgress
import kotlinx.coroutines.delay
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
        fun from(ordinal: Int): UserType {
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