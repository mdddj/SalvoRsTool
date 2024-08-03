package shop.itbug.salvorstool.dialog

import com.google.gson.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.util.Alarm
import com.intellij.util.ui.FormBuilder
import shop.itbug.salvorstool.dialog.GenerateType.*
import shop.itbug.salvorstool.help.MyWebHelpProvider
import shop.itbug.salvorstool.i18n.MyI18n
import shop.itbug.salvorstool.json.BaseJsonParse
import shop.itbug.salvorstool.json.SeaOrmTableFactory
import shop.itbug.salvorstool.json.testJsonString
import shop.itbug.salvorstool.tool.copy
import shop.itbug.salvorstool.tool.padding
import shop.itbug.salvorstool.tool.removeBorder
import shop.itbug.salvorstool.tool.vertical
import shop.itbug.salvorstool.widget.MyJsonEditor
import shop.itbug.salvorstool.widget.RsEditor
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*
import javax.swing.border.Border


private const val jsonEditorHeight = 400

data class TableNameJson(var tableName: String, var json: String)

///生成sea-orm表单
class AskSeaOrmJsonDialog(private val p: Project) : DialogWrapper(p, true) {

    private lateinit var myPanel: DialogPanel
    private val params = TableNameJson("User", "")
    private val jsonEditor = MyJsonEditor(p, initText = testJsonString).apply {
        preferredSize = Dimension(400, jsonEditorHeight)
    }
    private val alarm: Alarm = Alarm(disposable)

    init {
        super.init()
        title = "Json To SeaOrm"

        SwingUtilities.invokeLater {
            addRequest()
        }
    }


    private fun addRequest() {
        if (isDisposed) {
            return
        }
        alarm.addRequest({
            when (myPanel.isModified()) {
                true -> myPanel.validateAll()
                false -> {}
            }
            addRequest()
        }, 1000)
    }

    override fun doValidate(): ValidationInfo? {
        if (params.tableName.isEmpty()) {
            return ValidationInfoBuilder(myPanel).error("${MyI18n.getMessage("sea-orm-table-name-title")} Required field")
        }
        return validJson()
    }

    override fun createCenterPanel(): JComponent {
        myPanel = panel {
            row(MyI18n.getMessage("sea-orm-table-name-title")) {
                textField()
                    .validationOnInput {
                        if (it.text.isEmpty()) {
                            return@validationOnInput error("Required field")
                        }
                        return@validationOnInput null
                    }
                    .validationOnApply {
                        if (it.text.isEmpty()) {
                            return@validationOnApply error("Required field")
                        }
                        return@validationOnApply null
                    }
                    .bindText(params::tableName).align(Align.FILL)
                    .comment(MyI18n.getMessage("sea-orm-table-name-title-comment"))
            }
            row("Json") {
                scrollCell(jsonEditor)
                    .validationOnApply {
                        return@validationOnApply validJson()
                    }
                    .validationOnInput {
                        if (!inputIsJson()) {
                            return@validationOnInput ValidationInfoBuilder(jsonEditor).error("Json ${MyI18n.fieldValidFailed}")
                        }
                        null
                    }
                    .bind({ it.text }, { _, v -> params.json = v }, object : MutableProperty<String> {
                        override fun get(): String {
                            return params.json
                        }

                        override fun set(value: String) {
                            params.json = value
                        }

                    })
                    .align(Align.FILL)
                    .comment(MyI18n.getMessage("sea_orm_json_tips"))
            }
        }
        myPanel.registerValidators(disposable)
        return myPanel
    }

    private fun inputIsJson(): Boolean {
        return run {
            val isOk = isValidJson(jsonEditor.text)
            if (!isOk) return@run false
            if(hasNestedArray(jsonEditor.text)) return@run false
            return@run true
        }
    }

    private fun isValidJson(json: String): Boolean {
        try {
            Gson().fromJson(json, Any::class.java)
            return true
        } catch (ex: JsonSyntaxException) {
            return false
        }
    }

    private fun hasNestedArray(json: String): Boolean {
        try {
            val jsonElement: JsonElement = JsonParser.parseString(json)
            return checkForNestedArray(jsonElement)
        } catch (ex: Exception) {
            return false // 如果解析 JSON 失败，则返回 false
        }
    }

