package shop.itbug.salvorstool.action.bar

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import shop.itbug.salvorstool.dialog.AskSeaOrmJsonDialog

class JsonToSearOrm : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val dialog = e.project?.let { AskSeaOrmJsonDialog(it) }
        dialog?.show()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project!=null
        super.update(e)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
