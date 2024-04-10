package shop.itbug.salvorstool.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import org.rust.lang.core.psi.impl.RsStructItemImpl
import shop.itbug.salvorstool.dialog.GenerateDtoDialog
import shop.itbug.salvorstool.i18n.MyI18n
import shop.itbug.salvorstool.tool.myManager

class GenerateDtoAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val psiElement = e.getData(CommonDataKeys.PSI_ELEMENT)
        GenerateDtoDialog(e.project!!,psiElement as RsStructItemImpl).show()
    }

    override fun update(e: AnActionEvent) {
        val psiElement = e.getData(CommonDataKeys.PSI_ELEMENT)
        e.presentation.isVisible =  e.project != null && psiElement != null && psiElement.myManager.isStruct
        e.presentation.text = MyI18n.getMessage("g_dto")
        super.update(e)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
