package shop.itbug.salvorstool.window

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DataSink
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.UiDataProvider
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.*
import com.intellij.ui.components.JBList
import com.intellij.ui.speedSearch.SpeedSearchUtil
import com.intellij.util.ui.ListUiUtil
import shop.itbug.salvorstool.messageing.ApiScanMessaging
import shop.itbug.salvorstool.model.SalvoApiItem
import shop.itbug.salvorstool.service.SalvoApiService
import shop.itbug.salvorstool.tool.MyDataKey
import java.util.*
import javax.swing.*


object SalvoApiWindowFactory {

    fun installActions(salvoApiWindow: ApiScanWindow): JPanel {
        val actions = ActionManager.getInstance().getAction("SalvoApiActionList") as DefaultActionGroup
        return ToolbarDecorator.createDecorator(salvoApiWindow)
            .addExtraAction(actions)
            .createPanel()
    }

    fun create(project: Project): ApiScanWindow = ApiScanWindow(project)
}


/**
 * salvo api窗口
 */
class ApiScanWindow(myProject: Project) : JBList<SalvoApiItem>(), UiDataProvider, Disposable, ApiScanMessaging {

    private val busConnect = myProject.messageBus.connect()


    private val rightMenuAction =
        ActionManager.getInstance().getAction("SalvoApiRightMenuActionGroup") as DefaultActionGroup

    private var allApis = SalvoApiService.getInstance(myProject).getApiList()


    init {
        this.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        this.selectionMode = ListSelectionModel.SINGLE_SELECTION
        setExpandableItemsEnabled(false)
        ScrollingUtil.installActions(this)
        ListUiUtil.Selection.installSelectionOnRightClick(this)
        ListUiUtil.Selection.installSelectionOnFocus(this)
        cellRenderer = SalvoApiItemRender()
        PopupHandler.installPopupMenu(this, rightMenuAction, "Right Menu")
        TreeUIHelper.getInstance().installListSpeedSearch(this) { o -> o.api }
        busConnect.subscribe(ApiScanMessaging.TOPIC, this)
    }


    override fun uiDataSnapshot(sink: DataSink) {
        sink.set<SalvoApiItem>(MyDataKey.JListSelectItemDataKey, selectedValue)
    }

    override fun dispose() {
        println("salvo windows dispose")
        busConnect.disconnect()
    }

    override fun apiScanEnd(apiList: List<SalvoApiItem>) {
        println("api scan ended")
        allApis = apiList
        model = ItemModel(apiList)
    }


    //设置模型
    inner class ItemModel(list: List<SalvoApiItem>) : DefaultListModel<SalvoApiItem>() {
        init {
            addAll(list)
        }
    }

}


class SalvoApiItemRender() : ColoredListCellRenderer<SalvoApiItem>() {
    override fun customizeCellRenderer(
        list: JList<out SalvoApiItem>,
        value: SalvoApiItem?,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean
    ) {

        value?.let {
            icon = AllIcons.Nodes.Method
            append(it.api)
            appendTextPadding(15)
            ApplicationManager.getApplication().runReadAction {
                append(
                    ColoredText.builder().append(
                        "  (${it.rsMethodPsiElement.containingFile.name})",
                        SimpleTextAttributes.GRAY_ATTRIBUTES
                    )
                        .build()
                )
            }
            append(
                ColoredText.builder().append(
                    "  [${it.method.name.uppercase(Locale.getDefault())}]",
                    SimpleTextAttributes.GRAY_ATTRIBUTES
                )
                    .build()
            )
        }
        SpeedSearchUtil.applySpeedSearchHighlighting(list, this, false, selected)
    }

}
