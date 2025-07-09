package com.wiktor.testpin.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

@Composable
fun DebugNumericKeyboardSafe() {
    val value = remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current

    // Opóźniona inicjalizacja — bez zamrożenia UI
    LaunchedEffect(Unit) {
        snapshotFlow { view.isAttachedToWindow }
            .filter { it } // tylko jeśli widok jest przypięty
            .first()
        delay(300) // niewielkie opóźnienie dla bezpieczeństwa
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    BasicTextField(
        value = value.value,
        onValueChange = { value.value = it },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        modifier = Modifier
            .padding(16.dp)
            .border(1.dp, Color.Gray)
            .focusRequester(focusRequester)
            .focusable()
    )
}

