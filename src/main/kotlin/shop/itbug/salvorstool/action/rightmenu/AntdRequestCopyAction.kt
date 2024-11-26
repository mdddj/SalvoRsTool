package shop.itbug.salvorstool.action.rightmenu

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import shop.itbug.salvorstool.i18n.MyI18n
import shop.itbug.salvorstool.icons.MyIcon
import shop.itbug.salvorstool.tool.SalvoApiActionHelper
import shop.itbug.salvorstool.tool.copy

/**
 * 拷贝antd request 请求
 */
class AntdRequestCopyAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val item = SalvoApiActionHelper.getSelectItem(e)!!
        val str =  item.generateAntdRequest()
        str.copy()
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.text = "${MyI18n.getMessage("copy")} Antd Request"
        e.presentation.icon = MyIcon.antd
          e.presentation.isEnabled = SalvoApiActionHelper.getSelectItem(e) != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
