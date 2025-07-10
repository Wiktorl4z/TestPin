package com.wiktor.testpin

import PinFieldStyle
import PinFieldWithErrorMessage
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wiktor.testpin.ui.theme.TestPinTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestPinTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                ) { paddingValues ->
                    //     PinInput(6,{}, isError = false)

                    //      DebugNumericKeyboardSafe()
                  //      PinInput2(4,{}, isError = false) // poprawnie dzialajacy
                  //  PinInputTestingRefactor(4,{}, isError = false)
                        // PinInputDone(4,{}, isError = false)
                   // PinInputChatGPT(4,{}, isError = false)
                //    PinInputMiddle(4,{}, isError = false)

                  //  PinScreen()
                  //  PinScreenRef()
                    PinScreen()

                 //   PinInputExample()
                    //    PinInputExample()
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

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PinFieldWithErrorMessage(
                pinLength = 6,
                errorMessage = errorMessage,
                onPinChange = { newPin ->
                    pinCode = newPin
                    // Clear error when user types
                    errorMessage = null
                },
                style = customStyle,
                modifier = Modifier.padding(vertical = 32.dp)
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    errorMessage = when {
                        pinCode.length < 6 -> "Please complete the PIN"
                        pinCode != "123456" -> "Incorrect PIN"
                        else -> null
                    }
                }
            ) {
                Text("Verify PIN")
            }
        }
    }
}