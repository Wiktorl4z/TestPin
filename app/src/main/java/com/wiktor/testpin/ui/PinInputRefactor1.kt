import android.graphics.Rect
import android.view.KeyEvent
import android.view.ViewTreeObserver
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
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

    // Track real keyboard state
    val isKeyboardOpen by rememberKeyboardVisibility()
    var lastKeyboardState by remember { mutableStateOf(isKeyboardOpen) }

    // Calculate target focus index
    val targetIndex by remember(pinValues) {
        derivedStateOf {
            pinValues.indexOfFirst { it == null }.let {
                if (it != -1) it else pinLength - 1
            }
        }
    }

    // Handle refocus when error occurs
    LaunchedEffect(shouldRefocus) {
        if (shouldRefocus) {
            currentFocusIndex = targetIndex
            focusRequesters[targetIndex].requestFocus()
            keyboardController?.show()
            lastKeyboardState = true
        }
    }

    LaunchedEffect(currentFocusIndex) {
        focusRequesters.getOrNull(currentFocusIndex)?.requestFocus()
    }

    // Update lastKeyboardState when keyboard state changes
    LaunchedEffect(isKeyboardOpen) {
        lastKeyboardState = isKeyboardOpen
    }

    // Callbacks for focus and keyboard actions
    val onFocusRequest: (Int) -> Unit = { index ->
        currentFocusIndex = index
        // If keyboard was manually hidden, show it again
        if (!isKeyboardOpen && !lastKeyboardState) {
            keyboardController?.show()
        }
    }

    val onKeyboardAction: (KeyboardAction) -> Unit = { action ->
        when (action) {
            KeyboardAction.Show -> {
                keyboardController?.show()
            }
            KeyboardAction.ClearFocus -> {
                focusManager.clearFocus(true)
                keyboardController?.hide()
            }
        }
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
                onFocusRequest = onFocusRequest,
                onKeyboardAction = onKeyboardAction,
                targetIndex = targetIndex,
                pinLength = pinLength,
                isError = isError,
                style = style
            )

            if (index < pinLength - 1) {
                Spacer(Modifier.width(style.spacing))
            }
        }
    }
}

@Composable
private fun rememberKeyboardVisibility(): State<Boolean> {
    val view = LocalView.current
    val isVisible = remember { mutableStateOf(false) }

    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            isVisible.value = keypadHeight > screenHeight * 0.15
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    return isVisible
}

@Composable
private fun PinInputField(
    index: Int,
    pinValues: List<Char?>,
    onPinChange: (List<Char?>) -> Unit,
    focusRequesters: List<FocusRequester>,
    currentFocusIndex: Int,
    onFocusRequest: (Int) -> Unit,
    onKeyboardAction: (KeyboardAction) -> Unit,
    targetIndex: Int,
    pinLength: Int,
    isError: Boolean,
    style: PinFieldStyle,
) {
    val char = pinValues.getOrNull(index)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Only target index is focusable
    val isFocusable = index == targetIndex

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
                focusRequesters[targetIndex].requestFocus()
                onKeyboardAction(KeyboardAction.Show)
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
                        onFocusRequest = onFocusRequest,
                        onKeyboardAction = onKeyboardAction
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
                            onFocusRequest(index)
                        }
                    }
                    .onKeyEvent { event ->
                        handleKeyEvent(
                            event = event,
                            index = index,
                            char = char,
                            pinValues = pinValues,
                            onPinChange = onPinChange,
                            onFocusRequest = onFocusRequest
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

enum class KeyboardAction {
    Show, ClearFocus
}

private fun handleValueChange(
    newValue: String,
    index: Int,
    pinValues: List<Char?>,
    pinLength: Int,
    onPinChange: (List<Char?>) -> Unit,
    onFocusRequest: (Int) -> Unit,
    onKeyboardAction: (KeyboardAction) -> Unit
) {
    when {
        newValue.isEmpty() -> handleDeletion(
            index,
            pinValues,
            onPinChange,
            onFocusRequest,
            onKeyboardAction
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
                    onFocusRequest,
                    onKeyboardAction,
                    pinLength
                )

                // Pasting multiple digits
                else -> handlePaste(
                    digits,
                    pinValues,
                    onPinChange,
                    onFocusRequest,
                    onKeyboardAction,
                    pinLength
                )
            }
        }
    }
}

