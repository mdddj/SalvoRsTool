package shop.itbug.salvorstool.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import shop.itbug.salvorstool.dialog.GenerateRouterDialog
import shop.itbug.salvorstool.i18n.MyI18n
import shop.itbug.salvorstool.tool.myManager
import shop.itbug.salvorstool.tool.tryGetRsStructPsiElement

class GenerateRouterAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        e.tryGetRsStructPsiElement()?.let {
            GenerateRouterDialog(e.project!!,it).show()
        }

    }

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible =  e.project != null && e.tryGetRsStructPsiElement() != null
        e.presentation.text = MyI18n.getMessage("g_router")
        super.update(e)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
