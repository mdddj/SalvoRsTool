package shop.itbug.salvorstool.dialog

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.components.BorderLayoutPanel
import org.rust.lang.core.psi.RsNamedFieldDecl
import org.rust.lang.core.psi.impl.RsStructItemImpl
import shop.itbug.salvorstool.dsl.SaveToBindModel
import shop.itbug.salvorstool.dsl.saveTo
import shop.itbug.salvorstool.i18n.MyI18n
import shop.itbug.salvorstool.tool.*
import shop.itbug.salvorstool.widget.RsEditor
import java.awt.Dimension
import java.io.File
import java.util.*
import javax.swing.JComponent
import javax.swing.SwingUtilities


data class GenerateDtoDialogParam(
    var addRequestName: String = "",
    var addRequestText: String = "",
    var updateRequestName: String = "",
    var updateRequestText: String = "",
    var responseName: String = "",
    var responseText: String = "",
    var saveTo: String = "",
    var fileName: String = ""
)

enum class GenerateDtoDialogResultEnum {
    AddRequest, UpdateRequest, Response,
}

///获取struct名称,例如: "user"表名字,生成对象的名称:"UserAddRequest"
fun GenerateDtoDialogResultEnum.getStructName(sqlTableName: String): String {
    return when (this) {
        GenerateDtoDialogResultEnum.AddRequest -> sqlTableName.underlineToCamel + "AddRequest"
        GenerateDtoDialogResultEnum.UpdateRequest -> sqlTableName.underlineToCamel + "UpdateRequest"
        GenerateDtoDialogResultEnum.Response -> sqlTableName.underlineToCamel + "Response"
    }.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

///生成注解
fun GenerateDtoDialogResultEnum.getOuterAttr(): String {
    return when (this) {
        GenerateDtoDialogResultEnum.AddRequest -> "#[derive(Deserialize, Debug, Validate, ToSchema, Default)]"
        GenerateDtoDialogResultEnum.UpdateRequest -> "#[derive(Debug, Deserialize, Extractible, ToSchema, Default)]\n#[salvo(extract(default_source(from = \"body\", parse = \"json\")))]"
        GenerateDtoDialogResultEnum.Response -> "#[derive(Debug, Serialize, ToSchema, Default)]"
    }
}

///过滤字段
fun GenerateDtoDialogResultEnum.getFields(list: List<RsNamedFieldDecl>): List<RsNamedFieldDecl> {
    return when (this) {
        GenerateDtoDialogResultEnum.AddRequest -> list.filter { !it.namedFieldManager.isPrimaryKey }
        GenerateDtoDialogResultEnum.UpdateRequest -> list
        GenerateDtoDialogResultEnum.Response -> list
    }
}

///预处理
fun GenerateDtoDialogResultEnum.preview(field: RsNamedFieldDecl): String? {
    return when (this) {
        GenerateDtoDialogResultEnum.UpdateRequest -> {
            if (field.namedFieldManager.isPrimaryKey) {
                return "    #[salvo(extract(source(from = \"param\")))]"
            }
            return null
        }

        else -> null
    }
}

///执行写入
fun GenerateDtoDialogParam.save(project: Project, psiElement: RsStructItemImpl) {
    val sb = StringBuilder()
    val imports = Tools.getDtoImportPackagesText
    sb.append(imports)

    //添加add
    sb.appendLine(MyRsPsiFactory.generateDto(GenerateDtoDialogResultEnum.AddRequest, psiElement))
    //添加update
    sb.appendLine(MyRsPsiFactory.generateDto(GenerateDtoDialogResultEnum.UpdateRequest, psiElement))
    //添加response
    sb.appendLine(MyRsPsiFactory.generateDto(GenerateDtoDialogResultEnum.Response, psiElement))

    val newPsiFile = Tools.createRsPsiFile(fileName, sb.toString(), project)
    Tools.saveTo(project, newPsiFile, saveTo)
}


///生成dto对象
class GenerateDtoDialog(private val project: Project, private val psiElement: RsStructItemImpl) :
    DialogWrapper(project) {

    private val model = GenerateDtoDialogParam(
        addRequestText = MyRsPsiFactory.generateDto(GenerateDtoDialogResultEnum.AddRequest, psiElement),
        addRequestName = GenerateDtoDialogResultEnum.AddRequest.getStructName(
            psiElement.structItemManager.getTableName ?: ""
        ),
        updateRequestText = MyRsPsiFactory.generateDto(GenerateDtoDialogResultEnum.UpdateRequest, psiElement),
        updateRequestName = GenerateDtoDialogResultEnum.UpdateRequest.getStructName(
            psiElement.structItemManager.getTableName ?: ""
        ),
        responseText = MyRsPsiFactory.generateDto(GenerateDtoDialogResultEnum.Response, psiElement),
        responseName = GenerateDtoDialogResultEnum.Response.getStructName(
            psiElement.structItemManager.getTableName ?: ""
        ),
        saveTo = Tools.getDtoFolder(project)?.path ?: "",
        fileName = (psiElement.structItemManager.getTableName ?: "root")
    )

    private val tabView = JBTabbedPane()
    private lateinit var myPanel: DialogPanel
    private lateinit var settingPanel: Panel

    init {
        super.init()
        title = "Generate DTO Object"
        tabView.add("Add Request Param", panel {
            row("Name:") {
                textField().bindText(model::addRequestName).enabled(false)
            }
            row("Generate Class:") {
                scrollCell(RsEditor(project, model.addRequestText)).align(Align.FILL)
            }
        })
        tabView.add("Update Request Param", panel {
            row("Name:") {
                textField().bindText(model::updateRequestName).enabled(false)
            }
            row("Generate Class:") {
                scrollCell(RsEditor(project, model.updateRequestText)).align(Align.FILL)
            }
        })
        tabView.add("Response Param", panel {
            row("Name:") {
                textField().bindText(model::responseName).enabled(false)
            }
            row("Generate Class:") {
                scrollCell(RsEditor(project, model.responseText)).align(Align.FILL)
            }
        })
    }

    override fun createCenterPanel(): JComponent {
        myPanel = panel {
            settingPanel = saveTo(
                project, SaveToBindModel(
                    model::saveTo,
                    model::fileName
                )
            )
        }
        SwingUtilities.invokeLater {
            myPanel.registerValidators(disposable)
        }

        return FormBuilder.createFormBuilder()
            .addComponentFillVertically(tabView, 0)
            .addSeparator()
            .addComponent(myPanel, 12)
            .panel
    }


    override fun doOKAction() {
        model.save(project, psiElement)
        super.doOKAction()
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(550, super.getPreferredSize().height)
    }

    override fun doValidate(): ValidationInfo? {
        if (!fileIsExits(model.saveTo)) {
            return ValidationInfo(MyI18n.folderIsNotFound)
        } else if (fileIsExits(model.saveTo.removeSuffix(File.separator) + File.separator + model.fileName + ".rs")) {
            return ValidationInfo(MyI18n.fileIsExist)
        }
        return super.doValidate()
    }


}