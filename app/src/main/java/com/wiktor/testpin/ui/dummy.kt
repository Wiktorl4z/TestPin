package com.wiktor.testpin.ui

private fun handleValueChange(
    newValue: String,
    index: Int,
    pinValues: List<Char?>,
    pinLength: Int,
    onPinChange: (List<Char?>) -> Unit,
    onFocusChange: (Int) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    when {
        newValue.isEmpty() -> handleDeletion(index, pinValues, onPinChange, onFocusChange, pinLength)
        else -> {
            // Filter out non-digit characters from all inputs
            val digits = newValue.filter { it.isDigit() }
            if (digits.isEmpty()) return

            when {
                // Single digit input
                digits.length == 1 -> handleSingleDigit(digits[0], index, pinValues, onPinChange, onFocusChange, pinLength)

                // Pasting multiple digits
                else -> handlePaste(digits, pinValues, onPinChange, onFocusChange, pinLength)
            }
        }
    }
}

private fun handlePaste(
    digits: String,
    pinValues: List<Char?>,
    onPinChange: (List<Char?>) -> Unit,
    onFocusChange: (Int) -> Unit,
    pinLength: Int
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
    onFocusChange(newPin.indexOfFirst { it == null } ?: pinLength - 1)
}


private fun handleSingleDigit(
    digit: Char,
    index: Int,
    pinValues: List<Char?>,
    onPinChange: (List<Char?>) -> Unit,
    onFocusChange: (Int) -> Unit,
    pinLength: Int
) {
    val newPin = pinValues.toMutableList().apply {
        this[index] = digit
    }
    onPinChange(newPin)

    // Move to next field if available
    if (index < pinLength - 1) {
        onFocusChange(index + 1)
    } else {
        focusManager.clearFocus(true)
    }
}

private fun handleDeletion(
    index: Int,
    pinValues: List<Char?>,
    onPinChange: (List<Char?>) -> Unit,
    onFocusChange: (Int) -> Unit,
    pinLength: Int
) {
    val newPin = pinValues.toMutableList().apply {
        this[index] = null
    }
    onPinChange(newPin)

    // Move to previous field if available
    if (index > 0) {
        onFocusChange(index - 1)
    }
}