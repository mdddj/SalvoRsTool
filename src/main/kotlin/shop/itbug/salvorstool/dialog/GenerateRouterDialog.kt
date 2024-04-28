package shop.itbug.salvorstool.dialog

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.DialogWrapper
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

data class GenerateRouterDialogModel(var text: String = "", var saveTo: String = "", var fileName: String = "")

private fun GenerateRouterDialogModel.save(project: Project) {
    val psi = Tools.createRsPsiFile(fileName, text, project)
    Tools.saveTo(project, psi, saveTo)
}

class GenerateRouterDialog(private val project: Project, psiElement: RsStructItemImpl) : DialogWrapper(project) {

    private val tabView = JBTabbedPane()
    private val model = GenerateRouterDialogModel(
        text = MyRsPsiFactory.generateRouterFile(psiElement),
        saveTo = Tools.getRouterFolder(project)?.path ?: "",
        fileName = (psiElement.myManager.getTableName ?: "root")
    )

    init {
        super.init()
        title = "Generate Router Dialog"
        tabView.add("Preview", RsEditor(project, model.text))
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row {
                scrollCell(tabView)
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
                row(MyI18n.getMessage("file_name")) {
                    textField().bindText(model::fileName)
                }
            }
        }
    }

    override fun doOKAction() {
        model.save(project)
        super.doOKAction()
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(500, 500)
    }
}