package shop.itbug.salvorstool.action.base

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE
import com.intellij.openapi.actionSystem.DefaultActionGroup
import org.rust.lang.core.psi.RsFile
import shop.itbug.salvorstool.tool.structItemManager
import shop.itbug.salvorstool.tool.tryGetRsStructPsiElement

class SalvoActionGroupBase: DefaultActionGroup() {

    override fun update(e: AnActionEvent) {
        val struct = e.tryGetRsStructPsiElement()
        val file = e.getData(PSI_FILE)
        e.presentation.isVisible = file is RsFile && struct != null && struct.structItemManager.fieldList.isNotEmpty()

        super.update(e)
    }



    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}