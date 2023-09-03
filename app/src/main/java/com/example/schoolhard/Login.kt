package com.example.schoolhard

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumTouchTargetEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolhard.API.API
import com.example.schoolhard.API.School
import com.example.schoolhard.API.SchoolSoftAPI
import com.example.schoolhard.API.UserType
import com.example.schoolhard.data.Logins
import com.example.schoolhard.ui.theme.SchoolHardTheme


val smallBevel = 4.dp
val largeBevel = 10.dp


class Login : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SchoolHardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val store = getSharedPreferences("logins", MODE_PRIVATE)
                    val logins = Logins(store)
                    val api = SchoolSoftAPI()
                    val vibrator = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager

                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        NavBar()
                        Content(logins = logins, api = api, vibrationManager=vibrator) {
                            startActivity(Intent(applicationContext, MainActivity::class.java))
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun Content(modifier: Modifier=Modifier, logins: Logins, api: API, vibrationManager: VibratorManager, startApp: () -> Unit){

    val schools = rememberSaveable { mutableListOf<School>() }
    var isLoading by remember { mutableStateOf(false) }
    val school = rememberSaveable { mutableStateOf<School?>(null) }
    val username = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }
    val type = rememberSaveable { mutableStateOf(UserType.Student) }
    var isSelectingSchool by remember { mutableStateOf(false) }


    val login = {
        api.login(
            identification = username.value,
            password = password.value,
            school = school.value!!,
            type = type.value,
        ) {
            logins.saveLogin(
                url = school.value!!.url,
                user = it.user,
                setActive = true
            )
            startApp()
        }
    }


    LaunchedEffect(key1 = true) {
        if (schools.size != 0) {
            return@LaunchedEffect
        }
        isLoading = true
        api.schools {

            schools.clear()
            it.schools.forEach {school ->
                schools.add(school)
            }

            isLoading = false
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = if (isSelectingSchool) 35.dp else 200.dp, bottom = 35.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top)
    ) {
        if (isSelectingSchool) {
            SelectSchool(
                schools = schools,
                school = school,
                isLoading = isLoading
            ) {
                isSelectingSchool = false
            }
        } else {
            LoginForm(
                school = school,
                username = username,
                password = password,
                vibrationManager = vibrationManager,
                login = login,
            ) {
                isSelectingSchool = true
            }
        }
    }
}

@Composable
fun SelectSchool(
    modifier: Modifier = Modifier,
    schools: MutableList<School>,
    school: MutableState<School?>,
    isLoading: Boolean,
    close: () -> Unit,
){
    val query = remember { mutableStateOf("") }
    var filtered by remember { mutableStateOf<List<School>>(listOf()) }

    val filter = {
        filtered = schools.filter { school -> return@filter school.name.lowercase().contains(query.value.lowercase()) }
    }

    LaunchedEffect(key1 = isLoading) {
        filter()
    }

    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(10.dp)
            )
            .width(300.dp)
            .fillMaxHeight()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        SchoolSearch(
            query = query
        ){
            query.value = it
            filter()
        }

        if (isLoading) {
            Row {
                Text(
                    text = stringResource(id = R.string.loading),
                    color = MaterialTheme.colorScheme.onSecondary
                    )
            }
        }

        SchoolResults(
            schools = filtered
        ) {
            school.value = it
            close()
        }
    }
}

@Composable
fun SchoolSearch(modifier: Modifier = Modifier, query: MutableState<String>, updateQuery: (String)->Unit) {

    val source = remember {MutableInteractionSource()}
    val focused = source.collectIsFocusedAsState()
    updateQuery(query.value)


    BasicTextField(
        value = query.value,
        onValueChange = {updateQuery(it)},
        textStyle = TextStyle(
            fontSize = 15.sp,
            fontWeight = FontWeight(600),
            color = MaterialTheme.colorScheme.onSecondary,
        ),
        singleLine = true,
        interactionSource = source
    ) {
        Column(
            modifier = modifier
        ) {
            if (!focused.value && query.value == "") {
                Text(
                    text = stringResource(id = R.string.search),
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight(600),
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                )
            } else {
                it.invoke()
            }
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(2.dp)
                    )
                    .fillMaxWidth()
                    .height(4.dp)
            )
        }
    }
}

@Composable
fun SchoolResults(modifier: Modifier = Modifier, schools: List<School>, selectSchool: (School) -> Unit) {
    Log.d("SchoolResults", "${schools.size} schools available as search results")

    LazyColumn(
        modifier = modifier
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(schools) {
            SchoolResult(school = it, select = {school -> selectSchool(school)})
        }
    }
}

