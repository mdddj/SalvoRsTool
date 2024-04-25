package shop.itbug.salvorstool.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import shop.itbug.salvorstool.dialog.GenerateServiceDialog
import shop.itbug.salvorstool.i18n.MyI18n
import shop.itbug.salvorstool.tool.myManager
import shop.itbug.salvorstool.tool.tryGetRsStructPsiElement

class GenerateServiceAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        e.tryGetRsStructPsiElement()?.let {
            GenerateServiceDialog(e.project!!, it).show()
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = e.project != null && e.tryGetRsStructPsiElement() != null && e.tryGetRsStructPsiElement()?.myManager?.getTableName != null
        e.presentation.text = MyI18n.getMessage("g_service")
        super.update(e)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
