package shop.itbug.salvorstool.messageing

import com.intellij.util.messages.Topic
import shop.itbug.salvorstool.model.SalvoApiItem

interface ApiScanMessaging {

    fun apiScanEed(apiList: List<SalvoApiItem>)

    companion object {

        @Topic.ProjectLevel
        val TOPIC = Topic.create("salvorstool.api-scan-messaging", ApiScanMessaging::class.java)
    }

}