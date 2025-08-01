package shop.itbug.salvorstool.window

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import kotlinx.coroutines.runBlocking
import shop.itbug.salvorstool.service.RustProjectService

class ActixApiWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val actixApiWindow = ActixApiWindow(project)
        val content = ContentFactory.getInstance().createContent(actixApiWindow, "Actix", false)
        content.setDisposer(actixApiWindow)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project): Boolean {
        return runBlocking { RustProjectService.getInstance(project).hasActixWebDependencies() }
    }

}
