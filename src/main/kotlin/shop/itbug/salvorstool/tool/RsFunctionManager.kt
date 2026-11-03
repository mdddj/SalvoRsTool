package shop.itbug.salvorstool.tool

import com.intellij.psi.util.PsiTreeUtil
import org.rust.lang.core.psi.RsBlock
import org.rust.lang.core.psi.RsTypeReference
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
        // 不能用 org.rust.lang.core.psi.ext.block：该扩展属性在 RustRover 2026.2 已从
        // RsFunctionKt 移到 RsFunctionImplMixinKt，编译期无感知、运行期 NoSuchMethodError。
        val block = PsiTreeUtil.getChildOfType(rsFunction, RsBlock::class.java) ?: return emptyList()
        return PsiTreeUtil.findChildrenOfType(block, RsLetDeclImpl::class.java).toList()
    }
}