package shop.itbug.salvorstool.tool

import com.intellij.openapi.diagnostic.Logger

fun <T: Any> T.log() : Logger {
    return Logger.getInstance(this::class.java)
}