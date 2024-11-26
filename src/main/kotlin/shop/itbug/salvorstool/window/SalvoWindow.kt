package shop.itbug.salvorstool.window

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class SalvoWindow : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        val apiWindow = SalvoApiWindowFactory.create(project)
        val apiWindowContent = contentFactory.createContent(SalvoApiWindowFactory.installActions(apiWindow),"Api",false)
        apiWindowContent.setDisposer(apiWindow)
        toolWindow.contentManager.addContent(apiWindowContent)

        val tempWindow = TempFilesWindow(project)
        val tempWindowContent = contentFactory.createContent(tempWindow,"Temp",false)
        toolWindow.contentManager.addContent(tempWindowContent)
    }

}

fun ToolWindow.isSalvoWindow(): Boolean {
    return isSalvoWindow(this.id)
}

fun isSalvoWindow(id: String?): Boolean {
    return id == "Salvo"
}