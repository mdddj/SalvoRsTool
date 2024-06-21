package shop.itbug.salvorstool.widget

import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.TransactionGuard
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.EditorSettingsProvider
import com.intellij.ui.LanguageTextField
import com.intellij.util.Alarm
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.UIUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.rust.lang.RsLanguage
import shop.itbug.salvorstool.tool.Tools
import java.awt.Dimension
import java.awt.Font
import java.lang.Thread.sleep
import javax.swing.BorderFactory
import javax.swing.SwingUtilities
import javax.swing.border.Border
import kotlin.concurrent.thread

class RsEditor(projectP: Project, initText: String) : LanguageTextField(Tools.rustLanguage, projectP, initText, false) {

    fun format() {
        WriteCommandAction.runWriteCommandAction(project) {
            val file: PsiFile? = PsiDocumentManager.getInstance(project).getPsiFile(document)
            file?.let {
                CodeStyleManager.getInstance(project).reformat(it)
            }
        }
    }

    override fun createEditor(): EditorEx {
        return myCreateEditor(super.createEditor())
    }

    override fun getFont(): Font {
        return EditorColorsManager.getInstance().globalScheme.getFont(EditorFontType.PLAIN)
    }

    override fun getBorder(): Border {
        return BorderFactory.createEmptyBorder(0, 0, 0, 0)
    }


    fun changeText(newText: String) {
        ApplicationManager.getApplication().runWriteAction {
            document.setText(newText)
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
    }


}

fun myCreateEditor(ex: EditorEx): EditorEx {
    val settings = ex.settings
    settings.isLineNumbersShown = true
    settings.isAutoCodeFoldingEnabled = true
    settings.isFoldingOutlineShown = true
    settings.isAllowSingleLogicalLineFolding = true
    settings.isRightMarginShown = true
    return ex
}