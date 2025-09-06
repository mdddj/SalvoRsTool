package shop.itbug.salvorstool.window

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class SalvoApiWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        val apiWindow = SalvoApiWindowFactoryUtils.create(project)
        val apiWindowContent =
            contentFactory.createContent(SalvoApiWindowFactoryUtils.installActions(apiWindow), "Salvo api", false)
        apiWindowContent.setDisposer(apiWindow)
        toolWindow.contentManager.addContent(apiWindowContent)
    }

}
