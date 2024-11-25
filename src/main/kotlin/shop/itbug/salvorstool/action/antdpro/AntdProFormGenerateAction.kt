package shop.itbug.salvorstool.action.antdpro

import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.Alarm
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.components.BorderLayoutPanel
import shop.itbug.salvorstool.action.base.BaseRsStructCodeGenerateAction
import shop.itbug.salvorstool.tool.JavascriptType
import shop.itbug.salvorstool.tool.MyFieldPsiElementManager
import shop.itbug.salvorstool.widget.TypeJavaScriptEditor
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.SwingUtilities

/**
 * antd pro form code generate tool
 */
class AntdProFormGenerateAction : BaseRsStructCodeGenerateAction() {
    override fun codeGenerateAction(model: Model) {
        Dialog(model).show()
    }
}


/**
 * 生成模型代码
 */

private class Dialog(val model: BaseRsStructCodeGenerateAction.Model) : DialogWrapper(model.project) {


    enum class Type(val text: String) {
        ProForm("ProForm"),
        ModalForm("ModalForm"),
    }

    data class Config(var type: Type = Type.ModalForm)

    private val config = Config()
    private val jsEditor = TypeJavaScriptEditor(model.project, generateText())
    private val alarm = Alarm(disposable)
    private lateinit var myPanel: DialogPanel
    init {
        super.init()
        title = "Generate Antd Pro Form"
        SwingUtilities.invokeLater {
            listenChange()
        }
        listenChange()
    }


    private fun listenChange(){
        alarm.addRequest({
            val isUpdate = myPanel.isModified()
            if(isUpdate){
                myPanel.apply()
                jsEditor.text = generateText()
            }
            listenChange()
        },500)
    }

    override fun createCenterPanel(): JComponent {
         myPanel = panel {
            buttonsGroup("Type") {
                row {
                    for (type in Type.entries) {
                        radioButton(type.text, type)
                    }
                }
            }.bind(config::type)
        }
        val panel = FormBuilder.createFormBuilder()
            .addComponentFillVertically(jsEditor,0)
            .addComponent(myPanel,12)
            .panel
        return panel
    }

    fun generateText(): String {
        val sb = StringBuilder()
        sb.appendLine("<${config.type.text}>")
        model.manager.jsModelList.forEach {
            sb.appendLine("\t\t${it.codeGenerate()}")
        }
        sb.appendLine("</${config.type.text}>")
        return sb.toString()
    }

    private fun MyFieldPsiElementManager.JsModel.codeGenerate(): String {
        val requiredText = if (isOption) "" else "required={true}"
        val nameText = "name={'$fieldName'}"
        val labelText = "label={'${comment ?: fieldName}'}"
        return when (this.type) {
            JavascriptType.Number -> """
                <ProFormDigit $requiredText $nameText $labelText />
            """.trimIndent()

            JavascriptType.String -> """
            <ProFormText $requiredText $nameText $labelText />
            """.trimIndent()

            JavascriptType.Bool -> """
                <ProFormCheckbox $requiredText $nameText $labelText />
            """.trimIndent()

            JavascriptType.Unknown -> """
                <ProFormText $requiredText $nameText $labelText />
            """.trimIndent()
        }
    }
}