package xyz.naotiki_apps.compose_kakeibo

class EnumExtensions {
    companion object {
        inline fun <reified T : Enum<T>> getFromOrdinal(value: Int): T? =
            enumValues<T>().firstOrNull { it.ordinal == value }

        inline fun <reified T : Enum<T>> Int.toEnum(): T? =
            enumValues<T>().getOrNull(this)

        inline fun <reified T : Enum<T>> getSize(): Int =
            enumValues<T>().size
    }
}

/**
  最後の要素なら最初に戻す
 **/
inline fun <reified T : Enum<T>> Enum<T>.slideToNextWithLoop(): T {
    val values = enumValues<T>()
    return if (values.last() == this) values.first() else values[values.indexOf(this) + 1]
}

/**
 最初の要素なら最後に戻す
 **/
inline fun <reified T : Enum<T>> Enum<T>.slideToPreviousWithLoop(): T {
    val values = enumValues<T>()
    return if (values.first() == this) values.last() else values[values.indexOf(this) - 1]
}

