package shop.itbug.salvorstool.widget

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.ui.LanguageTextField
import shop.itbug.salvorstool.tool.Tools
import java.awt.Dimension
import java.awt.Font

class RsEditor(projectP: Project, initText: String) : LanguageTextField(Tools.rustLanguage, projectP, initText, false) {

    fun format() {
        WriteCommandAction.runWriteCommandAction(project) {
            val file: PsiFile? = PsiDocumentManager.getInstance(project).getPsiFile(document)
            file?.let {
                CodeStyleManager.getInstance(project).reformat(it)
            }
        }
    }

    init {
        autoscrolls = true
    }

    override fun createEditor(): EditorEx {
        return myCreateEditor(super.createEditor())
    }

    override fun getFont(): Font {
        return EditorColorsManager.getInstance().globalScheme.getFont(EditorFontType.PLAIN)
    }


    override fun getBorder() = Tools.emptyBorder()


    fun changeText(newText: String) {
        ApplicationManager.getApplication().runWriteAction {
            document.setText(newText)
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
    }
    override fun getPreferredSize(): Dimension {
        return Dimension(600, 500)
    }
}

fun myCreateEditor(ex: EditorEx): EditorEx {
    val settings = ex.settings
    settings.isLineNumbersShown = true
    settings.isAutoCodeFoldingEnabled = true
    settings.isFoldingOutlineShown = true
    settings.isAllowSingleLogicalLineFolding = true
    settings.isRightMarginShown = true
    settings.isAnimatedScrolling = true
    ex.setVerticalScrollbarVisible(true)
    ex.setHorizontalScrollbarVisible(true)
    ex.setBorder(Tools.emptyBorder())
    return ex
}