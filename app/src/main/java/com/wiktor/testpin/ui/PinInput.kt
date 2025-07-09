import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.border
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import kotlinx.coroutines.delay

@Composable
fun PinInput(
    pinLength: Int = 6,
    onPinComplete: (String) -> Unit,
    onPinChange: (String) -> Unit = {},
    isError: Boolean = false,
    isEnabled: Boolean = true,
    clearPin: Boolean = false,
    onClearComplete: () -> Unit = {},
    modifier: Modifier = Modifier,
    style: PinInputStyle = PinInputStyle.default()
) {
    // Validate PIN length (max 8 characters)
    val validatedPinLength = if (pinLength > 8) 8 else if (pinLength < 4) 4 else pinLength

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val pinCodes = remember { mutableStateListOf<Char?>().apply {
        repeat(validatedPinLength) { add(null) }
    }}

    val focusRequesters = remember { List(validatedPinLength) { FocusRequester() } }
    var targetFocusIndex by remember { mutableIntStateOf(0) }
    var isProcessingInput by remember { mutableStateOf(false) }

    // Clear PIN when requested
    LaunchedEffect(clearPin) {
        if (clearPin) {
            repeat(validatedPinLength) { pinCodes[it] = null }
            targetFocusIndex = 0
            onClearComplete()
        }
    }

    // Calculate the correct focus index - last filled item or first empty
    val correctFocusIndex = remember(pinCodes.toList()) {
        val lastFilledIndex = pinCodes.indexOfLast { it != null }
        if (lastFilledIndex == -1) 0 else lastFilledIndex
    }

    // Update target focus when pin changes
    LaunchedEffect(correctFocusIndex) {
        if (!isProcessingInput) {
            targetFocusIndex = correctFocusIndex
        }
    }

    // Auto-focus management with safety checks
    LaunchedEffect(targetFocusIndex) {
        if (targetFocusIndex in 0 until validatedPinLength && isEnabled) {
            try {
                focusRequesters[targetFocusIndex].requestFocus()
                keyboardController?.show()
            } catch (e: Exception) {
                // Ignore focus errors
            }
        }
    }

    // Initial focus
    LaunchedEffect(Unit) {
        if (isEnabled) {
            try {
                focusRequesters[0].requestFocus()
                keyboardController?.show()
            } catch (e: Exception) {
                // Ignore initial focus errors
            }
        }
    }

    // Notify pin change
    LaunchedEffect(pinCodes.toList()) {
        val currentPin = pinCodes.joinToString("") { it?.toString() ?: "" }
        onPinChange(currentPin)
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(style.spacing, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        repeat(validatedPinLength) { index ->
            val char = pinCodes[index]
            val interactionSource = remember { MutableInteractionSource() }
            val isFocused by interactionSource.collectIsFocusedAsState()

            PinInputField(
                value = char?.toString() ?: "",
                onValueChange = { newValue ->
                    if (!isEnabled) return@PinInputField

                    isProcessingInput = true

                    when {
                        // Handle paste - improved logic
                        newValue.length > 1 -> {
                            val digits = newValue.filter { it.isDigit() }

                            // Clear all fields first
                            repeat(validatedPinLength) { pinCodes[it] = null }

                            // Fill from the beginning
                            digits.take(validatedPinLength).forEachIndexed { i, digit ->
                                pinCodes[i] = digit
                            }

                            // Check if PIN is complete
                            val pin = pinCodes.joinToString("") { it?.toString() ?: "" }
                            if (pin.length == validatedPinLength) {
                                onPinComplete(pin)
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            } else {
                                // Focus on next empty field
                                val nextEmpty = pinCodes.indexOfFirst { it == null }
                                if (nextEmpty != -1) {
                                    targetFocusIndex = nextEmpty
                                }
                            }
                        }

                        // Single digit entered
                        newValue.isNotEmpty() -> {
                            val lastChar = newValue.last()
                            if (lastChar.isDigit()) {
                                pinCodes[index] = lastChar

                                // Check if PIN is complete
                                val pin = pinCodes.joinToString("") { it?.toString() ?: "" }
                                if (pin.length == validatedPinLength) {
                                    onPinComplete(pin)
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                } else {
                                    // Move to next empty field if available
                                    val nextEmpty = pinCodes.indexOfFirst { it == null }
                                    if (nextEmpty != -1) {
                                        targetFocusIndex = nextEmpty
                                    }
                                }
                            }
                        }

                        // Backspace - clear current field
                        newValue.isEmpty() -> {
                            pinCodes[index] = null
                        }
                    }

                    isProcessingInput = false
                },
                onFocusChange = { focusState ->
                    // When any field gets focus, redirect to last filled item
                    if (focusState.isFocused && index != correctFocusIndex && !isProcessingInput) {
                        targetFocusIndex = correctFocusIndex
                    }
                },
                onKeyEvent = { event ->
                    if (!isEnabled) return@PinInputField false

                    // Handle backspace
                    if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DEL) {
                        if (char == null && index > 0) {
                            // Clear previous field and focus it
                            pinCodes[index - 1] = null
                            val newTarget = pinCodes.indexOfLast { it != null }
                            targetFocusIndex = if (newTarget == -1) 0 else newTarget
                            true
                        } else {
                            false
                        }
                    } else {
                        false
                    }
                },
                isFocused = isFocused,
                isError = isError,
                isEnabled = isEnabled,
                style = style,
                interactionSource = interactionSource,
                focusRequester = focusRequesters[index],
                isLast = index == validatedPinLength - 1
            )
        }
    }
}

