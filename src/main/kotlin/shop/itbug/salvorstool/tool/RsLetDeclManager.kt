package shop.itbug.salvorstool.tool

import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import org.rust.lang.core.psi.impl.*
import shop.itbug.salvorstool.model.SalvoApiItem

val RsLetDeclImpl.rsLetDeclImplManager get() = RsLetDeclManager(this)

/**
 *  let 表达式
 *  例子:
 *  let product_router = Router::with_path("/api/product").get(get_product_all)
 *         .post(post_add_product)
 *         .push(
 *             Router::with_path("<id>")
 *                 .put(put_update_product)
 *                 .delete(delete_product),
 *         );
 */


data class LetModel(
    ///表达式名称 -> product_router
    val letName: String? = null
)


class RsLetDeclManager(val psi: RsLetDeclImpl) {

    val project: Project = psi.project
    val getModel: LetModel
        get() {
            return LetModel(letName = psi.pat?.text)
        }

    /// 获取root ele 节点
    private val rootMethodExpr: RsMethodCallExprImpl?
        get() {
            val root = PsiTreeUtil.findChildOfType(psi, RsMethodCallExprImpl::class.java)
            return PsiTreeUtil.findChildOfType(root, RsMethodCallExprImpl::class.java) ?: root
        }


    ///获取所有的api列表
    val allApi: List<SalvoApiItem>
        get() {
            val result = mutableListOf<SalvoApiItem>()
            val root = rootMethodExpr ?: return emptyList()
            val rootManager = root.methodCallExprManager()
            val rootApi = rootManager.rootApiPath ?: return emptyList()
            val rootMethods = rootManager.getAllApiMethods
            //添加root path
            rootMethods.forEach {
                result.add(SalvoApiItem(rootApi, it.method, it.element, it.element.containingFile.name))
            }

            // 添加子URL
            allPushMethodCall.forEach {
                val manager = it.methodCallManager
                val methods = manager.allMethods
                methods.forEach { methodFun ->
                    val api = rootApi + "/" + manager.withPathString
                    result.add(
                        SalvoApiItem(
                            api,
                            methodFun.method,
                            methodFun.element,
                            methodFun.element.containingFile.name,
                        )
                    )
                }
            }
            return result.toList()
        }


    ///全部带有push的节点
    val allPushMethodCall: List<RsMethodCallImpl>
        get() {
            val allPushPsiElement = PsiTreeUtil.findChildrenOfType(psi, RsMethodCallImpl::class.java)
                .filter { it.methodCallManager.isPushMethodCall }
            return allPushPsiElement
        }


}
