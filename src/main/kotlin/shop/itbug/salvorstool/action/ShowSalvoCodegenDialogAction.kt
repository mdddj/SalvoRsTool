package shop.itbug.salvorstool.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import shop.itbug.salvorstool.dialog.SalvoCodegenAllDialog
import shop.itbug.salvorstool.tool.tryGetRsStructPsiElement

class ShowSalvoCodegenDialogAction: AnAction(), DumbAware{
    override fun actionPerformed(e: AnActionEvent) {
        e.project?.let { project ->
            val dialog = SalvoCodegenAllDialog(project,e.tryGetRsStructPsiElement()!!)
            dialog.show()
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.tryGetRsStructPsiElement() != null
        super.update(e)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}