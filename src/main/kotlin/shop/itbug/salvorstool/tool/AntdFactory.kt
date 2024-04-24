package shop.itbug.salvorstool.tool

import org.rust.lang.core.psi.impl.RsStructItemImpl


sealed class Rule
data class AntdRequiredRule(val message: String, val required: Boolean) : Rule()

private fun Rule.generateString(): String {
    return when (this) {
        is AntdRequiredRule -> "{ message: '$message', required: ${if (required) "true" else "false"}}"
    }
}


object AntdFactory {


    ///生成form
    fun generateAntdForm(psiElement: RsStructItemImpl): String {
        val sb = StringBuilder()
        val manager = psiElement.myManager
        val jsModels = manager.jsModelList

        //1.添加参数
        sb.appendLine(
            """
            type Prop = {
              trigger?: JSX.Element | undefined,
              initValues?: PropInitValue | undefined
            }
        """.trimIndent()
        )

        //2.添加模型
        sb.appendLine(manager.getTSInterface)

        //3.添加field
        val fieldSb = StringBuilder()

        jsModels.forEach {
            fieldSb.appendLine("\t\t" + generateFormItem(it))
        }

        //4.添加最外层的包装
        sb.appendLine(getTemp(fieldSb.toString()))

        return sb.toString()
    }

    ///生成form item
    fun generateFormItem(model: MyFieldPsiElementManager.JsModel): String {
        val type = when (model.type) {
            JavascriptType.Number -> "ProFormDigit"
            JavascriptType.String -> "ProFormText"
            JavascriptType.Bool -> "ProFormSwitch"
            JavascriptType.Unknown -> ""
        }
        if (type.isEmpty()) {
            return ""
        }

        val rules = mutableListOf<Rule>()
        //添加rule
        if (!model.isOption) {
            rules.add(AntdRequiredRule(message = "请输入${model.comment ?: "内容"}", required = true))
        }

        return "<$type name='${model.fieldName}' label='${model.comment ?: ""}' ${generateRulesText(rules)} \t\t/>"
    }

    ///生成规则
    private fun generateRulesText(rules: List<Rule>): String {
        if (rules.isEmpty()) {
            return ""
        }
        val sb = StringBuilder()
        sb.appendLine("rules={[")
        rules.forEach {
            sb.appendLine("\t\t\t\t\t" + it.generateString())
        }
        sb.appendLine("\t\t\t]}")
        return sb.toString()
    }

}


///模板引擎
private fun getTemp(field: String): String = """
const AddOrUpdateForm: React.FC<Prop> = (props) => {
  let isUpdate = props.initValues !== undefined
  //提交数据
  const onFinish = async (values: PropInitValue) => {
    if(isUpdate) {
      // Todo! 修改
    }else{
      // ToDo! 新增
    }
    return true
  }
  return (
    <ModalForm<PropInitValue> trigger={props.trigger} initialValues={props.initValues} onFinish={onFinish}>
      $field
    </ModalForm>
  );
};
export {AddOrUpdateForm}
""".trimIndent()