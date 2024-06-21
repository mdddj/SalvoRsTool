package shop.itbug.salvorstool.bar

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.wm.IconLikeCustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.wm.impl.status.TextPanel
import com.intellij.ui.ClickListener
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.util.preferredHeight
import shop.itbug.salvorstool.action.bar.SeaOrmStatusBarFactory
import shop.itbug.salvorstool.icons.MyIcon
import java.awt.Point
import java.awt.event.MouseEvent
import javax.swing.Icon
import javax.swing.JComponent

class BottomStatusBar : StatusBarWidgetFactory {

    override fun getId(): String {
        return "SalvoToolBar"
    }

    override fun getDisplayName(): String {
        return "Salvo ToolBar"
    }

    override fun isAvailable(project: Project): Boolean {
        return true
    }

    override fun createWidget(project: Project): StatusBarWidget {
        return Widget(project)
    }
}


private class Widget(val project: Project) : TextPanel.WithIconAndArrows(), IconLikeCustomStatusBarWidget {



    private val clickListen = object : ClickListener() {
        override fun onClick(event: MouseEvent, clickCount: Int): Boolean {
            if (project.isDisposed) {
                return false
            }
            val pop = createPopupActionGroups()
            val conetxt = DataManager.getInstance().getDataContext(this@Widget)
            val comp = PlatformCoreDataKeys.CONTEXT_COMPONENT.getData(conetxt)
            val point = Point(0, -pop.content.preferredHeight)
            pop.show(RelativePoint(comp ?: this@Widget, point))
            return true
        }
    }

    private fun createPopupActionGroups(): ListPopup {
        val group = SeaOrmStatusBarFactory.getActionGroup()
        val conetxt = DataManager.getInstance().getDataContext(this)
        return JBPopupFactory.getInstance().createActionGroupPopup(
            "Tools",
            group,
            conetxt,
            true,
            {}, 10
        )
    }

    override fun ID(): String {
        return "Salvo ToolBar Widget"
    }

    override fun getComponent(): JComponent {
        return this
    }

    override var icon: Icon?
        get() = MyIcon.pluginIcon
        set(_) {}


    override fun install(statusBar: StatusBar) {
        if (project.isDisposed) {
            return
        }
        clickListen.installOn(this, true)
        super.install(statusBar)
    }

}