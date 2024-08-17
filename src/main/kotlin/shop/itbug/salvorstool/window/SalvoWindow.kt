package shop.itbug.salvorstool.window

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class SalvoWindow : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        val apiWindow = ApiScanWindow(project,toolWindow)
        val apiWindowContent = contentFactory.createContent(apiWindow,"Api",false)
        toolWindow.contentManager.addContent(apiWindowContent)

        val tempWindow = TempFilesWindow(project)
        val tempWindowContent = contentFactory.createContent(tempWindow,"Temp",false)
        toolWindow.contentManager.addContent(tempWindowContent)

    }
}

fun ToolWindow.isSalvoWindow(): Boolean {
    return this.id == "Salvo"
}