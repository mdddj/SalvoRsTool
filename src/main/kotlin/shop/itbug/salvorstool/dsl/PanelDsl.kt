package shop.itbug.salvorstool.dsl

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.layout.ValidationInfoBuilder
import shop.itbug.salvorstool.i18n.MyI18n
import shop.itbug.salvorstool.tool.fileIsExits
import java.io.File
import kotlin.reflect.KMutableProperty0

data class SaveToBindModel(var folder: KMutableProperty0<String>, var fileName: KMutableProperty0<String>)

/**
 * 保存文件到目录的基本配置
 */
fun Panel.saveTo(project: Project,bind: SaveToBindModel) : Panel {
    this.group(MyI18n.saveTo) {
        row(MyI18n.selectDir) {
            textFieldWithBrowseButton(
                project = project,
                fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor().withRoots(
                    project.guessProjectDir()
                )
            ).align(Align.FILL)
                .bindText(bind.folder).validationOnInput {
                    val fullPath =  it.text
                    if(!project.fileIsExits(fullPath)) {
                        return@validationOnInput ValidationInfoBuilder(it.textField).error(MyI18n.folderIsNotFound)
                    }
                    return@validationOnInput null
                }
        }
        row (MyI18n.getMessage("file_name")) {
            textField().bindText(bind.fileName).validationOnInput {
                var folderPath = bind.folder.get()
                if(!folderPath.endsWith(File.separatorChar)){
                    folderPath += File.separator
                }
                val fullPath =  folderPath + it.text + ".rs"
                if(project.fileIsExits(fullPath)){
                    return@validationOnInput ValidationInfoBuilder(it).error(MyI18n.fileIsExist)
                }
                return@validationOnInput null
            }
        }
    }
    return this
}