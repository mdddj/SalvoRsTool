package shop.itbug.salvorstool.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import shop.itbug.salvorstool.dialog.GenerateAntdFormDialog
import shop.itbug.salvorstool.tool.tryGetRsStructPsiElement


///生成antd表单
class GenerateAntdFormAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        e.tryGetRsStructPsiElement()?.let {
            GenerateAntdFormDialog(e.project!!, it).show()
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = e.tryGetRsStructPsiElement() != null
        super.update(e)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
