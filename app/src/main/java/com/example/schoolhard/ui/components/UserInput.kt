package com.example.schoolhard.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

class UserInput { companion object {

    /**
     * Button input field
     *
     * @param title Switch title displayed to the user
     * @param description short description explaining what the switch does
     * @param disabled If the switch should be intractable or not
     * @param onClick callback function that runs when the button is pressed
     * */
    @Composable
    fun Button(
        modifier: Modifier = Modifier,
        title: String,
        description: String? = null,
        disabled: Boolean = false,
        onClick: () -> (Unit) = {},
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (description != null) {
                Text(
                    text = description,
                    fontSize = 15.sp,
                    fontWeight = FontWeight(400),
                    color = Color.LightGray,
                    maxLines = 2,
                )
            }
            
            androidx.compose.material3.Button(
                enabled = !disabled,
                onClick={
                    Log.d("Field - Button", "$title pressed")
                    onClick()
                }

            ) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight(500),
                    color = Color.White,
                    maxLines = 1,
                )
            }
        }
    }





    /**
     * Switch input field
     *
     * @param state Start state
     * @param title Switch title displayed to the user
     * @param description short description explaining what the switch does
     * @param disabled If the switch should be intractable or not
     * @param onChange callback function that runs when the state of the switch changes
     * */
    @Composable
    fun Toggle(modifier: Modifier = Modifier,
               state: Boolean,
               title: String,
               description: String? = null,
               disabled:Boolean=false,
               onChange: (Boolean) -> (Unit)) {
        Row(
            modifier = modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            var checked by remember { mutableStateOf(state) }


            Column(
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight(500),
                    color = Color.White,
                    maxLines = 1,
                )
                if (description != null) {
                    Text(
                        text = description,
                        fontSize = 15.sp,
                        fontWeight = FontWeight(400),
                        color = Color.LightGray,
                        maxLines = 1,
                    )
                }
            }

            Switch(
                checked = checked,
                enabled = !disabled,
                onCheckedChange = {
                    Log.d("Field - Toggle", "$title ${if (it) "enabled" else "disabled"}")
                    checked = it
                    onChange(checked)
                }
            )
        }
    }
} }