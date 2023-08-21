package com.example.schoolhard.ui

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolhard.API.SchoolSoftAPI
import com.example.schoolhard.API.UserType
import com.example.schoolhard.ui.ui.theme.SchoolHardTheme

@Composable
fun LoginPage(modifier: Modifier = Modifier, logins: SharedPreferences) {
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
fun Content(modifier: Modifier = Modifier, logins: SharedPreferences) {
    Box(
        modifier = modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        LoginForm(logins = logins)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginForm(modifier: Modifier = Modifier, logins: SharedPreferences) {
    Column {
        var school by rememberSaveable { mutableStateOf("") }
        var username by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }

        val localContext = LocalContext.current

        TextField(
            value = school,
            onValueChange = {school = it},
            label = { Text("School") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                autoCorrect = true,
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Next,
                ),
            singleLine = true,
        )

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

            Log.v("UI", "attempting login with username: \"$username\", password: \"$password\", school:\"$school\"")
            api.login(
                identification = username,
                password = password,
                school = school,
                UserType.Student,
                {response ->  response.body?.let { Log.v("UI", it) }
                    failToast.show()}

            ){
                val index = logins.getInt("count", 0)+1
                val edit = logins.edit()
                edit.putString("${index}appKey", api.appKey!!)
                edit.putString("${index}school", school.trim())
                edit.putInt("count", index)
                edit.putInt("index", index)
                edit.apply()
            }
        }) {
            Text(text = "Login")
        }
    }
}
@Preview(showBackground = true)
@Composable
fun NavBarPreview() {
    SchoolHardTheme {
        NavBar()
    }
}