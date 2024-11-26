package shop.itbug.salvorstool.action.database

import com.intellij.database.model.basic.BasicModTable
import com.intellij.database.view.getSelectedElements
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

abstract class SqlCodeGenerateBaseAction : AnAction() {
    override fun actionPerformed(p0: AnActionEvent) {
        doAction(p0, getTableGenerate(p0))
    }

    override fun update(e: AnActionEvent) {
        doUpdate(e, getTableGenerate(e))
        super.update(e)
    }

    abstract fun doAction(e: AnActionEvent, tables: List<SqlTableGenerate>)
    abstract fun doUpdate(e: AnActionEvent, tables: List<SqlTableGenerate>)


    private fun getTableGenerate(e: AnActionEvent): List<SqlTableGenerate> {
        val datasource = e.dataContext.getSelectedElements()
        val tables = datasource.map { it as BasicModTable }
        val sqlGenerates = SqlGenerateFactory.from(tables)
        return sqlGenerates
    }


}