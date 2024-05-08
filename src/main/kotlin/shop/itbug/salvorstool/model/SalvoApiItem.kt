package shop.itbug.salvorstool.model

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.search.searches.ReferencesSearch
import org.rust.lang.core.psi.RsMethodCall
import shop.itbug.salvorstool.tool.*
import java.util.*


enum class SalvoApiItemMethod {
    Get,
    Post,
    Update,
    Delete,
    Put
}

data class SalvoApiItem(val api: String,val method: SalvoApiItemMethod,val rsMethodPsiElement: RsMethodCall){
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
        val name = removeIdWithSlash(api).replace("/","_").underlineToCamel.capitalizeFirstLetter()
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
    private fun generateFunctionParams() : String {
        val params = extractBracketContents(api)
        if(params.isNotEmpty()){
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
    fun navTo(project: Project) {
        val navigationElement = rsMethodPsiElement.navigationElement
        ApplicationManager.getApplication().invokeLater {
            if (navigationElement != null && navigationElement is Navigatable && (navigationElement as Navigatable).canNavigate()) {
                (navigationElement as Navigatable).navigate(true)
            }else{
                FileEditorManager.getInstance(project).openFile(rsMethodPsiElement.containingFile.virtualFile)
            }
        }
    }
}
