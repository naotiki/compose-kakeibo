package xyz.naotiki_apps.compose_kakeibo

interface  CSVExporter<T> {
    fun generateHeader():String
    fun encodeToCSV(value:T):String
    fun decodeFromCSV(string: String): T

}

