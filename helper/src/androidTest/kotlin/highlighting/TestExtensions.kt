package highlighting

import android.graphics.Color
import android.graphics.Typeface
import android.text.ParcelableSpan
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.text.buildSpannedString
import androidx.core.text.getSpans
import androidx.core.text.inSpans
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.algolia.instantsearch.core.highlighting.HighlightTokenizer
import com.algolia.instantsearch.helper.android.highlighting.toSpannedString
import org.junit.Test
import org.junit.runner.RunWith
import shouldEqual


@SmallTest
@RunWith(AndroidJUnit4::class)
class TestExtensions {

    private val tokenizer = HighlightTokenizer("[", "]")
    private val highlightStrings = listOf("foo[ba]r", "foo[ba]r[ba]z")
    private val highlights = highlightStrings.map(tokenizer)
    private val defaultSpan = StyleSpan(Typeface.BOLD)
    private val customSpan = ForegroundColorSpan(Color.RED)

    private fun expectedSpannedStrings(span: ParcelableSpan = defaultSpan): List<SpannedString> {
        return listOf(
            buildSpannedString {
                append("foo")
                inSpans(span) { append("ba") }
                append("r")
            }, buildSpannedString {
                append("foo")
                inSpans(span) { append("ba") }
                append("r")
                inSpans(span) { append("ba") }
                append("z")
            }
        )
    }

    @Test
    fun stringToSpannedString() = stringToCustomSpannedString(defaultSpan)

    @Test
    fun stringToCustomSpannedString() = stringToCustomSpannedString(customSpan)

    @Test
    fun listToSpannedString() = listToCustomSpannedString(defaultSpan)

    @Test
    fun listToCustomSpannedString() = listToCustomSpannedString(customSpan)

    private fun stringToCustomSpannedString(customSpan: ParcelableSpan) {
        highlights.forEachIndexed { index, it ->
            val tested = it.toSpannedString(customSpan)
            val expected = expectedSpannedStrings(customSpan)[index]
            val max = tested.length

            tested.toString() shouldEqual expected.toString()

            var i = 0
            while (i < max) {
                val expectTransitionAt = expected.nextSpanTransition(i, max, Any::class.java)
                tested.nextSpanTransition(i, max, Any::class.java) shouldEqual expectTransitionAt
                i = expectTransitionAt
            }
        }
    }

    private fun listToCustomSpannedString(customSpan: ParcelableSpan) {
        val tested = highlights.toSpannedString(customSpan)
        val expectedSpannedStrings = expectedSpannedStrings(customSpan)

        tested.toString() shouldEqual expectedSpannedStrings.joinToString() // Built strings are the same
        tested.getSpans<Any>().size shouldEqual 1                           // and tested does still have its span
    }

    private inline fun <reified T : Any> SpannedString.getSpans(): Array<out T> {
        return getSpans(0, length)
    }
}