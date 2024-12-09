package shop.itbug.salvorstool.tool

import com.google.common.base.CaseFormat
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.PsiNavigateUtil
import org.rust.lang.core.psi.RsNamedFieldDecl
import org.rust.lang.core.psi.RsOuterAttr
import org.rust.lang.core.psi.impl.RsStructItemImpl
import java.awt.datatransfer.StringSelection
import java.util.*
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JScrollPane

val rsFileType = FileTypeManager.getInstance().getFileTypeByExtension("rs")
val RsStructItemImpl.structItemManager get() = MyRsStructManager(this)
val RsNamedFieldDecl.namedFieldManager get() = MyFieldPsiElementManager(this)
val RsOuterAttr.outerAttrManager get() = MyRsOuterAttrPsiElementManager(this)
val JScrollPane.removeBorder get() = this.apply { this.border = BorderFactory.createEmptyBorder(0, 0, 0, 0) }
val JPanel.padding get() = this.apply { this.border = BorderFactory.createEmptyBorder(12, 12, 12, 12) }
val JPanel.vertical get() = this.apply { this.border = BorderFactory.createEmptyBorder(12, 0, 12, 0) }

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


///将字符串变为标准的rust struct name
val String.structName get() = firstCharToLowercase(this).underlineToCamel.capitalizeFirstLetter()

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

///尝试获取Rs struct 模型
fun AnActionEvent.tryGetRsStructPsiElement(): RsStructItemImpl? {
    val psiElement = this.getData(CommonDataKeys.PSI_ELEMENT)
    if (psiElement is RsStructItemImpl) {
        return psiElement
    }
    val firstParent = PsiTreeUtil.findFirstParent(psiElement) { it is RsStructItemImpl }
    return firstParent as? RsStructItemImpl
}

fun String.copy() {
    CopyPasteManager.getInstance().setContents(StringSelection(this))
}

inline fun <reified T : PsiElement> PsiElement.filterByType(): List<T> {
    return PsiTreeUtil.findChildrenOfAnyType(this, T::class.java).toList()
}

inline fun <reified T : PsiElement> PsiElement.findByTypeAndText(text: String): T? {
    return filterByType<T>().find { it.text == text }
}

/**
 * 递归查找符合[T]类型的第一个元素
 */
inline fun <reified T : PsiElement> PsiElement.findFirstChild(): T? {
    return try {
        PsiTreeUtil.findChildOfType(this, T::class.java)
    } catch (e: Exception) {
        null
    }
}

fun PsiElement.findLastLeafChild(): PsiElement? {
    return filterByType<LeafPsiElement>().lastOrNull()
}


/**
 * 查找第一个匹配的[T]父元素
 */
inline fun <reified T : PsiElement> PsiElement.findFirstParentChild(): PsiElement? {
    return PsiTreeUtil.findFirstParent(this) {
        return@findFirstParent it is T
    }
}

/**
 * 查找[element]在文件中的引用列表
 */
fun PsiFile.getUseAge(element: PsiElement): List<PsiReference> {
    return ReferencesSearch.search(element, LocalSearchScope(this)).findAll().toList()
}

/**
 * 尝试跳转到代码为止
 */
fun PsiElement.tryNavTo() {
    PsiNavigateUtil.navigate(this,true)
}


/**
 * 验证[path]是否存在,可以是目录或者文件
 */
 fun fileIsExits(path: String): Boolean {
    val vf = LocalFileSystem.getInstance().findFileByPath(path)
    return vf != null
}