    private fun checkForNestedArray(element: JsonElement): Boolean {
        if (element.isJsonArray) {
            val array: JsonArray = element.asJsonArray
            for (arrayElement in array) {
                if (arrayElement.isJsonArray || arrayElement.isJsonObject) {
                    if (checkForNestedArray(arrayElement)) {
                        return true
                    }
                }
            }
        } else if (element.isJsonObject) {
            val obj: JsonObject = element.asJsonObject
            for (key in obj.keySet()) {
                val objElement: JsonElement = obj.get(key)
                if (objElement.isJsonArray || objElement.isJsonObject) {
                    if (checkForNestedArray(objElement)) {
                        return true
                    }
                }
            }
        }
        return false
    }


    private fun validJson(): ValidationInfo? {
        return run {
            val valid = inputIsJson()
            if (!valid) {
                return@run ValidationInfoBuilder(jsonEditor).error("Json" + MyI18n.getMessage("valid_failed"))
            }
            null
        }
    }

    override fun doOKAction() {
        super.doOKAction()
        SeaOrmGeneratedDialog(p, params).show()
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(500, 600)
    }

    override fun createContentPaneBorder(): Border? {
        return BorderFactory.createEmptyBorder(20, 20, 20, 20)
    }


}


private enum class GenerateType {
    All, UpOnly, DownOnly, EnumOnly
}

private data class GenerateOptions(var type: GenerateType = All, var tableName: String = "")

///获取生成的数据
class SeaOrmGeneratedDialog(p: Project, val prop: TableNameJson) : DialogWrapper(p, true) {

    private val jsonFactory = BaseJsonParse.createByJsonString(prop.json)
    private val factory = object : SeaOrmTableFactory(prop.tableName) {}
    private val options = GenerateOptions(tableName = prop.tableName)
    private val rustEditor = RsEditor(p, factory.generateAll(jsonFactory.getPairList()))
    private val alarm: Alarm = Alarm(this.disposable)
    private lateinit var myPanel: DialogPanel

    init {
        super.init()
        title = MyI18n.getMessage("preview-the-code")
        setOKButtonText(MyI18n.getMessage("copy"))
        SwingUtilities.invokeLater {
            addRequest()
        }
        rustEditor.format()

    }


    private fun addRequest() {
        if (isDisposed) {
            return
        }
        alarm.addRequest({
            val isUpdate = myPanel.isModified()
            if (isUpdate) updateCode()
            addRequest()
        }, 1000)
    }


    private fun updateCode() {
        myPanel.apply()
        factory.changeTableName(options.tableName)
        rustEditor.changeText(getCode())
        rustEditor.format()
    }

    private fun getCode(): String {
        val columns = jsonFactory.getPairList()
        return when (options.type) {
            All -> factory.generateAll(columns)
            UpOnly -> factory.getUpText(columns)
            DownOnly -> factory.getDownText()
            EnumOnly -> factory.getEnum(columns)
        }

    }


    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout())
        myPanel = panel {
            row(MyI18n.getMessage("sea-orm-table-name-title")) {
                textField().bindText(options::tableName).align(Align.FILL)
                    .comment(MyI18n.getMessage("sea-orm-table-name-title-comment"))
            }
            buttonsGroup {
                row("Generate") {
                    GenerateType.entries.forEach {
                        radioButton(it.name, it)
                    }
                }
            }.bind(options::type)
        }
        val previewPanel = JPanel(BorderLayout())
        val builder = FormBuilder.createFormBuilder()
            .addLabeledComponent(MyI18n.getMessage("preview-the-code"), JScrollPane(rustEditor).removeBorder).panel
        previewPanel.add(builder, BorderLayout.CENTER)
        mainPanel.add(myPanel, BorderLayout.NORTH)
        mainPanel.add(previewPanel.vertical, BorderLayout.CENTER)
        return mainPanel.padding
    }

    override fun getHelpId(): String {
        return MyWebHelpProvider.GENERATE_SEA_ORM_HELP
    }


    override fun doOKAction() {
        rustEditor.text.copy()
        super.doOKAction()
    }
}
