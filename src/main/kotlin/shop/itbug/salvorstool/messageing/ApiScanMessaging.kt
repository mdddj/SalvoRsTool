package shop.itbug.salvorstool.messageing

import com.intellij.util.messages.Topic
import shop.itbug.salvorstool.model.SalvoApiItem

interface ApiScanMessaging {

    fun apiScanEnd(apiList: List<SalvoApiItem>,refresh: Boolean)

    companion object {

        @Topic.ProjectLevel
        val TOPIC = Topic.create("salvorstool.api-scan-messaging", ApiScanMessaging::class.java)
    }

}