package shop.itbug.salvorstool.action

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.wm.ToolWindowManager
import kotlinx.coroutines.runBlocking
import shop.itbug.salvorstool.service.ActixProjectService

class ShowActixApiWindowAction :
        AnAction("Show Actix Endpoints", "Show Actix-Web API endpoints", AllIcons.FileTypes.Json),
        DumbAware {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project
        if (project == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        // 检查是否是Actix项目
        val actixService = ActixProjectService.getInstance(project)
        val isActixProject = runBlocking {
            try {
                actixService.isUseActixDeps()
            } catch (ex: Exception) {
                false
            }
        }

        e.presentation.isEnabledAndVisible = isActixProject
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val toolWindowManager = ToolWindowManager.getInstance(project)
        val toolWindow = toolWindowManager.getToolWindow("Actix Endpoints")

        if (toolWindow != null) {
            toolWindow.show()
        }
    }
}
