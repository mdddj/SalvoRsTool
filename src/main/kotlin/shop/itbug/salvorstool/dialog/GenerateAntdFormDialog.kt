package shop.itbug.salvorstool.dialog

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTabbedPane
import org.rust.lang.core.psi.impl.RsStructItemImpl
import shop.itbug.salvorstool.tool.AntdFactory
import shop.itbug.salvorstool.widget.TypeJavaScriptEditor
import java.awt.Dimension
import javax.swing.JComponent

///生成antd表单弹窗
class GenerateAntdFormDialog(project: Project, psiElement: RsStructItemImpl) : DialogWrapper(project, true) {


    private val tabview = JBTabbedPane()

    init {
        super.init()
        title = "Generate Antd Form"
        tabview.add("预览", TypeJavaScriptEditor(project, AntdFactory.generateAntdForm(psiElement)))
    }

    override fun createCenterPanel(): JComponent {
        return tabview
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(500, super.getPreferredSize().height)
    }
}