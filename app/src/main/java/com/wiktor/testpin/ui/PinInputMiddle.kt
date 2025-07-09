import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

@Composable
fun PinInputMiddle(
    pinLength: Int,
    onPinComplete: (String) -> Unit,
    isError: Boolean = false
) {
    // Validate PIN length (max 6 characters)
    val validatedPinLength = if (pinLength > 6) 6 else pinLength

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val pinCodes = remember { mutableStateListOf<Char?>().apply {
        repeat(validatedPinLength) { add(null) }
    }}

    val focusRequesters = remember { List(validatedPinLength) { FocusRequester() } }
    var currentFocusIndex by remember { mutableIntStateOf(0) }

    // Calculate first empty index
    val firstEmptyIndex by remember {
        derivedStateOf {
            pinCodes.indexOfFirst { it == null }.takeIf { it != -1 } ?: validatedPinLength
        }
    }

    // Focus should always be on the first empty field or last field
    LaunchedEffect(firstEmptyIndex) {
        val targetIndex = if (firstEmptyIndex < validatedPinLength) {
            firstEmptyIndex
        } else {
            validatedPinLength - 1
        }

        if (currentFocusIndex != targetIndex) {
            currentFocusIndex = targetIndex
        }

        focusRequesters[targetIndex].requestFocus()
        keyboardController?.show()
    }

    // Handle focus changes
    LaunchedEffect(currentFocusIndex) {
        focusRequesters[currentFocusIndex].requestFocus()
        keyboardController?.show()
    }

    // Colors
    val borderColor = Color(0xFF828282) // #828282
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface
    val focusedColor = Color(0xFF007548) // #007548
    val errorColor = Color(0xFFD40000)   // #D40000

    // Fixed element sizes
    val itemWidth = 36.dp
    val itemHeight = 44.dp

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        repeat(validatedPinLength) { index ->
            val char = pinCodes[index]
            val interactionSource = remember { MutableInteractionSource() }
            val isFocused by interactionSource.collectIsFocusedAsState()

            // Only allow focus on the first empty field or last field when all filled
            val isFocusable = index == currentFocusIndex

            BasicTextField(
                value = char?.toString() ?: "",
                onValueChange = { newValue ->
                    when {
                        // Paste full code
                        newValue.length == validatedPinLength -> {
                            newValue.forEachIndexed { i, c ->
                                if (i < validatedPinLength) pinCodes[i] = c
                            }
                            onPinComplete(newValue)
                            // Hide keyboard after last field is filled
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }

                        // Digit entered
                        newValue.isNotEmpty() -> {
                            pinCodes[index] = newValue.last()

                            if (index < validatedPinLength - 1) {
                                // Move to next field
                                currentFocusIndex = index + 1
                            } else {
                                // Last field filled - complete and hide keyboard
                                onPinComplete(pinCodes.joinToString(""))
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        }

                        // Backspace pressed
                        newValue.isEmpty() -> {
                            if (index > 0) {
                                // Clear current field and move to previous
                                pinCodes[index] = null
                                currentFocusIndex = index - 1
                            } else {
                                // First field - just clear it
                                pinCodes[index] = null
                            }
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = if (index == validatedPinLength - 1) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        if (index < validatedPinLength - 1) {
                            currentFocusIndex = index + 1
                        }
                    },
                    onDone = {
                        if (pinCodes.all { it != null }) {
                            onPinComplete(pinCodes.joinToString(""))
                        }
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                ),
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    color = onSurfaceColor
                ),
                singleLine = true,
                maxLines = 1,
                cursorBrush = SolidColor(Color.Transparent),
                visualTransformation = PinTransformation,
                interactionSource = interactionSource,
                enabled = isFocusable,
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(itemWidth)
                            .height(itemHeight)
                            .clip(RoundedCornerShape(2.dp))
                            .border(1.dp, borderColor, RoundedCornerShape(2.dp))
                            .background(surfaceColor, RoundedCornerShape(2.dp))
                            .drawBehind {
                                if (isFocused || isError) {
                                    val color = if (isError) errorColor else focusedColor
                                    val linePosition = size.height - 1.5.dp.toPx()
                                    drawLine(
                                        color = color,
                                        start = Offset(0f, linePosition),
                                        end = Offset(size.width, linePosition),
                                        strokeWidth = 3.dp.toPx()
                                    )
                                }
                            }
                            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                                // When any field is clicked, focus on the first empty or last field
                                val targetIndex = if (firstEmptyIndex < validatedPinLength) {
                                    firstEmptyIndex
                                } else {
                                    validatedPinLength - 1
                                }

                                if (currentFocusIndex != targetIndex) {
                                    currentFocusIndex = targetIndex
                                } else {
                                    // If already focused, show keyboard again
                                    keyboardController?.show()
                                }
                            }
                    ) {
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .focusRequester(focusRequesters[index])
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            currentFocusIndex = index
                        }
                    }
                    .onKeyEvent { event ->
                        // Handle backspace
                        if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DEL) {
                            if (char == null && index > 0) {
                                // Move to previous field and clear it
                                pinCodes[index - 1] = null
                                currentFocusIndex = index - 1
                                true
                            } else {
                                false
                            }
                        } else {
                            false
                        }
                    }
            )
        }
    }
}