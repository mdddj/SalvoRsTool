package shop.itbug.salvorstool.intention

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.rust.lang.core.psi.impl.RsStructItemImpl
import shop.itbug.salvorstool.icons.MyIcon
import shop.itbug.salvorstool.tool.MyRsStructManager
import shop.itbug.salvorstool.tool.Tools
import shop.itbug.salvorstool.tool.copy
import shop.itbug.salvorstool.tool.structItemManager
import javax.swing.Icon


abstract class CopyTSInterfaceActionBase : PsiElementBaseIntentionAction(), IntentionAction, Iconable {
    override fun getFamilyName(): String {
        return "RustX: Copy TS interface"
    }

    override fun getText(): String {
        return familyName
    }

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return element.parent is RsStructItemImpl
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val rs = element.parent as? RsStructItemImpl ?: return
        rs.structItemManager.getTSInterface.copy()
    }

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile): IntentionPreviewInfo {
        var preview = IntentionPreviewInfo.Html("")
        val psiElement = getElement(editor, file)?.parent as? RsStructItemImpl
        psiElement?.let {
            val interfaceString = getTsModelString(it,it.structItemManager)
            val html = Tools.highlightCodeToHtml(interfaceString,project, Tools.jsxLanguage)
            preview = IntentionPreviewInfo.Html(html)
        }
        return preview
    }

    override fun getIcon(p0: Int): Icon? {
        return MyIcon.pluginIcon
    }

    abstract fun getTsModelString(element: PsiElement,manager: MyRsStructManager): String

}

///
class CopyTSInterfaceAction : CopyTSInterfaceActionBase() {
    override fun getTsModelString(element: PsiElement, manager: MyRsStructManager): String {
        return manager.getTSInterface
    }
}

class CopyTSInterfaceActionWithCodegen : CopyTSInterfaceActionBase() {
    override fun getTsModelString(element: PsiElement, manager: MyRsStructManager): String {
        return manager.getTSInterfaceWithCodegen
    }

    override fun getFamilyName(): String {
        return super.getFamilyName() + "(codegen)"
    }
}