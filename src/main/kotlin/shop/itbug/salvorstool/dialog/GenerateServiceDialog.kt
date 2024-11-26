package shop.itbug.salvorstool.dialog

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
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


data class GenerateServiceDialogModel(
    var addText: String = "",
    var updateText: String = "",
    var deleteText: String = "",
    var findAllText: String = "",
    var saveTo: String = "",
    var fileName: String = ""
)

///执行写入
fun GenerateServiceDialogModel.save(project: Project, psiElement: RsStructItemImpl) {
    val sb = StringBuilder()
    val imports = Tools.getDtoImportPackagesText
    sb.append(imports)

    //添加import
    sb.appendLine(Tools.getServiceImportPackages(psiElement))
    //添加add
    sb.appendLine(addText)
    sb.appendLine(updateText)
    sb.appendLine(deleteText)
    sb.appendLine(findAllText)
    val newPsiFile = Tools.createRsPsiFile(fileName, sb.toString(), project)
    Tools.saveTo(project, newPsiFile, saveTo)
}

///生成service弹窗
class GenerateServiceDialog(private val project: Project, private val psiElement: RsStructItemImpl) :
    DialogWrapper(project, true) {


    private val tabView = JBTabbedPane()
    private lateinit var settingPanel: DialogPanel

    private val model = GenerateServiceDialogModel(
        addText = MyRsPsiFactory.generateServiceWithAdd(psiElement),
        updateText = MyRsPsiFactory.generateServiceByUpdate(psiElement),
        deleteText = MyRsPsiFactory.generateServiceByDelete(psiElement),
        findAllText = MyRsPsiFactory.generateServiceByAll(psiElement),
        saveTo = Tools.getServiceFolder(project)?.path ?: "",
        fileName = (psiElement.structItemManager.getTableName ?: "root")
    )

    init {
        super.init()
        title = "Generate Service"
        tabView.add("Add Service", RsEditor(project, model.addText))
        tabView.add("Update Service", RsEditor(project, model.updateText))
        tabView.add("Delete Service", RsEditor(project, model.deleteText))
        tabView.add("Find Service", RsEditor(project, model.findAllText))
        tabView.border = Tools.emptyBorder()
    }

    override fun createCenterPanel(): JComponent {
        settingPanel = panel {
            saveTo(
                project, SaveToBindModel(
                    folder = model::saveTo,
                    fileName = model::fileName
                )
            )
        }
        return FormBuilder.createFormBuilder()
            .addComponentFillVertically(tabView, 0)
            .addComponent(settingPanel, 12)
            .panel
    }


    override fun doOKAction() {
        settingPanel.apply()
        model.save(project, psiElement)
        super.doOKAction()
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(550, super.getPreferredSize().height)
    }
}