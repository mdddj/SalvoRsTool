package shop.itbug.salvorstool.dialog

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.FormBuilder
import org.rust.lang.core.psi.impl.RsStructItemImpl
import shop.itbug.salvorstool.dsl.SaveToBindModel
import shop.itbug.salvorstool.dsl.saveTo
import shop.itbug.salvorstool.tool.MyRsPsiFactory
import shop.itbug.salvorstool.tool.Tools
import shop.itbug.salvorstool.tool.structItemManager
import shop.itbug.salvorstool.widget.RsEditor
import java.awt.Dimension
import javax.swing.JComponent

data class GenerateRouterDialogModel(var text: String = "", var saveTo: String = "", var fileName: String = "")

private fun GenerateRouterDialogModel.save(project: Project) {
    val psi = Tools.createRsPsiFile(fileName, text, project)
    Tools.saveTo(project, psi, saveTo)
}

class GenerateRouterDialog(private val project: Project, psiElement: RsStructItemImpl) : DialogWrapper(project) {

    private val model = GenerateRouterDialogModel(
        text = MyRsPsiFactory.generateRouterFile(psiElement),
        saveTo = Tools.getRouterFolder(project)?.path ?: "",
        fileName = (psiElement.structItemManager.getTableName ?: "root")
    )
    private val editor = RsEditor(project, model.text)
    private lateinit var settingPanel : DialogPanel

    init {
        super.init()
        title = "Generate Router Dialog"
        editor.border = Tools.emptyBorder()
    }

    override fun createCenterPanel(): JComponent {
       settingPanel = panel {
            saveTo(project, SaveToBindModel(
                folder = model::saveTo,
                fileName = model::fileName
            ))
        }
        settingPanel.registerValidators(disposable)
        return FormBuilder.createFormBuilder()
            .addComponentFillVertically(editor,0)
            .addComponent(settingPanel)
            .panel
    }

    override fun doOKAction() {
        settingPanel.apply()
        model.save(project)
        super.doOKAction()
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(500, 500)
    }
}