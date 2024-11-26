package shop.itbug.salvorstool.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import shop.itbug.salvorstool.dialog.GenerateDtoDialog
import shop.itbug.salvorstool.i18n.MyI18n
import shop.itbug.salvorstool.tool.myManager
import shop.itbug.salvorstool.tool.structItemManager
import shop.itbug.salvorstool.tool.tryGetRsStructPsiElement

class GenerateDtoAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        e.tryGetRsStructPsiElement()?.let {
            GenerateDtoDialog(e.project!!,it).show()
        }
    }
    override fun update(e: AnActionEvent) {
        val struct = e.tryGetRsStructPsiElement()
        e.presentation.isVisible =  e.project != null && struct!=null && struct.structItemManager.getTableName != null
        e.presentation.text = MyI18n.getMessage("g_dto")
        super.update(e)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
