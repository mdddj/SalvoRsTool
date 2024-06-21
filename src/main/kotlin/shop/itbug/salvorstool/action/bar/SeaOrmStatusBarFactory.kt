package shop.itbug.salvorstool.action.bar

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup

class SeaOrmStatusBarFactory {
    companion object {

        fun getActionGroup(): DefaultActionGroup {
            return ActionManager.getInstance().getAction("SeaOrmStatusBarActions") as DefaultActionGroup
        }
    }
}