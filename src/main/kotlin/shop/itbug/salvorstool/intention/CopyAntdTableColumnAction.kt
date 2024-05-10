package shop.itbug.salvorstool.intention

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.rust.lang.core.psi.impl.RsStructItemImpl
import shop.itbug.salvorstool.tool.copy
import shop.itbug.salvorstool.tool.myManager

class CopyAntdTableColumnAction : PsiElementBaseIntentionAction(), IntentionAction {

    override fun getFamilyName(): String {
        return "SalvoRsTool: Copy Antd Table Column"
    }

    override fun getText(): String {
        return familyName
    }

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return element.parent is RsStructItemImpl
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val rs = element.parent as? RsStructItemImpl ?: return
        rs.myManager.getAntdTableColumnDefine.copy()
    }

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile): IntentionPreviewInfo {
        val rsPsi = getElement(editor, file)?.parent as? RsStructItemImpl
        rsPsi?.let {
            return IntentionPreviewInfo.Html("""<pre lang='json'>${rsPsi.myManager.getAntdTableColumnDefine}</pre>""".trimIndent())
        }
        return IntentionPreviewInfo.EMPTY
    }

}