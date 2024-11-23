package shop.itbug.salvorstool.dialog

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.JBUI
import shop.itbug.salvorstool.action.database.SqlTableGenerate
import shop.itbug.salvorstool.tool.Tools
import shop.itbug.salvorstool.tool.structName
import shop.itbug.salvorstool.widget.RsEditor
import java.awt.Insets
import javax.swing.JComponent

class SqlGenerateRustStructDialog(project: Project, private val generates: List<SqlTableGenerate>) :
    DialogWrapper(project, true) {
    private val tab = JBTabbedPane()

    init {
        super.init()
        title = "Generate Database to Rust Struct"
        tab.add("Structs", RsEditor(project, generateAll()))
        generates.forEach {
            tab.add(it.getTableName().structName, RsEditor(project, it.generateRustStruct()))
        }
        tab.rootPane.border = Tools.emptyBorder()
        tab.border = Tools.emptyBorder()
        tab.tabComponentInsets = JBUI.emptyInsets()
    }


    override fun createCenterPanel(): JComponent {
        val pane = JBScrollPane(tab)
        pane.border = Tools.emptyBorder()
        pane.preferredSize = JBUI.size(500, 500)
        return pane
    }


    private fun generateAll(): String {
        return generates.joinToString("\n\n") { it.generateRustStruct() }
    }
}