private fun handlePaste(
    digits: String,
    pinValues: List<Char?>,
    onPinChange: (List<Char?>) -> Unit,
    onFocusRequest: (Int) -> Unit,
    onKeyboardAction: (KeyboardAction) -> Unit,
    pinLength: Int
) {
    val newPin = pinValues.toMutableList()

    // Count available empty fields
    val emptyFieldCount = newPin.count { it == null }
    if (emptyFieldCount == 0) return // All fields already filled

    // Only take as many digits as we have empty fields
    val digitsToPaste = digits.take(emptyFieldCount)
    var digitsUsed = 0

    // Fill empty fields in order
    for (i in newPin.indices) {
        if (newPin[i] == null) {
            newPin[i] = digitsToPaste[digitsUsed]
            digitsUsed++
            if (digitsUsed >= digitsToPaste.length) break
        }
    }

    onPinChange(newPin)

    // Set focus to next empty field or last field if all filled
    val nextEmptyIndex = newPin.indexOfFirst { it == null }

    // Move to next field if available
    when {
        nextEmptyIndex < pinLength - 1 -> {
            onFocusRequest(nextEmptyIndex + 1)
            onKeyboardAction(KeyboardAction.Show)
        }
        nextEmptyIndex == -1 -> {
            // Last field - clear focus and hide keyboard
            onKeyboardAction(KeyboardAction.ClearFocus)
        }
    }
}

private fun handleSingleDigit(
    digit: Char,
    index: Int,
    pinValues: List<Char?>,
    onPinChange: (List<Char?>) -> Unit,
    onFocusRequest: (Int) -> Unit,
    onKeyboardAction: (KeyboardAction) -> Unit,
    pinLength: Int
) {
    val newPin = pinValues.toMutableList().apply {
        this[index] = digit
    }
    onPinChange(newPin)

    // Move to next field if available
    when {
        index < pinLength - 1 -> {
            onFocusRequest(index + 1)
        }
        index == pinLength - 1 -> {
            // Last field - clear focus and hide keyboard
            onKeyboardAction(KeyboardAction.ClearFocus)
        }
    }
}

private fun handleDeletion(
    index: Int,
    pinValues: List<Char?>,
    onPinChange: (List<Char?>) -> Unit,
    onFocusRequest: (Int) -> Unit,
    onKeyboardAction: (KeyboardAction) -> Unit
) {
    val newPin = pinValues.toMutableList().apply {
        this[index] = null
    }
    onPinChange(newPin)

    // Stay in current field after deletion
    onFocusRequest(index)
 //   onKeyboardAction(KeyboardAction.Show)
}

private fun handleKeyEvent(
    event: androidx.compose.ui.input.key.KeyEvent,
    index: Int,
    char: Char?,
    pinValues: List<Char?>,
    onPinChange: (List<Char?>) -> Unit,
    onFocusRequest: (Int) -> Unit,
): Boolean {
    return if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DEL) {
        if (char != null) {
            // If current field has a value, delete it
            val newPin = pinValues.toMutableList().apply {
                this[index] = null
            }
            onPinChange(newPin)
            onFocusRequest(index)
            true
        } else if (index > 0) {
            // If current field is empty, delete previous field
            val newPin = pinValues.toMutableList().apply {
                this[index - 1] = null
            }
            onPinChange(newPin)
            onFocusRequest(index - 1)
            true
        } else {
            false
        }
    } else false
}

// VisualTransformation to handle PIN input completion
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