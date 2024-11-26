package shop.itbug.salvorstool.action.database

import com.intellij.database.model.basic.BasicModTable
import com.intellij.database.view.getSelectedElements
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import shop.itbug.salvorstool.dialog.SqlGenerateRustStructDialog
import shop.itbug.salvorstool.icons.MyIcon

/**
 * 数据表生成
 */
class GenerateDatabaseAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val datasource = e.dataContext.getSelectedElements()
        val tables = datasource.map { it as BasicModTable }
        val sqlGenerates = SqlGenerateFactory.from(tables)
        e.project?.let { project ->
            SqlGenerateRustStructDialog(project, sqlGenerates).show()
        }

    }


    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.dataContext.getSelectedElements().isNotEmpty
        e.presentation.text = "RustX: Generate Rust Struct"
        e.presentation.icon = MyIcon.pluginIcon
        super.update(e)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}