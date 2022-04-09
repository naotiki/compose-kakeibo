package xyz.naotiki_apps.compose_kakeibo

import android.icu.text.BreakIterator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.CoroutineScope

//ゆーてぃりてぃーーーーーーーーーたち


// Util class for emojis (or surrogate pair)
//パフォーマンスに懸念あり.要プロファイル
class EmojiUtil private constructor() {
    //Javaの標準ライブラリじゃだめらしいのでAndroid依存。ほかにはIBMのライブラリも使えるらしい。
    private val breakIterator: BreakIterator = BreakIterator.getCharacterInstance()
    /**
     * This func extract first character from strings if include emojis or not.
     * @throws StringIndexOutOfBoundsException If strings is empty. Please check by [String.isNotEmpty] before call this.
     * */
    fun firstCharacter(string: String): String {
        breakIterator.setText(string)
        val start = breakIterator.first()
        val end = breakIterator.next()
       return string.substring(start, end)
    }
    /**
     * This func count how many character include if include emojis or not.
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
/**
 * Add items to [SnapshotStateList] without conflict items.
 * @receiver [SnapshotStateList]
 * @param values items (If it has conflicts,this func ignore them.)
 * You can also use "vararg" params.
 * */
fun <T> SnapshotStateList<T>.addAsSet(values: Iterable<T>) = this.addAll(values.filter { !this.contains(it) })
fun <T> SnapshotStateList<T>.addAsSet(vararg values: T) = this.addAsSet(values.toList())


