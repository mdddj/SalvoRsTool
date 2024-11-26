package shop.itbug.salvorstool.help

import com.intellij.openapi.help.WebHelpProvider

class MyWebHelpProvider : WebHelpProvider() {


    override fun getHelpPageUrl(helpTopicId: String): String? {
       return when (helpTopicId) {
           GENERATE_SEA_ORM_HELP -> "https://mdddj.github.io/SalvoRsToolDocument/sea-orm-json-gen-migration-code.html"
           SEA_ORM_COMMAND  -> "https://www.sea-ql.org/SeaORM/docs/generate-entity/sea-orm-cli/"
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
        const val SEA_ORM_COMMAND = "sea:sea_orm_command_run"
    }
}