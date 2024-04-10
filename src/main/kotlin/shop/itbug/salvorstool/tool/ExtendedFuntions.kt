package shop.itbug.salvorstool.tool

import com.intellij.psi.PsiElement
import org.rust.lang.core.psi.RsNamedFieldDecl
import org.rust.lang.core.psi.RsOuterAttr
import org.rust.lang.core.psi.impl.RsStructItemImpl
val PsiElement.myManager get() = MyRsPsiElementManager(this)
val RsStructItemImpl.myManager get() = MyRsStructManager(this)
val RsNamedFieldDecl.myManager get() = MyFieldPsiElementManager(this)
val RsOuterAttr.myManager get() = MyRsOuterAttrPsiElementManager(this)
fun String.capitalizeFirstLetter(): String {
    if (isEmpty()) {
        return this
    }
    return substring(0, 1).toUpperCase() + substring(1)
}