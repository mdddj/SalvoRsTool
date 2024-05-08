package shop.itbug.salvorstool.tool

import com.intellij.psi.util.PsiTreeUtil
import org.rust.lang.core.psi.RsTypeReference
import org.rust.lang.core.psi.ext.block
import org.rust.lang.core.psi.impl.RsFunctionImpl
import org.rust.lang.core.psi.impl.RsLetDeclImpl

val RsFunctionImpl.myManager: RsFunctionManager get() = RsFunctionManager(this)

///rs fun tool
class RsFunctionManager(private val rsFunction: RsFunctionImpl) {

    //获取返回类型
    private fun getReturnType() : RsTypeReference? {
        val returnType = rsFunction.retType ?: return null
        return returnType.typeReference
    }

    //判断是否为路由
    val isReturnRouter: Boolean get() = getReturnType()?.text == "Router"

    //获取所有的let表达式
    val allLet : List<RsLetDeclImpl> get(){
        val block = rsFunction.block ?: return emptyList()
        return PsiTreeUtil.findChildrenOfType(block, RsLetDeclImpl::class.java).toList()
    }
}