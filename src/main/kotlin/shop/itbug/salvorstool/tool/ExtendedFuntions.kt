package shop.itbug.salvorstool.tool

import com.google.common.base.CaseFormat
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.psi.PsiElement
import org.rust.lang.core.psi.RsNamedFieldDecl
import org.rust.lang.core.psi.RsOuterAttr
import org.rust.lang.core.psi.impl.RsStructItemImpl
import java.awt.datatransfer.StringSelection
import java.util.*

val PsiElement.myManager get() = MyRsPsiElementManager(this)
val RsStructItemImpl.myManager get() = MyRsStructManager(this)
val RsNamedFieldDecl.myManager get() = MyFieldPsiElementManager(this)
val RsOuterAttr.myManager get() = MyRsOuterAttrPsiElementManager(this)
fun String.capitalizeFirstLetter(): String {
    if (isEmpty()) {
        return this
    }
    return substring(0, 1).uppercase(Locale.getDefault()) + substring(1)
}
///
val String.underlineToCamel: String get() = underlineToCamel(this)
///将驼峰变成下划线
fun underlineToCamel(underlineString: String): String {
    return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, underlineString)
}



fun AnActionEvent.tryGetRsStructPsiElement(): RsStructItemImpl? {
    val psiElement = this.getData(CommonDataKeys.PSI_ELEMENT)
    if ( psiElement != null && psiElement.myManager.isStruct) {
        return psiElement as? RsStructItemImpl
    }
    return null
}

fun String.copy(){
    CopyPasteManager.getInstance().setContents(StringSelection(this))
}