package shop.itbug.salvorstool.tool

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class NotificationUtil(val project: Project) {

    private val SEA_ORM_CLI_GROUP_ID = "SeaOrmCliNotification"

    ///弹出一个通知
    fun seaOrmNotifyInfo(text: String) {
        NotificationGroupManager.getInstance().getNotificationGroup(SEA_ORM_CLI_GROUP_ID)
            .createNotification(text, NotificationType.INFORMATION).notify(project)
    }

    fun seaOrmNotifyError(text: String) {
        NotificationGroupManager.getInstance().getNotificationGroup(SEA_ORM_CLI_GROUP_ID)
            .createNotification(text, NotificationType.ERROR).notify(project)
    }

    companion object {
        fun getInstance(project: Project): NotificationUtil = project.service<NotificationUtil>()
    }


}