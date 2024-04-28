package shop.itbug.salvorstool.dialog

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import org.rust.lang.core.psi.impl.RsStructItemImpl
import shop.itbug.salvorstool.i18n.MyI18n
import shop.itbug.salvorstool.tool.MyRsPsiFactory
import shop.itbug.salvorstool.tool.Tools
import shop.itbug.salvorstool.tool.myManager
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
fun GenerateServiceDialogModel.save(project: Project,psiElement: RsStructItemImpl) {
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
    val newPsiFile = Tools.createRsPsiFile(fileName,sb.toString(),project)
    Tools.saveTo(project, newPsiFile, saveTo)
}

///生成service弹窗
class GenerateServiceDialog(private val project: Project, private val psiElement: RsStructItemImpl) : DialogWrapper(project, true) {


    private val tabView = JBTabbedPane()

    private val model = GenerateServiceDialogModel(
        addText = MyRsPsiFactory.generateServiceWithAdd(psiElement),
        updateText = MyRsPsiFactory.generateServiceByUpdate(psiElement),
        deleteText = MyRsPsiFactory.generateServiceByDelete(psiElement),
        findAllText = MyRsPsiFactory.generateServiceByAll(psiElement),
        saveTo = Tools.getServiceFolder(project)?.path ?: "",
        fileName = (psiElement.myManager.getTableName?:"root")
    )

    init {
        super.init()
        title = "Generate Service"
        tabView.add("Add Service", JBScrollPane(RsEditor(project,model.addText)))
        tabView.add("Update Service", JBScrollPane(RsEditor(project,model.updateText)))
        tabView.add("Delete Service", JBScrollPane(RsEditor(project,model.deleteText)))
        tabView.add("Find Service", JBScrollPane(RsEditor(project,model.findAllText)))

    }

    override fun createCenterPanel(): JComponent {
        return panel {
            group("Preview") {
                row {
                    scrollCell(tabView)
                }
            }
            group(MyI18n.saveTo) {
                row(MyI18n.selectDir) {
                    textFieldWithBrowseButton(
                        project = project,
                        fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor().withRoots(
                            project.guessProjectDir()
                        )
                    ).align(Align.FILL)
                        .bindText(model::saveTo)
                }
                row (MyI18n.getMessage("file_name")){
                    textField().bindText(model::fileName)
                }
            }
        }
    }


    override fun doOKAction() {
        model.save(project,psiElement)
        super.doOKAction()
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(550, super.getPreferredSize().height)
    }
}