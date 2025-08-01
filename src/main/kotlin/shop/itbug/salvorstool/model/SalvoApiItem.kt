package shop.itbug.salvorstool.model

import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.search.GlobalSearchScope
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

data class SalvoApiItemFunction(val method: SalvoApiItemMethod, val element: SmartPsiElementPointer<RsMethodCall>)

data class SalvoApiItem(
    val api: String,
    val method: SalvoApiItemMethod,
    val rsMethodPsiElement: SmartPsiElementPointer<RsMethodCall>,
    val routerFileName: String,
) {
    override fun toString(): String {
        return "\n${api} - $method"
    }

    // get
    fun getGetApiFunctionName(): String {
        return "${generateRequestName()}GetAllApi"
    }

    //post
    fun getPostApiFunctionName(): String {
        return "${generateRequestName()}PostApi"
    }

    //put
    fun getPutApiFunctionName(): String {
        return "${generateRequestName()}UpdateApi"
    }

    //delete
    fun getDeleteApiFunctionName(): String {
        return "${generateRequestName()}DeleteApi"
    }

    /**
     * 生成 antd request get 方法
     */
    fun apiWithFindAll(resultDtoName: String): String {

        return $"""
export async function ${getGetApiFunctionName()}() : Promise<ApiResult<$resultDtoName[]>>{
  return request(`${api}`,{
    method: 'GET',
  })
}
        """.trimIndent()
    }

    /**
     * 添加
     */
    fun apiWithAdd(dtoName: String): String {
        return $"""
export async function ${getPostApiFunctionName()}(data: $dtoName): Promise<ApiResult<$dtoName>> {
  return request(`${api}`, {
    method: 'POST',
    data: data,
  });
}
        """.trimIndent()
    }

    /**
     * 修改 API
     */
    fun apiWithUpdatePut(dtoName: String): String {
        val vs = formatVariables()
        return $"""
export async function ${getPutApiFunctionName()}(${if (vs.isEmpty()) "" else "$vs,"} data: $dtoName | undefined ): Promise<ApiResult<$dtoName>> {
  return request(`${replacePathParameters(api)}`, {
    method: 'PUT',
    data: data,
  });
}
        """.trimIndent()
    }

    /**
     * 删除 API
     */
    fun apiWithDelete(): String {
        return $"""
export async function ${getDeleteApiFunctionName()}(${formatVariables()}) : Promise<void>{
  return request(`${replacePathParameters(api)}`,{
    method: 'DELETE',
  })
}
        """.trimIndent()
    }

    /**
     * 生成antd request
     */
    fun generateAntdRequest(): String {
        val sb = StringBuilder()
        sb.appendLine("export async function ${generateRequestName()}(${generateFunctionParams()}) : Promise<any>{")
        sb.appendLine("\treturn request(`${replacePathParameters(api)}`,{")
        sb.appendLine("\t\tmethod: '${method.name.uppercase(Locale.getDefault())}',")
        sb.appendLine("\t})")
        sb.appendLine("}")
        return sb.toString()
    }

    /**
     * 函数名称
     */
    private fun generateRequestName(): String {
        var name = removeIdWithSlash(api).replace("/", "_").underlineToCamel.capitalizeFirstLetter()
        name =  firstCharToLowercase(name)
        val regex = Regex("\\{.*?\\}") // 匹配任意 {xxx} 结构
        return name.replace(regex, "")
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
    private fun replacePathParameters(url: String): String {
        return url.replace(Regex("\\{(\\w+)\\}")) { matchResult ->
            "\${${matchResult.groupValues[1]}}"
        }
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
     * 跳到代码位置
     */
    fun navTo() {
        rsMethodPsiElement.element?.tryNavTo()
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
    fun getServiceRefs() = findServiceRef(rsMethodPsiElement.element)

    /**
     * 获取 api路径所有变量
     */
    fun extractPathVariables(): List<String> {
        val regex = Regex("\\{(\\w+)\\}")
        return regex.findAll(api)
            .map { it.groupValues[1] }
            .toList()
    }

    /**
     * 格式化为 ts函数入参
     */
    fun formatVariables(): String {
        val variables = extractPathVariables()
        return if (variables.isEmpty()) {
            ""
        } else {
            variables.joinToString(", ") { "${it}: string" }
        }
    }

    companion object {

        /**
         * 查找指向service
         */
        fun findServiceRef(rsMethod: RsMethodCall?): RsNamedElement? {
            val rsServiceFun = rsMethod?.findFirstChild<RsPathImpl>() ?: return null
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