@Composable
fun SchoolResult(modifier: Modifier = Modifier, school: School, select: (School) -> Unit){
    Row(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(5.dp),
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .background(
                color = MaterialTheme.colorScheme.secondary,
            )
            .fillMaxWidth()
            .padding(5.dp)
            .clickable { select(school) },
    ) {
        Text(
            text = school.name,
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight(600),
                color = MaterialTheme.colorScheme.onSecondary,
            )
        )
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun LoginForm(
    modifier: Modifier = Modifier,
    school: MutableState<School?>,
    username: MutableState<String>,
    password: MutableState<String>,
    vibrationManager: VibratorManager,
    login: () -> Unit,
    requestSchool: () -> Unit,
){

    Column(
        modifier = modifier
            .width(256.dp)
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
        ) {
        SchoolField(school = school) {requestSchool()}
        UsernameField(username = username)
        PasswordField(password = password)
        LoginButton(
            enabled = (
                    school.value != null &&
                    username.value != "" &&
                    password.value != ""
                    ),
            onClick = login,
            vibratorManager = vibrationManager
        )
    }

}

@Composable
fun SchoolField(modifier: Modifier = Modifier, school: MutableState<School?>, onClick: ()->(Unit)){
    Column(modifier = modifier
        .fillMaxWidth()
        .background(
            color = MaterialTheme.colorScheme.secondary,
            shape = RoundedCornerShape(
                topStart = largeBevel,
                topEnd = largeBevel,
                bottomStart = smallBevel,
                bottomEnd = smallBevel
            )
        )
        .padding(10.dp, 5.dp)
        .clickable { onClick() }
    ) {
        Text(
            text = stringResource(id = R.string.school),
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight(600),
                color = MaterialTheme.colorScheme.background,
            )
        )
        Text(
            text = school.value?.name ?: stringResource(id = R.string.notselected),
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight(600),
                color = Color.Black,
            )
        )
    }
}



@Composable
fun UsernameField(modifier: Modifier = Modifier, username: MutableState<String>){
    val source = remember {MutableInteractionSource()}
    val focused = source.collectIsFocusedAsState()

    val labelColor = if (!focused.value) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.primary

    BasicTextField(
        value = username.value,
        onValueChange = { username.value = it },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth(),
        textStyle = TextStyle(
            fontSize = 15.sp,
            fontWeight = FontWeight(600),
            color = Color.Black,
        ),
        interactionSource = source,
        decorationBox = {
            Column(modifier = modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(smallBevel)
                )
                .padding(10.dp, 5.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(id = R.string.username),
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight(600),
                        color = labelColor,
                    )
                )
                it.invoke()
            }
        }
    )
}

@Composable
fun PasswordField(modifier: Modifier = Modifier, password: MutableState<String>){
    val source = remember {MutableInteractionSource()}
    val focused = source.collectIsFocusedAsState()

    val labelColor = if (!focused.value) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.primary

    BasicTextField(
        value = password.value,
        onValueChange = { password.value = it },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth(),
        textStyle = TextStyle(
            fontSize = 15.sp,
            fontWeight = FontWeight(600),
            color = Color.Black,
        ),
        interactionSource = source,
        visualTransformation = PasswordVisualTransformation(),
        decorationBox = {
            Column(modifier = modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(smallBevel)
                )
                .padding(10.dp, 5.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(id = R.string.password),
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight(600),
                        color = labelColor,
                    )
                )
                it.invoke()
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginButton(modifier: Modifier = Modifier, enabled: Boolean, onClick: () -> Unit, vibratorManager: VibratorManager) {
    CompositionLocalProvider(
        LocalMinimumTouchTargetEnforcement provides false,
    ) {
        TextButton(
            modifier = modifier
                .fillMaxWidth(),
            onClick = {
                if (enabled) { onClick(); return@TextButton}
                vibratorManager.vibrate(
                    CombinedVibration.createParallel(VibrationEffect.createOneShot(15, VibrationEffect.EFFECT_TICK))
                )
            },
            shape = RoundedCornerShape(smallBevel, smallBevel, largeBevel, largeBevel),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
        ) {
            Text(
                text = stringResource(id = R.string.login),
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight(600),
                    color = Color.Black,
                )
            )
        }
    }
}



@Composable
fun NavBar(modifier: Modifier = Modifier){
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary)
            .fillMaxWidth()
            .padding(0.dp, 5.dp)
        ,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            text = stringResource(id = R.string.app_name),
            color=MaterialTheme.colorScheme.onPrimary,
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight(600),
                color = Color(0xFFFFFFFF),
            )
        )
    }
}