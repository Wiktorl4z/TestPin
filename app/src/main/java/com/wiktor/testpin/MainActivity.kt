package com.wiktor.testpin

import PinScreenRef
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
                    PinScreenRef()


                 //   PinInputExample()
                    //    PinInputExample()
                }
            }
        }
    }
}


/*
@Preview
@Composable
fun PinInputPreview() {
    PinInput(
        pinLength = 4,
        onPinComplete = { pin -> println("PIN entered: $pin") },
        isError = false
    )
}*/
