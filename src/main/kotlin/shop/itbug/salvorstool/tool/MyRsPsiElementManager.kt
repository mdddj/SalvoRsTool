package shop.itbug.salvorstool.tool

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import com.intellij.psi.util.PsiTreeUtil
import org.rust.lang.core.psi.RsMetaItem
import org.rust.lang.core.psi.RsNamedFieldDecl
import org.rust.lang.core.psi.RsOuterAttr
import org.rust.lang.core.psi.ext.elementType
import org.rust.lang.core.psi.ext.stringValue
import org.rust.lang.core.psi.impl.RsNamedFieldDeclImpl
import org.rust.lang.core.psi.impl.RsPathTypeImpl
import org.rust.lang.core.psi.impl.RsStructItemImpl
import org.rust.lang.doc.psi.RsDocTokenType
import org.rust.lang.doc.psi.impl.RsDocCommentImpl

val JavascriptType.typeScriptText
    get() = when (this) {
        JavascriptType.Number -> "number"
        JavascriptType.String -> "string"
        JavascriptType.Bool -> "bool"
        JavascriptType.Unknown -> "any"
    }

//rs属性类型对应的js类型
enum class JavascriptType {
    Number, String, Bool, Unknown
}


/// struct操作管理
class MyRsStructManager(private val psiElement: RsStructItemImpl) {


    ///属性列表
    val fieldList: List<RsNamedFieldDecl> =
        psiElement.blockFields?.namedFieldDeclList
            ?: emptyList<RsNamedFieldDeclImpl>()

    ///获取struct名称
    val structName: String? = psiElement.name

    ///获取表名
    val getTableName: String?
        get() {
            val outerAttr = psiElement.outerAttrList.find { it.outerAttrManager.getSeaOrmTabName != null }
                ?: return null
            val tabName = outerAttr.outerAttrManager.getSeaOrmTabName
            return tabName
        }

    ///主键字段
    val primaryField = fieldList.find { it.namedFieldManager.isPrimaryKey }

    ///js 模型列表
    val jsModelList: List<MyFieldPsiElementManager.JsModel> =
        fieldList.mapNotNull { it.namedFieldManager.getJsModel }

    ///获取ts模型
    val getTSInterface: String get() {
        val sb = StringBuilder()
        sb.appendLine("interface $structName {")
        jsModelList.forEach {
            sb.appendLine("\t${it.propTextString},")
        }
        sb.appendLine("}")
        return sb.toString()
    }

    /// 生成antd table 列
    val getAntdTableColumnDefine: String get() {
        val sb = StringBuilder()
        sb.appendLine("[")
        val ls = jsModelList
        ls.map {
            sb.append(it.antdTableColumnItem(it == ls.last()))
        }
        sb.appendLine("]")
        return sb.toString()
    }

    val getHookForm: String get() {
        val sb = StringBuilder()
        jsModelList.forEach {
            sb.appendLine(it.hookFormItem())
        }
        var temp = FilesUtil.loadTemplateFileList("temps/ts_dialog_warp.tsx")
        temp = temp.replace("{model}",getTSInterface).replace("Model",structName?:"Model").replace("{fields}",sb.toString())
        return temp
    }
}


///属性处理
class MyFieldPsiElementManager(private val psiElement: RsNamedFieldDecl) {

    ///是否为主键的字段
    val isPrimaryKey = hasMetaItem("sea_orm") { it.text == "primary_key" }

    ///参数字段
    val name: String? = psiElement.name

    ///获取注释
    private val comment: String?
        get() {
            //判断//
            val comm = PsiTreeUtil.getChildOfType(psiElement, PsiCommentImpl::class.java)
            if (comm != null) {
                return comm.text.replace("//", "")
            }
            val docPsi = PsiTreeUtil.getChildOfType(psiElement, RsDocCommentImpl::class.java)
            //判断///
            if (docPsi != null) {
                val last = PsiTreeUtil.lastChild(docPsi)
                if (last.elementType is RsDocTokenType) {
                    return last.text
                }
            }
            return null
        }

    ///参数类型文本
    val typeString: String?
        get() {
            if (isOption) {
                return extractTextBetweenBrackets(typePsiText?:"")
            }
            return psiElement.typeReference?.text
        }


    private val getPathTypeImpl = PsiTreeUtil.findChildOfType(
        psiElement,
        RsPathTypeImpl::class.java
    )

    private val typePsiText = getPathTypeImpl?.text

    ///判断是否为可空的属性,比如Option<i32> return true
    private val isOption: Boolean get() = typePsiText?.startsWith("Option<") == true && typePsiText.endsWith(">")


    ///获取字段文本(除了宏以外)
    val getSimpleText: String
        get() {
            var text = psiElement.text
            val metas = psiElement.outerAttrList
            if (metas.isNotEmpty()) {
                metas.forEach { meta -> text = text.replace(meta.text, "").replace("\n", "").trim() }
            }
            return text
        }

    //获取JavaScript类型
    private val javaScriptType: JavascriptType
        get() {
            if (typeString == null) {
                return JavascriptType.Unknown
            }
            if (isIntegerType(typeString!!) == true) {
                return JavascriptType.Number
            }
            if (isStringType(typeString!!) == true) {
                return JavascriptType.String
            }
            if (typeString == "bool") {
                return JavascriptType.Bool
            }
            return JavascriptType.Unknown
        }


