package xyz.naotiki_apps.compose_kakeibo

import android.icu.text.BreakIterator
import androidx.compose.runtime.snapshots.SnapshotStateList

// Util class for emojis (or surrogate pair)
class EmojiUtil private constructor() {
    private val breakIterator: BreakIterator = BreakIterator.getCharacterInstance()
    /**
     * This func extract first character from strings if include emojis or not
     * @throws StringIndexOutOfBoundsException If strings is empty. Please check by [String.isNotEmpty] before call this
     * */
    fun firstCharacter(string: String): String {
        breakIterator.setText(string)
        val start = breakIterator.first()
        val end = breakIterator.next()
       return string.substring(start, end)
    }
    /**
     * This func count how many character include if include emojis or not
     * */
    fun characterCount(string: String): Int {
        breakIterator.setText(string)
        var count=0
        while (breakIterator.next()!=BreakIterator.DONE) count++
        return count
    }

    //Singleton
    companion object {
        @Volatile
        private var INSTANCE: EmojiUtil? = null
        fun getInstance() = INSTANCE ?: synchronized(this) {
            INSTANCE = EmojiUtil()
            INSTANCE!!
        }
    }
}

//Setのようにあつかいたーい
fun <T> SnapshotStateList<T>.addAsSet(values: Iterable<T>) = this.addAll(values.filter { !this.contains(it) })
fun <T> SnapshotStateList<T>.addAsSet(vararg values: T) = this.addAsSet(values.toList())


