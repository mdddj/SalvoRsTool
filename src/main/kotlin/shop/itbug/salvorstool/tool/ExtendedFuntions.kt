package shop.itbug.salvorstool.tool

import com.google.common.base.CaseFormat
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.psi.PsiElement
import org.rust.lang.core.psi.RsNamedFieldDecl
import org.rust.lang.core.psi.RsOuterAttr
import org.rust.lang.core.psi.impl.RsStructItemImpl
import java.awt.datatransfer.StringSelection
import java.util.*

val rsFileType = FileTypeManager.getInstance().getFileTypeByExtension("rs")
val PsiElement.myManager get() = MyRsPsiElementManager(this)
val RsStructItemImpl.myManager get() = MyRsStructManager(this)
val RsNamedFieldDecl.myManager get() = MyFieldPsiElementManager(this)
val RsOuterAttr.myManager get() = MyRsOuterAttrPsiElementManager(this)

///首字母变大小
fun String.capitalizeFirstLetter(): String {
    if (isEmpty()) {
        return this
    }
    return substring(0, 1).uppercase(Locale.getDefault()) + substring(1)
}
///下划线变驼峰
val String.underlineToCamel: String get() = underlineToCamel(this)
///将驼峰变成下划线
fun underlineToCamel(underlineString: String): String {
    return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, underlineString)
}
///首字母变小写
fun firstCharToLowercase(input: String): String {
    if (input.isEmpty() || input.length == 1) return input
    val firstChar = input[0]
    return if (firstChar.isUpperCase()) {
        firstChar.lowercase() + input.substring(1)
    } else {
        input
    }
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