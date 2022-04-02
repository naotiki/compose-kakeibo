package xyz.naotiki_apps.compose_kakeibo

import androidx.compose.runtime.snapshots.SnapshotStateList

val KakeiboScreen.Companion.a: String
    get() = "ssss"

/**
 * 最初の文字を取得する
 * - サロゲートペア文字対応
 * - 絵文字とか𠮷田さん向け
 * */
fun String.firstLetter(): String {

    val int = this.codePointAt(0)
    //サロゲ...じゃなきゃそのままreturn
    if (int <= 0xFFFF) return this[0].toString()
    //なんかしてる・・・・よくわからん
    val cp: Int = int - 0x10000
    val high = 0xD800 or (cp shr 10)
    val low = 0xDC00 or (cp and 0x3FF)
    return String(codePoints = arrayOf(high, low).toIntArray(), offset = 0, 2)
}

//Setのようにあつかいたーい
fun <T> SnapshotStateList<T>.addAsSet(values: Iterable<T>) = this.addAll(values.filter { !this.contains(it) })
fun <T> SnapshotStateList<T>.addAsSet(vararg values: T) = this.addAsSet(values.toList())


