package shop.itbug.salvorstool.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTabbedPane
import org.rust.lang.core.psi.impl.RsStructItemImpl
import shop.itbug.salvorstool.tool.tryGetRsStructPsiElement
import javax.swing.JComponent

class GenerateHookFormAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        Dialog(e.project!!,e.tryGetRsStructPsiElement()!!).show()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = e.tryGetRsStructPsiElement() != null && e.project != null
        e.presentation.text = "Generate React Hook Form"
        super.update(e)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    private inner class Dialog(project: Project,psi: RsStructItemImpl) : DialogWrapper(project) {
        private val tab = JBTabbedPane()

        init {
            tab.add(JBLabel("Removed!"))
            super.init()
        }

        override fun createCenterPanel(): JComponent {
            return tab
        }
    }
}