@Composable
private fun PinInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onFocusChange: (androidx.compose.ui.focus.FocusState) -> Unit,
    onKeyEvent: (androidx.compose.ui.input.key.KeyEvent) -> Boolean,
    isFocused: Boolean,
    isError: Boolean,
    isEnabled: Boolean,
    style: PinInputStyle,
    interactionSource: MutableInteractionSource,
    focusRequester: FocusRequester,
    isLast: Boolean
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = if (isLast) ImeAction.Done else ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = {
 },
            onDone = {
          //      LocalFocusManager.current.clearFocus()
         //       LocalSoftwareKeyboardController.current?.hide()
            }
        ),
        textStyle = style.textStyle,
        singleLine = true,
        maxLines = 1,
        enabled = isEnabled,
        cursorBrush = SolidColor(Color.Transparent),
        visualTransformation = PinTransformation,
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(style.fieldSize)
                    .clip(style.shape)
                    .border(
                        width = style.borderWidth,
                        color = when {
                            isError -> style.errorColor
                            isFocused -> style.focusedBorderColor
                            else -> style.borderColor
                        },
                        shape = style.shape
                    )
                    .background(
                        color = if (isEnabled) style.backgroundColor else style.disabledBackgroundColor,
                        shape = style.shape
                    )
                    .drawBehind {
                        if (isFocused && !isError) {
                            val linePosition = size.height - style.focusedLineThickness.toPx()
                            drawLine(
                                color = style.focusedBorderColor,
                                start = Offset(0f, linePosition),
                                end = Offset(size.width, linePosition),
                                strokeWidth = style.focusedLineThickness.toPx()
                            )
                        }
                        if (isError) {
                            val linePosition = size.height - style.errorLineThickness.toPx()
                            drawLine(
                                color = style.errorColor,
                                start = Offset(0f, linePosition),
                                end = Offset(size.width, linePosition),
                                strokeWidth = style.errorLineThickness.toPx()
                            )
                        }
                    }
                    .clickable(enabled = isEnabled) {
                        focusRequester.requestFocus()
                    }
            ) {
                innerTextField()
            }
        },
        modifier = Modifier
            .focusRequester(focusRequester)
            .onFocusChanged(onFocusChange)
            .onKeyEvent(onKeyEvent)
    )
}

data class PinInputStyle(
    val fieldSize: androidx.compose.ui.unit.DpSize = androidx.compose.ui.unit.DpSize(40.dp, 48.dp),
    val shape: RoundedCornerShape = RoundedCornerShape(4.dp),
    val spacing: androidx.compose.ui.unit.Dp = 12.dp,
    val borderWidth: androidx.compose.ui.unit.Dp = 1.dp,
    val borderColor: Color = Color(0xFF828282),
    val focusedBorderColor: Color = Color(0xFF007548),
    val errorColor: Color = Color(0xFFD40000),
    val backgroundColor: Color = Color.Transparent,
    val disabledBackgroundColor: Color = Color(0xFFF5F5F5),
    val textStyle: TextStyle = TextStyle(
        fontSize = 24.sp,
        textAlign = TextAlign.Center,
        color = Color.Black
    ),
    val focusedLineThickness: androidx.compose.ui.unit.Dp = 2.dp,
    val errorLineThickness: androidx.compose.ui.unit.Dp = 2.dp
) {
    companion object {
        @Composable
        fun default() = PinInputStyle(
            backgroundColor = MaterialTheme.colorScheme.surface,
            textStyle = TextStyle(
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

/*@Composable
fun PinInputExample() {
    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isComplete by remember { mutableStateOf(false) }
    var clearPin by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enter PIN",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(32.dp))

        PinInput(
            pinLength = 6,
            onPinComplete = { completedPin ->
                pin = completedPin
                isComplete = true
                // Validate PIN here
                if (completedPin != "123456") {
                    isError = true
                }
            },
            onPinChange = { currentPin ->
                pin = currentPin
                isError = false
                isComplete = false
            },
            isError = isError,
            clearPin = clearPin,
            onClearComplete = {
                clearPin = false
                isError = false
                isComplete = false
                pin = ""
            },
            style = PinInputStyle.default().copy(
                fieldSize = androidx.compose.ui.unit.DpSize(45.dp, 52.dp),
                spacing = 16.dp,
                shape = RoundedCornerShape(8.dp)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when {
                isComplete && !isError -> "PIN accepted!"
                isError -> "Invalid PIN"
                else -> "Current PIN: $pin"
            },
            color = when {
                isComplete && !isError -> Color.Green
                isError -> Color.Red
                else -> MaterialTheme.colorScheme.onSurface
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { clearPin = true },
            enabled = pin.isNotEmpty()
        ) {
            Text("Clear PIN")
        }
    }
}*/
