import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import android.view.KeyEvent as AndroidKeyEvent  // Aliased to avoid conflict
import androidx.compose.ui.input.key.KeyEvent  // Compose KeyEvent
import androidx.compose.ui.input.key.key  // For key comparison


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
fun PinInputTestingRefactor(
    pinLength: Int,
    onPinComplete: (String) -> Unit,
    isError: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Fixed type declaration for pinCodes
    val pinCodes = remember { mutableStateListOf<Char?>().apply { repeat(pinLength) { add(null) } } }

    val focusRequesters = remember { List(pinLength) { FocusRequester() } }
    var currentFocusIndex by remember { mutableIntStateOf(0) }

    val firstEmptyIndex by remember {
        derivedStateOf { pinCodes.indexOfFirst { it == null }.takeIf { it != -1 } ?: pinLength }
    }

    val lastFilledIndex by remember {
        derivedStateOf { pinCodes.indexOfLast { it != null }.takeIf { it != -1 } ?: 0 }
    }

    LaunchedEffect(currentFocusIndex) {
        focusRequesters.getOrNull(currentFocusIndex)?.requestFocus()
        keyboardController?.show()
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(
            PinInputDefaults.ItemSpacing,
            Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        repeat(pinLength) { index ->
            PinInputField(
                value = pinCodes[index],
                isFocused = index == currentFocusIndex,
                isError = isError,
                isFocusable = if (firstEmptyIndex < pinLength) {
                    index == firstEmptyIndex
                } else {
                    index == pinLength - 1
                },
                onValueChange = { newValue ->
                    when {
                        newValue.length == pinLength -> {
                            newValue.forEachIndexed { i, c ->
                                if (i < pinLength) pinCodes[i] = c
                            }
                            onPinComplete(newValue)
                            focusManager.clearFocus(true)
                            keyboardController?.hide()
                        }

                        newValue.isNotEmpty() -> {
                            // Fixed: Explicitly handle Char? assignment
                            pinCodes[index] = newValue.lastOrNull()
                            if (index < pinLength - 1) {
                                currentFocusIndex = index + 1
                            } else {
                                onPinComplete(pinCodes.joinToString("") { it?.toString() ?: "" })
                                focusManager.clearFocus(true)
                                keyboardController?.hide()
                            }
                        }

                        newValue.isEmpty() -> {
                            pinCodes[index] = null
                            if (index > 0) {
                                currentFocusIndex = index - 1
                            }
                        }
                    }
                },
                onFocusRequested = {
                    val targetIndex = if (lastFilledIndex < pinLength - 1) {
                        lastFilledIndex
                    } else {
                        pinLength - 1
                    }
                    currentFocusIndex = targetIndex
                },
                onKeyEvent = { event ->
                    if (event.nativeKeyEvent.keyCode == AndroidKeyEvent.KEYCODE_DEL) {
                        if (pinCodes[index] == null && index > 0) {
                            pinCodes[index - 1] = null
                            currentFocusIndex = index - 1
                            true
                        } else false 1
                    } else false
                },
                keyboardActions = KeyboardActions(
                    onNext = {
                        if (index < pinLength - 1) {
                            currentFocusIndex = index + 1
                        }
                    },
                    onDone = {
                        if (pinCodes.all { it != null }) {
                            onPinComplete(pinCodes.joinToString("") { it.toString() })
                        }
                        focusManager.clearFocus(true)
                        keyboardController?.hide()
                    }
                ),
                focusRequester = focusRequesters[index],
                imeAction = if (index == pinLength - 1) ImeAction.Done else ImeAction.Next
            )
        }
    }
}

@Composable
private fun PinInputField(
    value: Char?,
    isFocused: Boolean,
    isError: Boolean,
    isFocusable: Boolean,
    onValueChange: (String) -> Unit,
    onFocusRequested: () -> Unit,
    onKeyEvent: (KeyEvent) -> Boolean,
    keyboardActions: KeyboardActions,
    focusRequester: FocusRequester,
    imeAction: ImeAction
) {
    val interactionSource = remember { MutableInteractionSource() }
    val actualIsFocused by interactionSource.collectIsFocusedAsState()

    val colors = MaterialTheme.colorScheme
    val borderColor = if (isError) PinInputDefaults.ErrorColor else PinInputDefaults.FocusedColor

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
            .background(colors.surface, RoundedCornerShape(PinInputDefaults.CornerRadius))
            .drawBehind {
                if (actualIsFocused || isError) {
                    val color = if (isError) PinInputDefaults.ErrorColor else borderColor
                    val linePosition = size.height - PinInputDefaults.UnderlineWidth.toPx() / 2
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
                indication = null
            ) { onFocusRequested() }
    ) {
        BasicTextField(
            value = value?.toString() ?: "",
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = imeAction
            ),
            keyboardActions = keyboardActions,
            textStyle = TextStyle(
                fontSize = PinInputDefaults.FontSize,
                textAlign = TextAlign.Center,
                color = colors.onSurface
            ),
            singleLine = true,
            maxLines = 1,
            cursorBrush = SolidColor(Color.Transparent),
            visualTransformation = PinTransformation,
            interactionSource = interactionSource,
            enabled = isFocusable,
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (it.isFocused && !isFocused) {
                        onFocusRequested()
                    }
                }
                .onKeyEvent(onKeyEvent = onKeyEvent)
        )
    }
}