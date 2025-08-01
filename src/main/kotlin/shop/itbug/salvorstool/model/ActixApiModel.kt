package shop.itbug.salvorstool.model

import com.intellij.openapi.module.Module
import com.intellij.psi.SmartPsiElementPointer
import org.rust.lang.core.psi.RsFile
import org.rust.lang.core.psi.RsFunction

/** actix api 模型 */
class ActixApiModel(
    val url: String,
    val method: ActixApiModelMethod,
    val functionElement: SmartPsiElementPointer<RsFunction>? = null,
    val comment: String? = null,
    val psiFile: RsFile? = null,
    val module: Module? = null
)

enum class ActixApiModelMethod {
    Get,
    Post,
    Update,
    Delete,
    Put,
    Patch,
    Head,
    Unknown
}
