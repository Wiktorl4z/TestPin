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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Styling configuration class
data class PinFieldStyle(
    val itemWidth: Dp,
    val itemHeight: Dp,
    val spacing: Dp,
    val cornerRadius: Dp,
    val borderColor: Color,
    val focusedColor: Color,
    val errorColor: Color,
    val textStyle: TextStyle,
    val errorTextStyle: TextStyle,
    val backgroundColor: Color,
)

// Composable function to create default style
@Composable
fun defaultPinFieldStyle(): PinFieldStyle {
    return PinFieldStyle(
        itemWidth = 36.dp,
        itemHeight = 44.dp,
        spacing = 16.dp,
        cornerRadius = 2.dp,
        borderColor = Color(0xFF828282),
        focusedColor = Color(0xFF007548),
        errorColor = Color(0xFFD40000),
        textStyle = TextStyle(
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        ),
        errorTextStyle = MaterialTheme.typography.bodySmall,
        backgroundColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun PinFieldWithErrorMessage(
    pinLength: Int = 6,
    errorMessage: String?,
    onPinChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    style: PinFieldStyle = defaultPinFieldStyle(),
) {
    // Internal state for PIN values
    val pinValues = remember {
        mutableStateListOf<Char?>().apply {
            repeat(pinLength) { add(null) }
        }
    }

    // Calculate total width for PIN fields
    val totalWidth = remember(pinLength, style) {
        (style.itemWidth * pinLength) + (style.spacing * (pinLength - 1))
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Container for PIN fields
        Box(
            modifier = Modifier
                .width(totalWidth)
        ) {
            PinField(
                pinValues = pinValues,
                onPinChange = { newValues ->
                    pinValues.clear()
                    pinValues.addAll(newValues)
                    onPinChange(pinValues.joinToString("") { it?.toString() ?: "" })
                },
                pinLength = pinLength,
                isError = errorMessage != null,
                shouldRefocus = errorMessage != null,
                style = style
            )
        }

        Spacer(Modifier.height(16.dp))

        // Error message container
        Box(
            modifier = Modifier
                .width(totalWidth)
                .padding(top = 8.dp)
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = style.errorColor,
                    style = style.errorTextStyle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterStart)
                )
            }
        }
    }
}

