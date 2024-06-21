package shop.itbug.salvorstool.icons

import com.intellij.openapi.util.IconLoader

object MyIcon {

    val pluginIcon = getIcon("/icons/logo.svg")
    val seaOrmPng = getIcon("/icons/sea-orm.png")

    @JvmStatic
    fun getIcon(path: String) = IconLoader.getIcon(path, MyIcon::class.java)
}