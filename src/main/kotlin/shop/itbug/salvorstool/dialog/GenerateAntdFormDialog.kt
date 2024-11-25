package shop.itbug.salvorstool.dialog

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import org.rust.lang.core.psi.impl.RsStructItemImpl
import shop.itbug.salvorstool.tool.AntdFactory
import shop.itbug.salvorstool.tool.Tools
import shop.itbug.salvorstool.widget.TypeJavaScriptEditor
import java.awt.Dimension
import javax.swing.JComponent

///生成antd表单弹窗
class GenerateAntdFormDialog(project: Project, psiElement: RsStructItemImpl) : DialogWrapper(project, true) {


    private val tabview = JBTabbedPane()

    init {
        super.init()
        title = "Generate Antd Form"
        tabview.add(
            "Preview",
            JBScrollPane(TypeJavaScriptEditor(project, AntdFactory.generateAntdForm(psiElement))).also { pane ->
                pane.border =
                    Tools.emptyBorder()
                pane.preferredSize = Dimension(500,400)
            })
        tabview.border = Tools.emptyBorder()
    }

    override fun createCenterPanel(): JComponent {
        val panel = FormBuilder.createFormBuilder().addComponentFillVertically(tabview, UIUtil.DEFAULT_VGAP).panel
        return panel
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(500, super.preferredSize.height)
    }
}