package shop.itbug.salvorstool.templete

import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateEditingAdapter
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateState
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class BaseTest : BasePlatformTestCase() {


    val listener = object : TemplateEditingAdapter() {
        override fun currentVariableChanged(
            templateState: TemplateState,
            template: Template?,
            oldIndex: Int,
            newIndex: Int
        ) {
            println("进来了:$newIndex $oldIndex ${template?.variables?.size} ${template?.variables?.getOrNull(templateState.currentVariableNumber)}")
            // 当变量激活时设置值
            if (newIndex >= 0 && template != null) {
                val variableName = template.variables[newIndex].name
                println("Variable changed to: $variableName")

            }
        }
    }


    fun testTemp() {

        myFixture.configureByText("test.kt", "<caret>")

        val manager = TemplateManager.getInstance(project)
        val template: Template = manager.createTemplate(
            "myTemplate", "user", $$$"""
            fun main() {
             println("$FIRST$, $NAME$!")
            }
        """.trimIndent()
        )
        template.addVariable("NAME", "", "", true)
        val editor = myFixture.editor
        val params = mapOf("NAME" to "World", "FIRST" to "Hello")

        WriteCommandAction.runWriteCommandAction(project) {
            manager.startTemplate(
                editor,
                template,
                true,
                params,
                listener
            )
        }

        myFixture.checkResult("fun main() {\n println(\"Hello, World!\")\n}")
        println(editor.document.text)
    }

}