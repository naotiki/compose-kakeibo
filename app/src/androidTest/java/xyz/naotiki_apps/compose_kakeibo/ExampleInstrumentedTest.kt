package xyz.naotiki_apps.compose_kakeibo

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    lateinit var emojiUtil:EmojiUtil
    @Before
    fun initialize(){
        emojiUtil= EmojiUtil.getInstance()
    }
    @Test
    fun useAppContext() {
        //Error Log.i("EMOJI",emojiUtil.firstCharacter(""))
        Log.i("EMOJI",emojiUtil.firstCharacter("あ"))
        Log.i("EMOJI",emojiUtil.firstCharacter("あいうえお"))
        Log.i("EMOJI",emojiUtil.firstCharacter("\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC66"))
        Log.i("EMOJI",emojiUtil.firstCharacter("\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC66あ"))



        Log.i("EMOJI", emojiUtil.characterCount("").toString())
        Log.i("EMOJI", emojiUtil.characterCount("あ").toString())
        Log.i("EMOJI", emojiUtil.characterCount("あいうえお").toString())
        Log.i("EMOJI", emojiUtil.characterCount("\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC66").toString())
        Log.i("EMOJI", emojiUtil.characterCount("\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC66あいうえお").toString())

    }
    @After
    fun a(){

    }
}
