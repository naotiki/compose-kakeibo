/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.naotiki_apps.compose_kakeibo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val IO_EXECUTOR = CoroutineScope(Dispatchers.IO)

/**
 * io/database作業に使用される、専用のバックグラウンドスレッドでブロックを実行するユーティリティメソッド。
 */
fun <T> ioThread(onComplete: (T?,Throwable?) -> Unit={_,_->},body:suspend CoroutineScope.() -> T) {
    var result :T?=null
    IO_EXECUTOR.launch{
       result= body()
    }
        .invokeOnCompletion {
        onComplete(result,it)
    }
}


