package shop.itbug.salvorstool.widget

import com.intellij.lang.Language
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorSettingsProvider
import com.intellij.ui.LanguageTextField
import com.intellij.util.ui.JBFont
import org.rust.lang.RsLanguage
import shop.itbug.salvorstool.tool.Tools
import java.awt.Dimension
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.border.Border

class RsEditor(projectP: Project, initText: String) : LanguageTextField(Tools.rustLanguage,projectP,initText,false) {

    override fun createEditor(): EditorEx {
        return myCreateEditor(super.createEditor())
    }

    override fun getFont(): Font {
        return EditorColorsManager.getInstance().globalScheme.getFont(EditorFontType.PLAIN)
    }

    override fun getBorder(): Border {
        return BorderFactory.createEmptyBorder(0,0,0,0)
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