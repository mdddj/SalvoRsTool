package shop.itbug.salvorstool.dialog

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.rust.lang.core.psi.impl.RsStructItemImpl
import shop.itbug.salvorstool.cache.SalvoCodegenConfig
import shop.itbug.salvorstool.dsl.HttpSelectDsl
import shop.itbug.salvorstool.model.SalvoApiItem
import shop.itbug.salvorstool.model.SalvoApiItemMethod
import shop.itbug.salvorstool.service.SalvoCodegenService
import shop.itbug.salvorstool.tool.StructCodegenBase
import shop.itbug.salvorstool.tool.capitalizeFirstLetter
import shop.itbug.salvorstool.tool.log
import shop.itbug.salvorstool.tool.structItemManager
import javax.swing.JComponent

data class SalvoCodegenDialogConfig(
    var folderName: String = "",
    var getApi: SalvoApiItem? = null,
    var postApi: SalvoApiItem? = null,
    var updateApi: SalvoApiItem? = null,
    var deleteApi: SalvoApiItem? = null,
)

///一键生成所有代码
class SalvoCodegenAllDialog(val project: Project, rsStruct: RsStructItemImpl) : DialogWrapper(project, true) {
    val log = log()
    val codeGenConfig = SalvoCodegenConfig.getInstance(project)
    val codegenService = SalvoCodegenService.getInstance(project)
    val rsFun = rsStruct.structItemManager
    val codegenFun = StructCodegenBase(project, rsStruct)
    val codegenState = codeGenConfig.state
    lateinit var myPanel: DialogPanel
    val dialogConfig = SalvoCodegenDialogConfig(folderName = rsFun.getTableName?.capitalizeFirstLetter() ?: "")

    init {
        super.init()
        title = "Salvo Codegen All"
    }

    override fun createCenterPanel(): JComponent? {
        myPanel = panel {

            group("Base Config") {
                row("Web Folder Name") {
                    textField().bindText(dialogConfig::folderName)
                }
            }

            group("Http API Select") {
                HttpSelectDsl.apiSelect(
                    project,
                    this,
                    filter = { it.filter { item -> item.method == SalvoApiItemMethod.Get } }, "Get"
                ) {
                    it.align(Align.FILL)
                    it.bindItem(dialogConfig::getApi)
                }
                HttpSelectDsl.apiSelect(
                    project,
                    this,
                    filter = { it.filter { item -> item.method == SalvoApiItemMethod.Post } }, "Post"
                ) {
                    it.align(Align.FILL)
                    it.bindItem(dialogConfig::postApi)
                }
                HttpSelectDsl.apiSelect(
                    project,
                    this,
                    filter = { it.filter { item -> item.method == SalvoApiItemMethod.Put } }, "Put"
                ) {
                    it.align(Align.FILL)
                    it.bindItem(dialogConfig::updateApi)
                }
                HttpSelectDsl.apiSelect(
                    project,
                    this,
                    filter = { it.filter { item -> item.method == SalvoApiItemMethod.Delete } }, "Delete"
                ) {
                    it.align(Align.FILL)
                    it.bindItem(dialogConfig::deleteApi)
                }
            }
        }
        return myPanel
    }

    override fun doOKAction() {
        codegenService.runJob {

        }
        myPanel.apply()

        //生成对应的模块目录
        super.doOKAction()
        writeAction()

    }

    fun writeAction() {
        val moduleFile = codegenFun.generateModelDirectory(codegenState.antdPagesPath, dialogConfig.folderName)
        codegenService.runJob {
            it.launch {
                codegenFun.generateApiFile(moduleFile, dialogConfig, project)
            }
            it.launch {
                codegenFun.generateModelFile(moduleFile, dialogConfig, rsFun, project)
            }
            it.launch {
                codegenFun.generateIndexFile(moduleFile, dialogConfig, rsFun, project)
            }
            it.launch {
                codegenFun.generateAddOrUpdateFile(moduleFile, dialogConfig, rsFun, project)
            }
        }
    }
}