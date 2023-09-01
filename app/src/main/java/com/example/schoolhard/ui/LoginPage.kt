package com.example.schoolhard.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.example.schoolhard.API.School
import com.example.schoolhard.API.SchoolSoftAPI
import com.example.schoolhard.API.UserType
import com.example.schoolhard.data.Logins
import com.example.schoolhard.ui.theme.SchoolHardTheme

@Composable
fun LoginPage(modifier: Modifier = Modifier, logins: Logins) {
    /*Main page for user login
    *
    * */

    SchoolHardTheme {
        Column(modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .systemBarsPadding()
            .background(MaterialTheme.colorScheme.background)
        ) {
            NavBar()
            Content(logins = logins)
        }
    }
}

@Composable
fun NavBar(modifier: Modifier = Modifier) {
    Row(modifier = modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.primary)
        .height(50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Text(
            text = "SchoolHard",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight(600),
                color = Color(0xFFFFFFFF),
            )
        )
    }
}

@Composable
fun Content(modifier: Modifier = Modifier, logins: Logins) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        LoginForm(logins = logins)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginForm(modifier: Modifier = Modifier, logins: Logins) {
    Column(modifier = modifier) {
        val school = remember { mutableStateOf<School?>(null) }
        var username by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }

        val localContext = LocalContext.current

        SchoolSelect(school = school)

        TextField(
            value = username,
            onValueChange = {username = it},
            label = { Text("Username") },
            textStyle = TextStyle(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                autoCorrect = true,
                imeAction = ImeAction.Next,
                ),
            singleLine = true,
        )

        TextField(
            value = password,
            onValueChange = {password = it},
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                autoCorrect = false,
                ),
            singleLine = true,
        )

        Button(onClick = {
            val failToast = Toast.makeText(localContext, "Incorrect login information", Toast.LENGTH_SHORT)

            val api = SchoolSoftAPI()

            Log.v("UI", "attempting login with username: \"$username\", password: \"$password\", school:\"${school.value!!.url}\"")
            api.login(
                identification = username,
                password = password,
                school = school.value!!,
                UserType.Student,
                {response ->  response.body?.let { Log.v("UI", it) }
                    failToast.show()}
            ){
                logins.saveLogin(school.value!!.url, it.user, setActive = true)
            }
        }) {
            Text(text = "Login")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolSelect(modifier: Modifier = Modifier, school: MutableState<School?>) {
    var schools by remember { mutableStateOf(mutableListOf<School>()) }
    var isSearching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var isOpen by remember { mutableStateOf(false) }
    var isLoaded by remember { mutableStateOf(false) }
    val focusSearch = remember { FocusRequester() }
    var filteredSchools by remember { mutableStateOf(schools.toList()) }


    LaunchedEffect(true) {
        SchoolSoftAPI().schools {
            isLoaded = true
            schools = it.schools.toMutableList()
            Log.v("UI", "response returned ${schools.size} schools")
        }
    }

    Box(modifier=modifier) {
        TextField(
            value = if (isSearching) query else school.value?.name ?: (if (!isLoaded) "Loading" else "Not Selected"),
            onValueChange = {
                query = it
                filteredSchools = schools.filter { it.name.lowercase().contains(query.lowercase()) }
                            },
            label = {
                Text(text = "School")
            },
            trailingIcon = { Icon(Icons.Outlined.ArrowDropDown, null) },
            leadingIcon = {
                IconButton(onClick = { }) {
                    if (isLoaded) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = ""
                        )
                    } else {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = ""
                        )
                    }
                }
            },
            modifier = Modifier.focusRequester(focusSearch)
            )
        DropdownMenu(expanded = isOpen, onDismissRequest = { isOpen = false }, properties = PopupProperties(focusable = false)) {

            Log.v("UI", "Filtered schools down to ${filteredSchools.size}")
            if (filteredSchools.size < 10) {
                isOpen = true
                filteredSchools.forEach {
                    DropdownMenuItem(text = { Text(text = it.name) }, onClick = { school.value = it; focusSearch.freeFocus(); isSearching = false; isOpen = false;})
                }
            }
        }
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Transparent)
                .padding(10.dp)
                .clickable(
                    onClick = { isSearching = true; isOpen = true; focusSearch.requestFocus() }
                )
        )
    }

}

@Preview(showBackground = true)
@Composable
fun NavBarPreview() {
    SchoolHardTheme {
        NavBar()
    }
}