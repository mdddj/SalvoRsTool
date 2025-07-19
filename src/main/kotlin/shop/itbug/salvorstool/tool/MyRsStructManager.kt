package shop.itbug.salvorstool.tool

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
import shop.itbug.salvorstool.tool.funs.RsStructItemFunBase


/**
 * rust struct操作管理
 */
class MyRsStructManager(private val psiElement: RsStructItemImpl) : RsStructItemFunBase(psiElement) {

    ///属性列表
    val fieldList: List<RsNamedFieldDecl> =
        psiElement.blockFields?.namedFieldDeclList
            ?: emptyList<RsNamedFieldDeclImpl>()

    ///获取struct名称
    val structName: String? = psiElement.name


    /// ts模型名字，eg MyModel
    val tsModelName: String
        get() {
            return (getTableName?.capitalizeFirstLetter() ?: "") + "Model"
        }

    ///获取表名
    val getTableName: String?
        get() {
            val outerAttr =
                psiElement.outerAttrList.find { it.outerAttrManager.getSeaOrmTabName != null } ?: return null
            val tabName = outerAttr.outerAttrManager.getSeaOrmTabName
            if (tabName != null && tabName.isNotBlank()) {
                return tabName
            }
            return structName
        }

    ///主键字段 (sea-orm)
    val primaryField = fieldList.find { it.namedFieldManager.isPrimaryKey }

    ///js 模型列表
    val jsModelList: List<MyFieldPsiElementManager.JsModel> =
        fieldList.mapNotNull { it.namedFieldManager.getJsModel }
    val primaryKeyFieldString = jsModelList.find { it.isPrimaryKey }?.fieldName ?: "id"


    ///获取ts模型
    val getTSInterface: String
        get() {
            val sb = StringBuilder()
            sb.appendLine("interface $structName {")
            jsModelList.forEach {
                sb.appendLine("\t${it.propTextString},")
            }
            sb.appendLine("}")
            return sb.toString()
        }

    ///获取 ts模型 （代码生成）
    val getTSInterfaceWithCodegen: String
        get() {
            if (getTableName == null) return "未知的表名称"
            val sb = StringBuilder()
            sb.appendLine("export interface ${getTableName!!.capitalizeFirstLetter()}Model {")
            jsModelList.forEach {
                sb.appendLine("\t${it.propTextString},")
            }
            sb.appendLine("}")
            return sb.toString()
        }

    /// 生成antd table 列
    val getAntdTableColumnDefine: String
        get() {
            val sb = StringBuilder()
            sb.appendLine("[")
            val ls = jsModelList
            ls.map {
                sb.append(it.antdTableColumnItem(it == ls.last()))
            }
            sb.appendLine("]")
            return sb.toString()
        }


    /// 生成antd table 列
    val getAntdTableColumnDefineV2: String
        get() {
            val sb = StringBuilder()
            sb.appendLine("[")
            val ls = jsModelList
            ls.map {
                sb.append(it.antdTableColumnItem(it == ls.last()))
            }
            sb.appendLine(
                $"""
,{
    dataIndex: 'actions',
    title: '操作',
    key: 'actions',
    render: (dom, entity, index, action) => {
      return <Space>
        <AddOrUpdateForm trigger={<Button size={'small'} >编辑</Button>} initValues={entity} onSuccess={action?.reload} />
        <Popconfirm title={'确定删除吗?'} onConfirm={ async () => {
          try{
            await apiIdeaPluginDeleteApi(`${'$'}{entity.${primaryKeyFieldString}}`)
            message.success("删除成功")
            action?.reload();
          }catch (e) {
            message.error(`${'$'}{e}`)
          }
        }}>
          <Button type={'default'} size={'small'} color={'danger'}>删除</Button>
        </Popconfirm>
      </Space>
    }
}
            """.trimIndent()
            )
            sb.appendLine("]")
            return sb.toString()
        }


    ///==============================代码生成 index.tsx

    // table column 配置
    fun genWithTableColumnDefine(): String {
        return $"""
            const columns: ProColumns<$tsModelName>[] = $getAntdTableColumnDefineV2
        """.trimIndent()
    }


    //请求列表 api
    fun genRequestListApiDefine(apiName: String): String {
        return $"""
        async () => {
          const result = await $apiName();
          return {
            data: result.data,
            success: true,
            total: result.data.length,
          };
        }
        """.trimIndent()
    }

    //生成 index组件
    fun genExportDefaultIndex(listApiName: String): String {
        val table = """
<ProTable<$tsModelName>
        actionRef={actionRef}
      search={false}
      request={
        ${genRequestListApiDefine(listApiName)}
      }
      columns={columns}
    >
    </ProTable>
        """.trimIndent()
        return $"""
export default function Index() {
    const actionRef = useRef<ActionType>(undefined);
  return <PageContainer title="列表">
     <Flex gap={'middle'} vertical={true}>
      <Flex align="center" gap="middle">
        <AddOrUpdateForm trigger={<Button type="primary">新增</Button>} onSuccess={actionRef?.current?.reload} />
      </Flex>
      $table
    </Flex>
  </PageContainer>;
}
        """.trimMargin()
    }

