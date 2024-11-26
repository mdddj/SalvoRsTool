package shop.itbug.salvorstool.widget

import com.intellij.lang.Language
import com.intellij.lang.javascript.dialects.TypeScriptJSXLanguageDialect
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.ui.LanguageTextField
import shop.itbug.salvorstool.tool.Tools
import java.awt.Dimension
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.border.Border

/// type script 编辑器
class TypeJavaScriptEditor(projectP: Project, initText: String) : LanguageTextField(
    Tools.jsxLanguage, projectP, initText, false
) {
    override fun createEditor(): EditorEx {
        return myCreateEditor(super.createEditor())
    }

    override fun getFont(): Font {
        val font = EditorColorsManager.getInstance().globalScheme.getFont(EditorFontType.PLAIN)
        return font
    }

    override fun getBorder() = Tools.emptyBorder()


    override fun getPreferredSize(): Dimension {
        return Dimension(600, 500)
    }

}
