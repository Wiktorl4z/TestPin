package com.wiktor.testpin.ui

import android.text.Spanned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight

private fun convertSpannedToAnnotatedString(spanned: Spanned): AnnotatedString {
    return AnnotatedString.Builder().apply {
        append(spanned.toString())

        spanned.getSpans(0, spanned.length, Any::class.java).forEach { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)

            if (span is android.text.style.StyleSpan && span.style == android.graphics.Typeface.BOLD) {
                addStyle(
                    SpanStyle(fontWeight = FontWeight.Bold),
                    start = start,
                    end = end
                )
            }
        }
    }.toAnnotatedString()
}