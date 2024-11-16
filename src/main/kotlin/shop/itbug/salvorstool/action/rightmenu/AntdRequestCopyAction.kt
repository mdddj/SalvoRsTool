package shop.itbug.salvorstool.action.rightmenu

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import shop.itbug.salvorstool.i18n.MyI18n
import shop.itbug.salvorstool.tool.MyDataKey
import shop.itbug.salvorstool.tool.copy

/**
 * 拷贝antd request 请求
 */
class AntdRequestCopyAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val item = e.getData(MyDataKey.JListSelectItemDataKey)!!
        val str =  item.generateAntdRequest()
        str.copy()
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.text = "${MyI18n.getMessage("copy")} Antd Request"
        e.presentation.icon = AllIcons.Actions.Copy
          e.presentation.isEnabled = e.getData(MyDataKey.JListSelectItemDataKey) != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