@Composable
private fun PinField(
    pinValues: List<Char?>,
    onPinChange: (List<Char?>) -> Unit,
    pinLength: Int,
    isError: Boolean = false,
    shouldRefocus: Boolean = false,
    style: PinFieldStyle,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequesters = remember { List(pinLength) { FocusRequester() } }
    var currentFocusIndex by remember { mutableIntStateOf(0) }

    val firstEmptyIndex by remember(pinValues) {
        derivedStateOf {
            pinValues.indexOfFirst { it == null }.takeIf { it != -1 } ?: pinLength
        }
    }

    val lastFilledIndex by remember(pinValues) {
        derivedStateOf {
            pinValues.indexOfLast { it != null }.takeIf { it != -1 } ?: 0
        }
    }

    // Handle refocus when error occurs
    LaunchedEffect(shouldRefocus) {
        if (shouldRefocus) {
            println("XXX Refocusing PIN input fields")
            val targetIndex = if (firstEmptyIndex < pinLength) firstEmptyIndex else pinLength - 1
            currentFocusIndex = targetIndex
            focusRequesters[targetIndex].requestFocus()
            keyboardController?.show()
        }
    }

    LaunchedEffect(currentFocusIndex) {
        println("XXX  LaunchedEffect(currentFocusIndex) ${currentFocusIndex}")
        focusRequesters.getOrNull(currentFocusIndex)?.requestFocus()
    }

    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(pinLength) { index ->
            PinInputField(
                index = index,
                pinValues = pinValues,
                onPinChange = onPinChange,
                focusRequesters = focusRequesters,
                currentFocusIndex = currentFocusIndex,
                onFocusChange = {
                    println("XXX onFocusChange: $it")
                    currentFocusIndex = it
                },
                firstEmptyIndex = firstEmptyIndex,
                lastFilledIndex = lastFilledIndex,
                pinLength = pinLength,
                isError = isError,
                focusManager = focusManager,
                style = style
            )

            if (index < pinLength - 1) {
                Spacer(Modifier.width(style.spacing))
            }
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
    style: PinFieldStyle,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val char = pinValues.getOrNull(index)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val isFocusable = if (firstEmptyIndex < pinLength) {
        index == firstEmptyIndex
    } else {
        index == pinLength - 1
    }

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
                val targetIndex = if (lastFilledIndex < pinLength - 1) {
                    lastFilledIndex
                } else {
                    pinLength - 1
                }
                focusRequesters[targetIndex].requestFocus()
                // 🔥 Explicitly request keyboard even if focus already existed
                keyboardController?.show()
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
                        index = index,
                        pinValues = pinValues,
                        pinLength = pinLength,
                        onPinChange = onPinChange,
                        onFocusChange = onFocusChange,
                        focusManager = focusManager
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
                textStyle = style.textStyle,
                singleLine = true,
                maxLines = 1,
                cursorBrush = SolidColor(Color.Transparent),
                visualTransformation = PinTransformationDone,
                interactionSource = interactionSource,
                enabled = isFocusable,
                modifier = Modifier
                    .fillMaxWidth()
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
    index: Int,
    pinValues: List<Char?>,
    pinLength: Int,
    onPinChange: (List<Char?>) -> Unit,
    onFocusChange: (Int) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager,
) {
    when {
        newValue.isEmpty() -> handleDeletion(
            index,
            pinValues,
            onPinChange,
            onFocusChange,
            pinLength
        )

        else -> {
            // Filter out non-digit characters from all inputs
            val digits = newValue.filter { it.isDigit() }
            if (digits.isEmpty()) return

            when {
                // Single digit input
                digits.length == 1 -> handleSingleDigit(
                    digits[0],
                    index,
                    pinValues,
                    onPinChange,
                    onFocusChange,
                    pinLength,
                    focusManager
                )

                // Pasting multiple digits
                else ->
                    if (index == pinLength - 1 && pinValues.all { it != null }) {
                        // nadpisz ostatnie pole ostatnią cyfrą z newValue
                        val newPin = pinValues.toMutableList().apply {
                            this[index] = digits.last()
                        }
                        onPinChange(newPin)
                        println("XXX Pasting multiple digits, clearing focus")
                        focusManager.clearFocus(true)
                    } else {
                        handlePaste(
                            digits,
                            pinValues,
                            onPinChange,
                            onFocusChange,
                            pinLength,
                            focusManager
                        )
                    }
            }
        }
    }
}

private fun handlePaste(
    digits: String,
    pinValues: List<Char?>,
    onPinChange: (List<Char?>) -> Unit,
    onFocusChange: (Int) -> Unit,
    pinLength: Int,
    focusManager: androidx.compose.ui.focus.FocusManager,
) {
    val newPin = pinValues.toMutableList()
    var digitsUsed = 0
    var fieldIndex = 0

    // Find first empty field
    val startIndex = newPin.indexOfFirst { it == null }

    if (startIndex == -1) return // All fields already filled

    // Fill sequentially from first empty field
    while (fieldIndex < pinLength && digitsUsed < digits.length) {
        if (newPin[fieldIndex] == null) {
            newPin[fieldIndex] = digits[digitsUsed]
            digitsUsed++
        }
        fieldIndex++
    }

    onPinChange(newPin)

    // Move to next field if available
    if (fieldIndex < pinLength - 1) {
        onFocusChange(fieldIndex + 1)
    } else {
        println("XXX handlePaste, clearing focus")
        focusManager.clearFocus(true)
    }

}


private fun handleSingleDigit(
    digit: Char,
    index: Int,
    pinValues: List<Char?>,
    onPinChange: (List<Char?>) -> Unit,
    onFocusChange: (Int) -> Unit,
    pinLength: Int,
    focusManager: androidx.compose.ui.focus.FocusManager,
) {
    val newPin = pinValues.toMutableList().apply {
        this[index] = digit
    }
    onPinChange(newPin)

    // Move to next field if available
    when {
        index < pinLength - 1 -> {
            onFocusChange(index + 1)
        }
        index == pinLength - 1 -> {
            println("XXX handleSingleDigit, clearing focus")
            focusManager.clearFocus(true)
        }
    }
}

private fun handleDeletion(
    index: Int,
    pinValues: List<Char?>,
    onPinChange: (List<Char?>) -> Unit,
    onFocusChange: (Int) -> Unit,
    pinLength: Int,
) {
    println("XXX handleDeletion at index: $index")
    val newPin = pinValues.toMutableList().apply {
        this[index] = null
    }
    onPinChange(newPin)

    // Move to previous field if available
    if (index != pinLength - 1 && index > 0) {
        onFocusChange(index - 1)
    }
}

private fun handleKeyEvent(
    event: androidx.compose.ui.input.key.KeyEvent,
    index: Int,
    char: Char?,
    pinValues: List<Char?>,
    onPinChange: (List<Char?>) -> Unit,
    onFocusChange: (Int) -> Unit,
): Boolean {
    return if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DEL) {
        if (char == null && index > 0) {
            // Delete in empty field - clear previous
            val newPin = pinValues.toMutableList().apply {
                this[index - 1] = null
            }
            println("XXX handleKeyEvent in empty field, moving focus to previous index: ${index - 1}")
            onPinChange(newPin)
            onFocusChange(index - 1)
            true
        } else false
    } else false
}

// VisualTransformation to handle PIN input completion
// This transformation is used to indicate that the PIN input is complete
// handle selector at the end
object PinTransformationDone : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text,
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int) = text.length
                override fun transformedToOriginal(offset: Int) = text.length
            }
        )
    }
}