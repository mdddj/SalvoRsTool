package shop.itbug.salvorstool.tool

import com.intellij.openapi.project.Project
import org.rust.lang.core.psi.impl.RsLetDeclImpl
import shop.itbug.salvorstool.model.SalvoApiItem

val RsLetDeclImpl.rsLetDeclImplManager get() = RsLetDeclManager(this)

/**
 *  let 表达式
 *  例子:
 *  let product_router = Router::with_path("/api/product").get(get_product_all)
 *         .post(post_add_product)
 *         .push(
 *             Router::with_path("{id}")
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
    ///获取所有的api列表
    val allApi: List<SalvoApiItem>
        get() {
            return psi.parseSalvoApis()
        }

}
