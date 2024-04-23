package shop.itbug.salvorstool.widget

import com.intellij.lang.Language
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.ui.LanguageTextField
import org.rust.lang.RsLanguage
import javax.swing.BorderFactory

class RsEditor(project: Project, initText: String) : LanguageTextField(Language.findInstance(RsLanguage::class.java),project,initText,false) {

    override fun createEditor(): EditorEx {
        return myCreateEditor(super.createEditor())
    }

}
 fun myCreateEditor(ex: EditorEx): EditorEx {
    ex.setVerticalScrollbarVisible(true)
    ex.setHorizontalScrollbarVisible(true)
    ex.setBorder(null)
    val settings = ex.settings
    settings.isLineNumbersShown = true
    settings.isAutoCodeFoldingEnabled = true
    settings.isFoldingOutlineShown = true
    settings.isAllowSingleLogicalLineFolding = true
    settings.isRightMarginShown = true
    return ex
}