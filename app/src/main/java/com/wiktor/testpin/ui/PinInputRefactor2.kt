import android.graphics.Rect
import android.view.KeyEvent
import android.view.ViewTreeObserver
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp


@Composable
fun PinFieldWithErrorMessage2(
    pinLength: Int = 6,
    errorMessage: String?,
    onPinChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    style: PinFieldStyle = defaultPinFieldStyle(),
) {
    // Use a single string state instead of list of nullable chars
    val pinState = remember { mutableStateOf("") }
    val focusRequesters = remember { List(pinLength) { FocusRequester() } }
    val keyboardController = LocalSoftwareKeyboardController.current
    val isKeyboardOpen by rememberMyKeyboardVisibility()

    // Track focus index
    val currentFocusIndex = remember { mutableIntStateOf(0) }

    // Calculate target index (first empty or last)
    val targetIndex by remember(pinState.value) {
        derivedStateOf {
            pinState.value.length.let {
                if (it < pinLength) it else pinLength - 1
            }
        }
    }

    // Handle pin changes
    LaunchedEffect(pinState.value) {
        onPinChange(pinState.value)
    }

    // Auto-focus first box on initial composition
    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
        keyboardController?.show()
    }

    // Handle refocus when error occurs
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            currentFocusIndex.intValue = targetIndex
            focusRequesters[targetIndex].requestFocus()
            keyboardController?.show()
        }
    }

    // Calculate total width
    val totalWidth = remember(pinLength, style) {
        (style.itemWidth * pinLength) + (style.spacing * (pinLength - 1))
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // PIN fields container
        Box(Modifier.width(totalWidth)) {
            PinFields(
                pinValue = pinState.value,
                pinLength = pinLength,
                focusRequesters = focusRequesters,
                currentFocusIndex = currentFocusIndex.intValue,
                targetIndex = targetIndex,
                onPinChange = { newValue -> pinState.value = newValue },
                isError = errorMessage != null,
                keyboardController = keyboardController,
                isKeyboardOpen = isKeyboardOpen,
                style = style
            )
        }

        Spacer(Modifier.height(16.dp))

        // Error message
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = style.errorColor,
                style = style.errorTextStyle,
                modifier = Modifier.width(totalWidth)
            )
        }
    }
}

@Composable
private fun PinFields(
    pinValue: String,  // This is the full PIN value
    pinLength: Int,
    focusRequesters: List<FocusRequester>,
    currentFocusIndex: Int,
    targetIndex: Int,
    onPinChange: (String) -> Unit,
    isError: Boolean,
    keyboardController: SoftwareKeyboardController?,
    isKeyboardOpen: Boolean,
    style: PinFieldStyle,
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        for (index in 0 until pinLength) {
            val char = pinValue.getOrNull(index)
            val isFocused = currentFocusIndex == index

            PinField(
                index = index,
                char = char,
                isFocused = isFocused,
                isTarget = targetIndex == index,
                focusRequester = focusRequesters[index],
                onFocusChange = { newIndex ->
                    if (newIndex != currentFocusIndex) {
                        focusRequesters[newIndex].requestFocus()
                    }
                },
                onPinChange = onPinChange,
                pinLength = pinLength,
                isError = isError,
                keyboardController = keyboardController,
                isKeyboardOpen = isKeyboardOpen,
                style = style,
                currentPin = pinValue  // Pass the current PIN value here
            )

            if (index < pinLength - 1) {
                Spacer(Modifier.width(style.spacing))
            }
        }
    }
}

@Composable
private fun PinField(
    index: Int,
    char: Char?,
    isFocused: Boolean,
    isTarget: Boolean,
    focusRequester: FocusRequester,
    onFocusChange: (Int) -> Unit,
    onPinChange: (String) -> Unit,
    pinLength: Int,
    isError: Boolean,
    keyboardController: SoftwareKeyboardController?,
    isKeyboardOpen: Boolean,
    style: PinFieldStyle,
    currentPin: String  // Add this parameter
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(style.itemWidth)
            .height(style.itemHeight)
            .clip(RoundedCornerShape(style.cornerRadius))
            .background(style.backgroundColor, RoundedCornerShape(style.cornerRadius))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Always focus the last box when clicked
                onFocusChange(pinLength - 1)
                if (!isKeyboardOpen) {
                    keyboardController?.show()
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, style.borderColor, RoundedCornerShape(style.cornerRadius)),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = char?.toString() ?: "",
                onValueChange = { newValue ->
                    handleValueChange(
                        newValue = newValue,
                        currentIndex = index,
                        currentPin = currentPin,  // Use the passed currentPin
                        pinLength = pinLength,
                        onPinChange = onPinChange,
                        onFocusChange = onFocusChange,
                        keyboardController = keyboardController
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = style.textStyle,
                singleLine = true,
                maxLines = 1,
                cursorBrush = SolidColor(Color.Transparent),
                visualTransformation = PinTransformationDone,
                interactionSource = interactionSource,
                enabled = isTarget,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            onFocusChange(index)
                        }
                    }
                    .onKeyEvent { event ->
                        handleKeyEvent(
                            event = event,
                            index = index,
                            char = char,
                            currentPin = currentPin,  // Use the passed currentPin
                            onPinChange = onPinChange,
                            onFocusChange = onFocusChange
                        )
                    }
            )
        }

        if (isFocused || isError) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        color = if (isError) style.errorColor else style.focusedColor,
                        shape = RoundedCornerShape(
                            bottomStart = style.cornerRadius,
                            bottomEnd = style.cornerRadius
                        )
                    )
            )
        }
    }
}

