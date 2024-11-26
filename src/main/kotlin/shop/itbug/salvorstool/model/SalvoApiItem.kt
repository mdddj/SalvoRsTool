package shop.itbug.salvorstool.model

import com.intellij.psi.search.GlobalSearchScope
import org.rust.lang.core.psi.RsFunction
import org.rust.lang.core.psi.RsMethodCall
import org.rust.lang.core.psi.ext.RsNamedElement
import org.rust.lang.core.psi.impl.RsPathImpl
import org.rust.lang.core.stubs.index.RsNamedElementIndex
import shop.itbug.salvorstool.tool.*
import java.util.*


enum class SalvoApiItemMethod {
    Get,
    Post,
    Update,
    Delete,
    Put,
    Patch,
    Unknown
}

data class SalvoApiItemFunction(val method: SalvoApiItemMethod, val element: RsMethodCall)

data class SalvoApiItem(
    val api: String,
    val method: SalvoApiItemMethod,
    val rsMethodPsiElement: RsMethodCall,
    val routerFileName: String,
    val serviceFileName: String,
) {
    override fun toString(): String {
        return "\n${api} - $method"
    }


    /**
     * 生成antd request
     */
    fun generateAntdRequest(): String {
        val sb = StringBuilder()
        sb.appendLine("export async function ${generateRequestName()}(${generateFunctionParams()}) : Promise<any>{")
        sb.appendLine("\treturn request(`${replaceAngleBracketsWithDollarCurlyBrackets(api)}`,{")
        sb.appendLine("\t\tmethod: '${method.name.uppercase(Locale.getDefault())}',")
        sb.appendLine("\t})")
        sb.appendLine("}")
        return sb.toString()
    }

    /**
     * 函数名称
     */
    private fun generateRequestName(): String {
        val name = removeIdWithSlash(api).replace("/", "_").underlineToCamel.capitalizeFirstLetter()
        return firstCharToLowercase(name)
    }


    /**
     * 去除/<id>这种文本
     */
    private fun removeIdWithSlash(input: String): String {
        val pattern = """\/<[^>]+>""".toRegex()
        return pattern.replace(input, "")
    }

    /**
     * 获取url参数
     */
    private fun extractBracketContents(input: String): List<String> {
        val pattern = """<([^>]+)>""".toRegex()
        return pattern.findAll(input).map { it.groupValues[1] }.toList()
    }


    /**
     * 替换<id>->${id}
     */
    private fun replaceAngleBracketsWithDollarCurlyBrackets(input: String): String {
        val angleBracketsContentPattern = """<([^>]+)>""".toRegex()
        return angleBracketsContentPattern.replace(input) { "\${${it.groupValues[1]}}" }
    }

    /**
     * 生成参数列表
     */
    private fun generateFunctionParams(): String {
        val params = extractBracketContents(api)
        if (params.isNotEmpty()) {
            val sb = StringBuilder()
            params.forEach {
                sb.append("${it}: string,")
            }
            return sb.toString().removeSuffix(",")
        }
        return ""
    }


    /**
     * 获取节点的路径
     */
    fun getElementFilePath(): String {
        return rsMethodPsiElement.containingFile.virtualFile.path
    }


    /**
     * 跳到代码位置
     */
    fun navTo() {
        rsMethodPsiElement.tryNavTo()
    }

    /**
     * 跳转到service实现
     */
    fun navToRouterImpl() {
        getServiceRefs()?.tryNavTo()
    }


    /**
     * 查找services函数的实现
     */
    private fun getServiceRefs() = findServiceRef(rsMethodPsiElement)


    companion object {

        /**
         * 查找指向service
         */
        fun findServiceRef(rsMethod: RsMethodCall): RsNamedElement? {
            val rsServiceFun = rsMethod.findFirstChild<RsPathImpl>() ?: return null
            val project = rsServiceFun.project
            val psis = RsNamedElementIndex.Helper.findElementsByName(
                project,
                rsServiceFun.text,
                GlobalSearchScope.projectScope(project)
            )
            return psis.firstOrNull()
        }
    }
}
