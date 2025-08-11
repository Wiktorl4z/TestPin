import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class PinState(
    private val length: Int,
    private val onChange: (String) -> Unit
) {
    private val currentValues = MutableList<String?>(length) { null }
    private val isEmpty = MutableList(length) { true }

    fun setValue(index: Int, value: String?) {
        if (index in 0 until length) {
            currentValues[index] = value
            isEmpty[index] = value.isNullOrEmpty()
            println("PinState.setValue() - index: $index, value: $value, isEmpty: ${isEmpty[index]}")
            notifyChange()
        } else {
            println("PinState.setValue() - invalid index: $index")
        }
    }

    fun getTargetIndex(): Int {
        val firstEmpty = isEmpty.indexOfFirst { it }
        println("PinState.getTargetIndex() - isEmpty=$isEmpty, firstEmpty=$firstEmpty")
        return if (firstEmpty != -1) firstEmpty else (length - 1)
    }

    fun getValues(): List<String?> = currentValues.toList()

    private fun notifyChange() {
        val current = getValues()
        println("PinState.notifyChange() - current values: $current")
        onChange(current.filterNotNull().joinToString(separator = ""))
    }
}

@Composable
fun PinFieldWithErrorMessage3(
    length: Int = 4,
    pinState: PinState,
    modifier: Modifier = Modifier,
    errorMessage: String? = null
) {
    val focusRequesters = remember {
        List(length) { FocusRequester() }
    }

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (index in 0 until length) {
            var text by remember { mutableStateOf("") }

            BasicTextField(
                value = text,
                onValueChange = { newValue ->
                    if (newValue.length <= 1) {
                        text = newValue
                        pinState.setValue(index, newValue)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .border(
                        width = 1.dp,
                        color = if (errorMessage != null) Color.Red else Color.Gray,
                        shape = MaterialTheme.shapes.small
                    )
                    .background(Color.White)
                    .focusRequester(focusRequesters[index])
                    .focusable(),
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    color = Color.Black
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                visualTransformation = VisualTransformation.None
            )
        }
    }

    // Request focus on the correct index when needed
    LaunchedEffect(key1 = pinState) {
        try {
            val targetIndex = pinState.getTargetIndex()
            if (targetIndex in focusRequesters.indices) {
                focusRequesters[targetIndex].requestFocus()
            } else {
                println("Invalid targetIndex for focusRequesters: $targetIndex")
            }
        } catch (ex: Exception) {
            println("Exception requesting focus: ${ex.message}")
            ex.printStackTrace()
        }
    }

    // Show error message if present
    if (!errorMessage.isNullOrEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorMessage,
            color = Color.Red,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
