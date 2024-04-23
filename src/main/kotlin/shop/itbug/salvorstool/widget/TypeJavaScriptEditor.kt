package shop.itbug.salvorstool.widget

import com.intellij.lang.Language
import com.intellij.lang.javascript.dialects.TypeScriptJSXLanguageDialect
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.ui.LanguageTextField
import org.rust.lang.RsLanguage

/// type script 编辑器
class TypeJavaScriptEditor(project: Project,initText: String)  : LanguageTextField(Language.findInstance(
    TypeScriptJSXLanguageDialect::class.java),project,initText,false) {
    override fun createEditor(): EditorEx {
        return myCreateEditor(super.createEditor())
    }
}
