package shop.itbug.salvorstool.action.seaorm

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import shop.itbug.salvorstool.dialog.GenerateEntityDialog
import shop.itbug.salvorstool.help.SeaOrmCommandHelp
import shop.itbug.salvorstool.i18n.MyI18n
import shop.itbug.salvorstool.icons.MyIcon
import javax.swing.JComponent


class SeamOrmCommandActionGroup: DefaultActionGroup() {

    override fun update(e: AnActionEvent) {
        val vf = e.getData(PlatformDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = vf != null && vf.isDirectory && e.project != null && vf.findChild("Cargo.toml") != null
        e.presentation.icon = MyIcon.pluginIcon
        super.update(e)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}

abstract class SeamOrmCommandBaseAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    fun AnActionEvent.getProj(): Project = this.project!!
    private fun AnActionEvent.getVf() = this.getData(CommonDataKeys.VIRTUAL_FILE)!!
    fun AnActionEvent.getSeaOrmHelper() = SeaOrmCommandHelp(getVf().path, getProj())
}

///初始化
class SeaOrmInitAction : SeamOrmCommandBaseAction() {
    override fun actionPerformed(e: AnActionEvent) {
        e.getSeaOrmHelper().migrateInit()
    }
}

///创建table
class SeaOrmCreateTableFileAction : SeamOrmCommandBaseAction() {
    override fun actionPerformed(e: AnActionEvent) {
        Dialog(e.getProj()) {
            e.getSeaOrmHelper().migrateGenerate(it.filename)
        }.show()
    }

    private class Dialog(project: Project, val ok: (model: Model) -> Unit) : DialogWrapper(project, true) {
        data class Model(var filename: String = "")

        val configModel = Model()

        init {
            super.init()
            title = "Seam Orm Create Table File"
        }

        override fun createCenterPanel(): JComponent {
            return panel {
                row(MyI18n.getMessage("file_name")) {
                    textField().bindText(configModel::filename)
                }
            }
        }

        override fun doOKAction() {
            super.doOKAction()
            ok.invoke(configModel)
        }
    }
}


/// 生成实体
class SeaOrmRunGenerateEntityAction : SeamOrmCommandBaseAction() {
    override fun actionPerformed(e: AnActionEvent) {
        GenerateEntityDialog(e.getProj()) { _, s ->
            run {
                e.getSeaOrmHelper().customRunCommand(*s.split(" ").toTypedArray())
            }
        }.show()
    }

}


/// up
class SeaOrmRunUpAction : SeamOrmCommandBaseAction() {
    override fun actionPerformed(e: AnActionEvent) {
        e.getSeaOrmHelper().runUp()
    }
}

/// Down
class SeaOrmRunDownAction : SeamOrmCommandBaseAction() {
    override fun actionPerformed(e: AnActionEvent) {
        e.getSeaOrmHelper().runDown()
    }
}

/// check status
class SeaOrmRunCheckStatusAction : SeamOrmCommandBaseAction() {
    override fun actionPerformed(e: AnActionEvent) {
        e.getSeaOrmHelper().checkStatus()
    }
}

/// check status
class SeaOrmRunFreshAction : SeamOrmCommandBaseAction() {
    override fun actionPerformed(e: AnActionEvent) {
        e.getSeaOrmHelper().runFresh()
    }
}

/// refresh
class SeaOrmRunRefreshAction : SeamOrmCommandBaseAction() {
    override fun actionPerformed(e: AnActionEvent) {
        e.getSeaOrmHelper().runRefresh()
    }
}

// reset
class SeaOrmRunResetAction : SeamOrmCommandBaseAction() {
    override fun actionPerformed(e: AnActionEvent) {
        e.getSeaOrmHelper().runReset()
    }
}