    //生成首页代码
    fun genIndexPageString(listApiName: String,deleteApi:String): String {
        return $"""
    import { ActionType,PageContainer, ProColumns, ProTable } from '@ant-design/pro-components';
    import { $tsModelName } from './Model';
    import { $listApiName,$deleteApi } from './Api';
    import { Button,Flex,message, Popconfirm, Space } from 'antd';
    import {AddOrUpdateForm} from './add_or_update';
    import { useRef } from 'react';
    
    ${genWithTableColumnDefine()}
    
    ${genExportDefaultIndex(listApiName)}
        """.trimMargin()
    }

    ///==============================代码生成 index.tsx ending


    ///==============================代码生成 新增&编辑.tsx

    fun genWithEditOrAddString(addApi: String, updateApi: String): String {
        val prop = $"""
            type Prop = {
              trigger?: JSX.Element | undefined,
              initValues?: $tsModelName | undefined,
              onSuccess?: () => void,
            }
        """.trimIndent()



        return $"""
import React, { JSX } from 'react';
import { $tsModelName } from './Model';
import { ModalForm, ProFormDigit, ProFormText } from '@ant-design/pro-form';
import { $addApi, $updateApi } from './Api';
import { message } from 'antd';

$prop


const AddOrUpdateForm: React.FC<Prop> = ({ initValues, trigger, onSuccess }) => {
  let isUpdate = initValues !== undefined;
  //提交数据
  const onFinish = async (values: $tsModelName) => {
    try {
      if (isUpdate && initValues) {
        const {msg} = await $updateApi(`${'$'}{initValues.$primaryKeyFieldString}`, values);
        message.success(msg);
      } else {
        const {msg} =  await $addApi(values);
        message.success(msg);
      }
      onSuccess?.();
      return true;
    } catch (err) {
      message.error(`${'$'}{err}`);
      return false;
    }
  };
  return (
    <ModalForm<$tsModelName> trigger={trigger} initialValues={initValues} onFinish={onFinish} modalProps={{
      destroyOnClose: true
    }}>
     ${jsModelList.filter { it.isPrimaryKey.not() }.joinToString("\n") { it.antdFormItem() }}
    </ModalForm>
  );
};
export { AddOrUpdateForm };
        """.trimIndent()
    }

}


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
                return extractTextBetweenBrackets(typePsiText ?: "")
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

    data class JsModel(
        val type: JavascriptType, val fieldName: String, val comment: String?, val isOption: Boolean,
        //是否为主键
        val isPrimaryKey: Boolean = false
    ) {
        fun antdFormItem(): String {
            return AntdFactory.generateFormItem(this)
        }
    }


    val getJsModel: JsModel?
        get() {
            if (javaScriptType == JavascriptType.Unknown) {
                return null
            } else if (name == null) {
                return null
            }
            return JsModel(
                javaScriptType,
                name,
                comment = comment,
                isOption = isOption,
                isPrimaryKey = this.isPrimaryKey
            )
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
fun MyFieldPsiElementManager.JsModel.antdTableColumnItem(isLast: Boolean): String {
    val sb = StringBuilder()
    sb.appendLine("{")
    sb.appendLine("\tdataIndex: '${fieldName}', ")
    sb.appendLine("\ttitle: '${comment ?: fieldName}',")
    sb.appendLine("\tkey: '${fieldName}'")
    sb.appendLine("}${if (isLast) "" else ","}")
    return "$sb"
}


/// react hook form
fun MyFieldPsiElementManager.JsModel.hookFormItem(): String {
    val sb = StringBuilder()

    val requiredString = if (this.isOption) {
        ""
    } else {
        "rules={{ required: '请输入${this.comment}' }}"
    }

    when (this.type) {
        JavascriptType.Number -> {
            sb.appendLine(
                """
                <Controller render={function({ field, fieldState: { error } }) {
            return <InputWrapper label={'${this.comment}'} bottomLeftLabel={error?.message}>
              <input type={'number'} {...field} {...register("${this.fieldName}")} className={get_input_class(error?.message)} placeholder={'${this.comment}'}  />
            </InputWrapper>;
          }} name={'name'} control={control} $requiredString />
            """.trimIndent()
            )
        }

        JavascriptType.String -> {
            sb.appendLine(
                """
                <Controller render={function({ field, fieldState: { error } }) {
            return <InputWrapper label={'${this.comment}'} bottomLeftLabel={error?.message}>
              <input type={'text'} {...field} {...register("${this.fieldName}")} className={get_input_class(error?.message)} placeholder={'${this.fieldName}'}  />
            </InputWrapper>;
          }} name={'name'} control={control} $requiredString />
            """.trimIndent()
            )
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

