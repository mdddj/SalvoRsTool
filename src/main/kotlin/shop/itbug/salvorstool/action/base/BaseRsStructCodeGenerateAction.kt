package shop.itbug.salvorstool.action.base

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import org.rust.lang.core.psi.RsNamedFieldDecl
import org.rust.lang.core.psi.RsStructItem
import shop.itbug.salvorstool.tool.MyRsStructManager
import shop.itbug.salvorstool.tool.myManager
import shop.itbug.salvorstool.tool.structItemManager
import shop.itbug.salvorstool.tool.tryGetRsStructPsiElement

/**
 * 基础的rs struct 代码生成操作
 *
 * 1. 它仅在struct上面起作用
 */
abstract class BaseRsStructCodeGenerateAction : AnAction() {

    data class Model(
        val project: Project,
        val e: AnActionEvent,
        val psiElement: RsStructItem,
        val manager: MyRsStructManager,
        val name: String?,
        val props: List<RsNamedFieldDecl>
    )

    override fun actionPerformed(e: AnActionEvent) {
        val psiElement = e.tryGetRsStructPsiElement()!!
        val project = e.project!!
        val manager: MyRsStructManager = psiElement.structItemManager
        val name: String? = manager.structName
        val props: List<RsNamedFieldDecl> = manager.fieldList
        val model = Model(project, e, psiElement, manager, name, props)
        codeGenerateAction(model)
    }


    /**
     * 生成代码操作
     */
    abstract fun codeGenerateAction(model: Model)


    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = e.tryGetRsStructPsiElement() != null && e.project != null
        super.update(e)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }


}