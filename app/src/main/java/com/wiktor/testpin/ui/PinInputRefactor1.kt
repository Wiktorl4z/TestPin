import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Constants
private const val DEFAULT_PIN_LENGTH = 6
private const val CORRECT_PIN = "123456"
private val BORDER_COLOR = Color(0xFF828282)
private val FOCUSED_COLOR = Color(0xFF007548)
private val ERROR_COLOR = Color(0xFFD40000)
private val ITEM_WIDTH = 36.dp
private val ITEM_HEIGHT = 44.dp

@Composable
fun PinScreenRef() {
    val pinValues = remember {
        mutableStateListOf<Char?>().apply {
            repeat(DEFAULT_PIN_LENGTH) { add(null) }
        }
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PinField(
            pinValues = pinValues,
            onPinChange = { newValues ->
                pinValues.clear()
                pinValues.addAll(newValues)
                errorMessage = null
            },
            pinLength = DEFAULT_PIN_LENGTH,
            isError = errorMessage != null
        )

        Spacer(modifier = Modifier.height(16.dp))

        ErrorMessage(errorMessage = errorMessage)

        Spacer(modifier = Modifier.height(24.dp))

        SubmitButton(
            pinValues = pinValues,
            onValidation = { pinCode ->
                errorMessage = validatePin(pinCode, pinValues)
            }
        )
    }
}