    ///查找meta
    fun hasMetaItem(filter: (item: RsMetaItem) -> Boolean): Boolean {
        val outerAttrList = psiElement.outerAttrList
        outerAttrList.forEach { metas ->
            val args = metas.metaItem.metaItemArgs
            args?.metaItemList?.forEach { meta ->
                run {
                    val result = filter(meta)
                    if (result) {
                        return true
                    }
                }
            }
        }
        return false
    }

    ///查找meta,比较精确的查找
    private fun hasMetaItem(name: String, filter: (item: RsMetaItem) -> Boolean): Boolean {
        val find = psiElement.outerAttrList.find { it.outerAttrManager.isMeta(name) } ?: return false
        val args = find.metaItem.metaItemArgs
        args?.metaItemList?.forEach { meta ->
            run {
                val result = filter(meta)
                if (result) {
                    return true
                }
            }
        }
        return false
    }

    //判断是整形
    private fun isIntegerType(rustType: String): Boolean? {
        val integerRegex = Regex("""^(i|u)\d+$""")
        return if (rustType.matches(integerRegex)) {
            true
        } else {
            null
        }
    }

    //判断是字符串
    private fun isStringType(rustType: String): Boolean? {
        return if (rustType == "String" || rustType == "&str") {
            true
        } else {
            null
        }
    }

    data class JsModel(val type: JavascriptType, val fieldName: String, val comment: String?, val isOption: Boolean)

    val getJsModel: JsModel?
        get() {
            if (javaScriptType == JavascriptType.Unknown) {
                return null
            } else if (name == null) {
                return null
            }
            return JsModel(javaScriptType, name, comment = comment, isOption = isOption)
        }

    private fun extractTextBetweenBrackets(input: String): String? {
        val regex = "<([^>]*)>".toRegex()
        val matchResult = regex.find(input)
        return if (matchResult != null) {
            matchResult.groupValues[1]
        } else {
            null
        }
    }
}


/// interface 字段
val MyFieldPsiElementManager.JsModel.propTextString: String
    get() {
        if (this.isOption) {
            return "${fieldName}: ${type.typeScriptText} | undefined"
        }
        return "${fieldName}: ${type.typeScriptText}"
    }

/// antd 表格字段
fun MyFieldPsiElementManager.JsModel.antdTableColumnItem(isLast: Boolean): String
     {
        val sb = StringBuilder()
        sb.appendLine("{")
        sb.appendLine("\tdataIndex: '${fieldName}', ")
        sb.appendLine("\ttitle: '${comment ?: fieldName}',")
        sb.appendLine("\tkey: '${fieldName}'")
        sb.appendLine("}${if (isLast) "" else ","}")
        return "$sb"
    }


/// react hook form
fun MyFieldPsiElementManager.JsModel.hookFormItem() : String {
    val sb = StringBuilder()

    val requiredString = if(this.isOption){
        ""
    } else {
        "rules={{ required: '请输入${this.comment}' }}"
    }

    when(this.type){
        JavascriptType.Number -> {
            sb.appendLine("""
                <Controller render={function({ field, fieldState: { error } }) {
            return <InputWrapper label={'${this.comment}'} bottomLeftLabel={error?.message}>
              <input type={'number'} {...field} {...register("${this.fieldName}")} className={get_input_class(error?.message)} placeholder={'${this.comment}'}  />
            </InputWrapper>;
          }} name={'name'} control={control} $requiredString />
            """.trimIndent())
        }
        JavascriptType.String -> {
            sb.appendLine("""
                <Controller render={function({ field, fieldState: { error } }) {
            return <InputWrapper label={'${this.comment}'} bottomLeftLabel={error?.message}>
              <input type={'text'} {...field} {...register("${this.fieldName}")} className={get_input_class(error?.message)} placeholder={'${this.fieldName}'}  />
            </InputWrapper>;
          }} name={'name'} control={control} $requiredString />
            """.trimIndent())
        }
        JavascriptType.Bool -> {

        }
        JavascriptType.Unknown -> {}
    }
    return sb.toString()
}

class MyRsOuterAttrPsiElementManager(private val psiElement: RsOuterAttr) {


    private val args = psiElement.metaItem.metaItemArgs
    private val argItems = args?.metaItemList ?: emptyList()
    val getSeaOrmTabName = getArgString("sea_orm", "table_name")

    ///判断是不是某个宏
    fun isMeta(name: String): Boolean {
        return psiElement.metaItem.path?.text == name
    }


    ///查找meta,比较精确的查找
    private fun hasMetaItem(name: String, filter: (item: RsMetaItem) -> Boolean): Boolean {
        if (!isMeta(name)) return false
        argItems.forEach { meta ->
            run {
                val result = filter(meta)
                if (result) {
                    return true
                }
            }
        }
        return false
    }

    private fun getArgPsiElement(name: String, attr: String): RsMetaItem? {
        if (!hasMetaItem(name) { it.path?.text == attr }) return null
        argItems.forEach {
            if (it.path?.text == attr) {
                return it
            }
        }
        return null
    }

    /**
     * 获取属性文本值
     *
     * 例子: #[sea_orm(table_name = "users")]
     * 传参: "sea_orm","table_name"
     * 返回: "users"
     */
    private fun getArgString(name: String, attr: String): String? {
        val psi = getArgPsiElement(name, attr)
        if (psi != null) {
            return psi.litExpr?.stringValue
        }
        return null
    }
}