private fun handleValueChange(
    newValue: String,
    currentIndex: Int,
    currentPin: String,
    pinLength: Int,
    onPinChange: (String) -> Unit,
    onFocusChange: (Int) -> Unit,
    keyboardController: SoftwareKeyboardController?
) {
    when {
        newValue.isEmpty() -> handleDeletion(
            currentIndex,
            currentPin,
            onPinChange,
            onFocusChange
        )
        else -> {
            val digits = newValue.filter { it.isDigit() }
            if (digits.isEmpty()) return

            when {
                digits.length == 1 -> handleSingleDigit(
                    digits[0],
                    currentIndex,
                    currentPin,
                    pinLength,
                    onPinChange,
                    onFocusChange,
                    keyboardController
                )
                else -> handlePaste(
                    digits,
                    currentIndex,
                    currentPin,
                    pinLength,
                    onPinChange,
                    onFocusChange,
                    keyboardController
                )
            }
        }
    }
}

private fun handleSingleDigit(
    digit: Char,
    currentIndex: Int,
    currentPin: String,
    pinLength: Int,
    onPinChange: (String) -> Unit,
    onFocusChange: (Int) -> Unit,
    keyboardController: SoftwareKeyboardController?
) {
    if (currentIndex >= pinLength) return

    val newPin = if (currentIndex < currentPin.length) {
        currentPin.substring(0, currentIndex) + digit + currentPin.substring(currentIndex + 1)
    } else {
        currentPin + digit
    }

    onPinChange(newPin)

    when {
        currentIndex < pinLength - 1 -> {
            onFocusChange(currentIndex + 1)
        }
        else -> {
            keyboardController?.hide()
        }
    }
}

private fun handlePaste(
    digits: String,
    currentIndex: Int,
    currentPin: String,
    pinLength: Int,
    onPinChange: (String) -> Unit,
    onFocusChange: (Int) -> Unit,
    keyboardController: SoftwareKeyboardController?
) {
    val availableLength = pinLength - currentIndex
    if (availableLength <= 0) return

    val digitsToPaste = digits.take(availableLength)
    val newPin = if (currentIndex < currentPin.length) {
        currentPin.substring(0, currentIndex) + digitsToPaste +
                currentPin.substring(minOf(currentIndex + digitsToPaste.length, currentPin.length))
    } else {
        currentPin + digitsToPaste
    }

    onPinChange(newPin)

    val nextIndex = minOf(currentIndex + digitsToPaste.length, pinLength - 1)
    if (nextIndex < pinLength - 1) {
        onFocusChange(nextIndex + 1)
    } else {
        keyboardController?.hide()
    }
}

private fun handleDeletion(
    currentIndex: Int,
    currentPin: String,
    onPinChange: (String) -> Unit,
    onFocusChange: (Int) -> Unit
) {
    if (currentPin.isEmpty()) return

    val newPin = if (currentIndex < currentPin.length) {
        currentPin.substring(0, currentIndex) + currentPin.substring(currentIndex + 1)
    } else {
        currentPin
    }

    onPinChange(newPin)

    // Handle focus movement
    when {
        currentIndex > 0 -> onFocusChange(maxOf(0, currentIndex - 1))
        else -> onFocusChange(0)
    }
}

private fun handleKeyEvent(
    event: androidx.compose.ui.input.key.KeyEvent,
    index: Int,
    char: Char?,
    currentPin: String,
    onPinChange: (String) -> Unit,
    onFocusChange: (Int) -> Unit
): Boolean {
    if (event.nativeKeyEvent.keyCode != KeyEvent.KEYCODE_DEL) return false

    return when {
        char != null -> {
            // Delete current character
            val newPin = if (index < currentPin.length) {
                currentPin.substring(0, index) + currentPin.substring(index + 1)
            } else {
                currentPin
            }
            onPinChange(newPin)
            true
        }
        index > 0 -> {
            // Delete previous character
            val prevIndex = index - 1
            val newPin = if (prevIndex < currentPin.length) {
                currentPin.substring(0, prevIndex) + currentPin.substring(prevIndex + 1)
            } else {
                currentPin
            }
            onPinChange(newPin)
            onFocusChange(prevIndex)
            true
        }
        else -> false
    }
}

