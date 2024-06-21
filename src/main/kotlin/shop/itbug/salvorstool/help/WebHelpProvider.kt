package shop.itbug.salvorstool.help

import com.intellij.openapi.help.WebHelpProvider

class MyWebHelpProvider : WebHelpProvider() {

    init {
        println("my web help provider initialized!")
    }

    override fun getHelpPageUrl(helpTopicId: String): String? {
        println("help id $helpTopicId")
       return when (helpTopicId) {
           GENERATE_SEA_ORM_HELP -> "https://itbug.shop/sea-orm-help"
           else -> {
               null
           }
        }
    }

    override fun getHelpTopicPrefix(): String {
        return "sea:"
    }


    companion object {
        const val GENERATE_SEA_ORM_HELP  = "sea:generate_sea_orm_helper"
    }
}