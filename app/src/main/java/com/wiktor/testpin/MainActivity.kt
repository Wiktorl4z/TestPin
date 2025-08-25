package com.wiktor.testpin

import PinFieldStyle
import PinFieldWithErrorMessage
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wiktor.testpin.ui.theme.TestPinTheme


class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestPinTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                ) { _->
                    PinScreen()
                }
            }
        }
    }
}

@Composable
fun PinScreen() {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var pinCode by remember { mutableStateOf("") }

    val customStyle = remember {
        PinFieldStyle(
            itemWidth = 40.dp,
            itemHeight = 50.dp,
            spacing = 12.dp,
            cornerRadius = 4.dp,
            borderColor = Color.Gray,
            focusedColor = Color.Blue,
            errorColor = Color.Red,
            textStyle = TextStyle(
                fontSize = 26.sp,
                textAlign = TextAlign.Center,
                color = Color.DarkGray,
                fontWeight = FontWeight.Bold
            ),
            errorTextStyle = TextStyle(
                fontSize = 14.sp,
                color = Color.Red
            ),
            backgroundColor = Color.White
        )
    }

    val pinLength = 5
    val totalWidthPx = with(LocalDensity.current) {
        ((customStyle.itemWidth * pinLength) + (customStyle.spacing * (pinLength - 1))).toPx()
    }

    var containerWidthPx by remember { mutableStateOf(0f) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .onGloballyPositioned { coordinates ->
                    containerWidthPx = coordinates.size.width.toFloat()
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (containerWidthPx == 0f) {
                // Waiting for layout to measure
                Text("Measuring layout...")
            } else if (containerWidthPx >= totalWidthPx) {
                PinFieldWithErrorMessage(
                    pinLength = pinLength,
                    errorMessage = errorMessage,
                    onPinChange = { newPin ->
                        pinCode = newPin
                        errorMessage = null
                    },
                    style = customStyle,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                Text(
                    "Screen too small to display PIN fields properly.",
                    color = Color.Red
                )
            }

            Spacer(Modifier.height(24.dp))

            var text by remember { mutableStateOf("") }
            OutlinedTextField(
                value = text,
                onValueChange = { newText ->
                    // opcjonalnie filtruj tylko cyfry
                    if (newText.all { it.isDigit() }) {
                        text = newText
                    }
                },
                label = { Text("Wpisz liczby") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

       /*     Button(
                onClick = {
                    errorMessage = when {
                        pinCode.length < 6 -> "Please complete the PIN"
                        pinCode != "123456" -> "Incorrect PIN"
                        else -> null
                    }
                }
            ) {
                Text("Verify PIN")
            }*/
        }
    }
}