@Composable
private fun ErrorMessage(errorMessage: String?) {
    errorMessage?.let {
        Text(
            text = it,
            color = ERROR_COLOR,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun SubmitButton(
    pinValues: List<Char?>,
    onValidation: (String) -> Unit
) {
    Button(
        onClick = {
            val pinCode = pinValues.joinToString("")
            onValidation(pinCode)
        }
    ) {
        Text("Submit")
    }
}

private fun validatePin(pinCode: String, pinValues: List<Char?>): String? {
    return when {
        pinValues.any { it == null } -> "All fields must be filled"
        pinCode != CORRECT_PIN -> "Pin is incorrect"
        else -> null
    }
}

@Composable
fun PinField(
    pinValues: List<Char?>,
    onPinChange: (List<Char?>) -> Unit,
    pinLength: Int,
    isError: Boolean = false,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequesters = remember { List(pinLength) { FocusRequester() } }
    var currentFocusIndex by remember { mutableIntStateOf(0) }
    var isKeyboardVisible by remember { mutableStateOf(false) }

    // Ensure pinValues has the correct length
    val firstEmptyIndex by remember(pinValues) {
        derivedStateOf {
            pinValues.indexOfFirst { it == null }.takeIf { it != -1 } ?: pinLength
        }
    }

    // Find the last filled index
    val lastFilledIndex by remember(pinValues) {
        derivedStateOf {
            pinValues.indexOfLast { it != null }.takeIf { it != -1 } ?: 0
        }
    }

    LaunchedEffect(currentFocusIndex) {
        focusRequesters.getOrNull(currentFocusIndex)?.requestFocus()
        if (isKeyboardVisible) {
            keyboardController?.show()
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        repeat(pinLength) { index ->
            PinInputField(
                index = index,
                pinValues = pinValues,
                onPinChange = onPinChange,
                focusRequesters = focusRequesters,
                currentFocusIndex = currentFocusIndex,
                onFocusChange = { currentFocusIndex = it },
                firstEmptyIndex = firstEmptyIndex,
                lastFilledIndex = lastFilledIndex,
                pinLength = pinLength,
                isError = isError,
                focusManager = focusManager,
                onKeyboardVisibilityChange = { isKeyboardVisible = it }
            )
        }
    }
}

@Composable
private fun PinInputField(
    index: Int,
    pinValues: List<Char?>,
    onPinChange: (List<Char?>) -> Unit,
    focusRequesters: List<FocusRequester>,
    currentFocusIndex: Int,
    onFocusChange: (Int) -> Unit,
    firstEmptyIndex: Int,
    lastFilledIndex: Int,
    pinLength: Int,
    isError: Boolean,
    focusManager: androidx.compose.ui.focus.FocusManager,
    onKeyboardVisibilityChange: (Boolean) -> Unit
) {
    val char = pinValues.getOrNull(index)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val isFocusable = if (firstEmptyIndex < pinLength) {
        index == firstEmptyIndex
    } else {
        // remove focus from all fields if all are filled
        index == pinLength - 1
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(ITEM_WIDTH)
            .height(ITEM_HEIGHT)
            .clip(RoundedCornerShape(2.dp))
            .border(1.dp, BORDER_COLOR, RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(2.dp))
            .drawBehind {
                if (isFocused || isError) {
                    val color = if (isError) ERROR_COLOR else FOCUSED_COLOR
                    val linePosition = size.height - 1.5.dp.toPx()
                    drawLine(
                        color = color,
                        start = Offset(0f, linePosition),
                        end = Offset(size.width, linePosition),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                val targetIndex = if (lastFilledIndex < pinLength - 1) {
                    lastFilledIndex
                } else {
                    pinLength - 1
                }
                focusRequesters[targetIndex].requestFocus()
                onKeyboardVisibilityChange(true)
                if (currentFocusIndex != targetIndex) {
                    onFocusChange(targetIndex)
                }
            }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = char?.toString() ?: "",
                onValueChange = { newValue ->
                    handleValueChange(
                        newValue = newValue,
                        index = index,
                        pinValues = pinValues,
                        pinLength = pinLength,
                        onPinChange = onPinChange,
                        onFocusChange = onFocusChange,
                        focusManager = focusManager,
                        onKeyboardVisibilityChange = onKeyboardVisibilityChange,
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = if (index == pinLength - 1) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        if (index < pinLength - 1) {
                            onFocusChange(index + 1)
                            onKeyboardVisibilityChange(true)
                        }
                    },
                    onDone = {
                        focusManager.clearFocus(true)
                        onKeyboardVisibilityChange(false)
                    }
                ),
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                maxLines = 1,
                cursorBrush = SolidColor(Color.Transparent),
                visualTransformation = PinTransformation,
                interactionSource = interactionSource,
                enabled = isFocusable,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .focusRequester(focusRequesters[index])
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
                            pinValues = pinValues,
                            onPinChange = onPinChange,
                            onFocusChange = onFocusChange
                        )
                    }
            )
        }
    }
}

private fun handleValueChange(
    newValue: String,
    index: Int,
    pinValues: List<Char?>,
    pinLength: Int,
    onPinChange: (List<Char?>) -> Unit,
    onFocusChange: (Int) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager,
    onKeyboardVisibilityChange: (Boolean) -> Unit
) {
    when {
        newValue.length == pinLength -> {
            // Pasting full code
            val newPin = newValue.map { it }
            onPinChange(newPin)
            focusManager.clearFocus(true)
            onKeyboardVisibilityChange(false)
        }

        newValue.isNotEmpty() -> {
            // Add new character
            val newPin = pinValues.toMutableList().apply {
                this[index] = newValue.last()
            }
            onPinChange(newPin)

            // Move to next field if available
            if (index < pinLength - 1) {
                onFocusChange(index + 1)
            } else {
                // Last field filled - hide keyboard
                focusManager.clearFocus(true)
                onKeyboardVisibilityChange(false)
            }
        }

        newValue.isEmpty() -> {
            // Remove current character
            val newPin = pinValues.toMutableList().apply {
                this[index] = null
            }
            onPinChange(newPin)

            // Move to previous field if available
            if (index != pinLength - 1 && index > 0) {
                onFocusChange(index - 1)
            }
        }
    }
}

private fun handleKeyEvent(
    event: androidx.compose.ui.input.key.KeyEvent,
    index: Int,
    char: Char?,
    pinValues: List<Char?>,
    onPinChange: (List<Char?>) -> Unit,
    onFocusChange: (Int) -> Unit
): Boolean {
    return if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DEL) {
        if (char == null && index > 0) {
            // Delete in empty field - clear previous
            val newPin = pinValues.toMutableList().apply {
                this[index - 1] = null
            }
            onPinChange(newPin)
            onFocusChange(index - 1)
            true
        } else false
    } else false
}

