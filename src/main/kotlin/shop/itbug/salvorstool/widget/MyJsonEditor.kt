package shop.itbug.salvorstool.widget

import com.intellij.json.JsonLanguage
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.ui.LanguageTextField
import shop.itbug.salvorstool.tool.Tools
import java.awt.Dimension
import java.awt.Font

/// json编辑器
class MyJsonEditor(p: Project, initText: String = "") : LanguageTextField(JsonLanguage.INSTANCE, p, initText, false) {

    override fun getFont(): Font {
        return EditorColorsManager.getInstance().globalScheme.getFont(EditorFontType.PLAIN)
    }

    override fun createEditor(): EditorEx {
        return myCreateEditor(super.createEditor())
    }


    override fun getPreferredSize(): Dimension {
        return Dimension(600, 500)
    }
}