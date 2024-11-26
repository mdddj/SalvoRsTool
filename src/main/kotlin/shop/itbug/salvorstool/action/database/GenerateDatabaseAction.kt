package shop.itbug.salvorstool.action.database

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import shop.itbug.salvorstool.dialog.SqlGenerateRustStructDialog
import shop.itbug.salvorstool.icons.MyIcon

/**
 * 数据表生成
 */
class GenerateDatabaseAction : SqlCodeGenerateBaseAction() {

    override fun doAction(
        e: AnActionEvent,
        tables: List<SqlTableGenerate>
    ) {
        e.project?.let { project ->
            SqlGenerateRustStructDialog(project, tables).show()
        }
    }

    override fun doUpdate(
        e: AnActionEvent,
        tables: List<SqlTableGenerate>
    ) {
        e.presentation.apply {
            isEnabledAndVisible = tables.isNotEmpty()
            text = "RustX: Generate Rust Struct"
            icon = MyIcon.pluginIcon
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}