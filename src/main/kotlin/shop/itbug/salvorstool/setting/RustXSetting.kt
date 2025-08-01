package shop.itbug.salvorstool.setting

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import shop.itbug.salvorstool.cache.SalvoCodegenConfig
import shop.itbug.salvorstool.i18n.MyI18n
import javax.swing.JComponent


class RustXSetting(val project: Project) : Configurable {

    private val configState = SalvoCodegenConfig.getInstance(project).state

    lateinit var myPanel: DialogPanel

    override fun getDisplayName(): @NlsContexts.ConfigurableName String? {
        return "RustX"
    }

    override fun createComponent(): JComponent? {
        myPanel = panel {
            group(MyI18n.codegen) {
                row ("admin project path") {
                    textFieldWithBrowseButton(
                        FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                        project,
                    ).bindText(prop = configState::antdProjectPath).align(Align.FILL)
                }
                row ("admin pages path") {
                    textFieldWithBrowseButton(
                        FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                        project,
                    ).bindText(prop = configState::antdPagesPath).align(Align.FILL)
                }
            }
        }
        return myPanel
    }

    override fun isModified(): Boolean {
        return myPanel.isModified()
    }

    override fun apply() {
        myPanel.apply()
    }
}