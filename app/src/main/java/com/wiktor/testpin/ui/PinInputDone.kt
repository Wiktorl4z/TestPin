/*
import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize

private object PinInputDefaults {
    val BorderColor = Color(0xFF828282)
    val FocusedColor = Color(0xFF007548)
    val ErrorColor = Color(0xFFD40000)
    val ItemWidth = 36.dp
    val ItemHeight = 44.dp
    val ItemSpacing = 16.dp
    val BorderWidth = 1.dp
    val CornerRadius = 2.dp
    val UnderlineWidth = 3.dp
    val FontSize = 24.sp
}

@Composable
fun PinInputDone(
    pinLength: Int,
    onPinComplete: (String) -> Unit,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // State management
    val pinCodes = remember {
        mutableStateListOf<Char?>().apply {
            repeat(pinLength) { add(null) }
        }
    }
    val focusRequesters = remember { List(pinLength) { FocusRequester() } }
    var currentFocusIndex by remember { mutableIntStateOf(0) }

    // Derived states for focus management
    val firstEmptyIndex by remember {
        derivedStateOf {
            pinCodes.indexOfFirst { it == null }.takeIf { it != -1 } ?: pinLength
        }
    }
    val lastFilledIndex by remember {
        derivedStateOf {
            pinCodes.indexOfLast { it != null }.takeIf { it != -1 } ?: 0
        }
    }

    // Auto-focus management
    LaunchedEffect(currentFocusIndex) {
        focusRequesters[currentFocusIndex].requestFocus()
        keyboardController?.show()
    }

    // Helper functions for cleaner code
    fun isPinComplete() = pinCodes.all { it != null }
    fun getCurrentPin() = pinCodes.joinToString("")
    fun hideKeyboardAndClearFocus() {
        focusManager.clearFocus(true)
        keyboardController?.hide()
    }
    fun completePinInput(pin: String) {
        onPinComplete(pin)
        hideKeyboardAndClearFocus()
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(
            PinInputDefaults.ItemSpacing,
            Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        repeat(pinLength) { index ->
            PinInputField(
                index = index,
                pinLength = pinLength,
                char = pinCodes[index],
                isError = isError,
                focusRequester = focusRequesters[index],
                isFocusable = if (firstEmptyIndex < pinLength) {
                    index == firstEmptyIndex
                } else {
                    index == pinLength - 1
                },
                onValueChange = { newValue ->
                    when {
                        newValue.length == pinLength -> {
                            // Handle full PIN paste
                            newValue.forEachIndexed { i, c ->
                                if (i < pinLength) pinCodes[i] = c
                            }
                            completePinInput(newValue)
                        }
                        newValue.isNotEmpty() -> {
                            // Handle single character input
                            pinCodes[index] = newValue.last()
                            if (index < pinLength - 1) {
                                currentFocusIndex = index + 1
                            } else {
                                completePinInput(getCurrentPin())
                            }
                        }
                        newValue.isEmpty() -> {
                            // Handle character deletion
                            pinCodes[index] = null
                            if (index != pinLength - 1 && index > 0) {
                                currentFocusIndex = index - 1
                            }
                        }
                    }
                },
                onFocusChanged = { focusState ->
                    if (focusState.isFocused) {
                        currentFocusIndex = index
                    }
                },
                onKeyEvent = { event ->
                    if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DEL) {
                        if (pinCodes[index] == null && index > 0) {
                            pinCodes[index - 1] = null
                            currentFocusIndex = index - 1
                            true
                        } else false
                    } else false
                },
                onBoxClick = {
                    val targetIndex = if (lastFilledIndex < pinLength - 1) {
                        lastFilledIndex
                    } else {
                        pinLength - 1
                    }
                    focusRequesters[targetIndex].requestFocus()
                    keyboardController?.show()
                    if (currentFocusIndex != targetIndex) {
                        currentFocusIndex = targetIndex
                    }
                },
                onNext = {
                    if (index < pinLength - 1) {
                        currentFocusIndex = index + 1
                    }
                },
                onDone = {
                    if (isPinComplete()) {
                        completePinInput(getCurrentPin())
                    } else {
                        hideKeyboardAndClearFocus()
                    }
                }
            )
        }
    }
}

@Composable
private fun PinInputField(
    index: Int,
    pinLength: Int,
    char: Char?,
    isError: Boolean,
    focusRequester: FocusRequester,
    isFocusable: Boolean,
    onValueChange: (String) -> Unit,
    onFocusChanged: (androidx.compose.ui.focus.FocusState) -> Unit,
    onKeyEvent: (androidx.compose.ui.input.key.KeyEvent) -> Boolean,
    onBoxClick: () -> Unit,
    onNext: () -> Unit,
    onDone: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Theme colors
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(PinInputDefaults.ItemWidth)
            .height(PinInputDefaults.ItemHeight)
            .clip(RoundedCornerShape(PinInputDefaults.CornerRadius))
            .border(
                width = PinInputDefaults.BorderWidth,
                color = PinInputDefaults.BorderColor,
                shape = RoundedCornerShape(PinInputDefaults.CornerRadius)
            )
            .background(
                color = surfaceColor,
                shape = RoundedCornerShape(PinInputDefaults.CornerRadius)
            )
            .drawBehind {
                if (isFocused || isError) {
                    val color = if (isError) {
                        PinInputDefaults.ErrorColor
                    } else {
                        PinInputDefaults.FocusedColor
                    }
                    val linePosition = size.height - 1.5.dp.toPx()
                    drawLine(
                        color = color,
                        start = Offset(0f, linePosition),
                        end = Offset(size.width, linePosition),
                        strokeWidth = PinInputDefaults.UnderlineWidth.toPx()
                    )
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onBoxClick
            )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = char?.toString() ?: "",
                onValueChange = onValueChange,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = if (index == pinLength - 1) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        if (index < pinLength - 1) {
                            currentFocusIndex = index + 1
                        }
                    },
                    onDone = {
                        if (pinCodes.all { it != null }) {
                            onPinComplete(pinCodes.joinToString(""))
                        }
                        focusManager.clearFocus(true)
                        keyboardController?.hide()
                    }
                ),
                textStyle = TextStyle(
                    fontSize = PinInputDefaults.FontSize,
                    textAlign = TextAlign.Center,
                    color = onSurfaceColor
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
                    .focusRequester(focusRequester)
                    .onFocusChanged(onFocusChanged)
                    .onKeyEvent(onKeyEvent)
            )
        }
    }
}

object PinTransformation : VisualTransformation {
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
*/
