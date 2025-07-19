package shop.itbug.salvorstool.window

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DataSink
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.UiDataProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.ui.*
import com.intellij.ui.components.JBList
import com.intellij.ui.speedSearch.SpeedSearchUtil
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListUiUtil
import shop.itbug.salvorstool.messageing.ApiScanMessaging
import shop.itbug.salvorstool.model.SalvoApiItem
import shop.itbug.salvorstool.service.SalvoApiService
import shop.itbug.salvorstool.tool.MyDataKey
import shop.itbug.salvorstool.tool.Tools
import java.util.*
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListSelectionModel


object SalvoApiWindowFactory {

    fun installActions(salvoApiWindow: ApiScanWindow): JPanel {
        val actions = ActionManager.getInstance().getAction("SalvoApiActionList") as DefaultActionGroup
        return ToolbarDecorator.createDecorator(salvoApiWindow)
            .addExtraAction(actions)
            .setScrollPaneBorder(Tools.emptyBorder())
            .setToolbarBorder(Tools.emptyBorder())
            .setPanelBorder(Tools.emptyBorder())
            .createPanel()
    }

    fun create(project: Project): ApiScanWindow = ApiScanWindow(project)

    fun getDefaultActions() =
        ActionManager.getInstance().getAction("SalvoApiRightMenuActionGroup") as DefaultActionGroup
}


/**
 * salvo api窗口
 */
class ApiScanWindow(private val myProject: Project) : JBList<SalvoApiItem>(), UiDataProvider, Disposable,
    ApiScanMessaging,
    DumbService.DumbModeListener {

    private val busConnect = myProject.messageBus.connect(this)

    private val rightMenuAction = SalvoApiWindowFactory.getDefaultActions()

    private var allApis = SalvoApiService.getInstance(myProject).getApiList()


    init {
        cellRenderer = SalvoApiItemRender(myProject)
        model = ItemModel(allApis)
        border = Tools.emptyBorder()
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        setExpandableItemsEnabled(false)
        ScrollingUtil.installActions(this)
        ListUiUtil.Selection.installSelectionOnRightClick(this)
        ListUiUtil.Selection.installSelectionOnFocus(this)
        PopupHandler.installPopupMenu(this, rightMenuAction, "Right Menu")
        TreeUIHelper.getInstance().installListSpeedSearch(this) { o -> o.api }
        busConnect.subscribe(ApiScanMessaging.TOPIC, this)
        busConnect.subscribe(DumbService.DUMB_MODE, this)


        setWhatsSalvo()
    }

    private fun setWhatsSalvo() {
        setEmptyText("No Salvo API found")
        emptyText.apply {
            appendLine(
                "What is salvo?",
                SimpleTextAttributes(SimpleTextAttributes.STYLE_HOVERED, JBUI.CurrentTheme.Link.Foreground.ENABLED)
            ) {
                BrowserUtil.browse("https://salvo.rs/")
            }
        }
    }


    override fun uiDataSnapshot(sink: DataSink) {
        sink[MyDataKey.JListSelectItemDataKey] = selectedValue
    }

    override fun dispose() {
        println("salvo windows dispose")
        busConnect.disconnect()
    }

    override fun apiScanEnd(apiList: List<SalvoApiItem>, refresh: Boolean) {
        println("api scan ended")
        allApis = apiList
        if (listIsEmpty()) {
            model = ItemModel(apiList)
        }

        if (refresh) {
            model = ItemModel(allApis)
        }

    }


    //设置模型
    inner class ItemModel(list: List<SalvoApiItem>) : DefaultListModel<SalvoApiItem>() {
        init {
            addAll(list)
        }
    }

    override fun exitDumbMode() {
        println("索引完毕")
        if (listIsEmpty()) {
            model = ItemModel(getApiList())
        }
        super.exitDumbMode()
    }

    private fun getApiList() = SalvoApiService.getInstance(myProject).getApiList()
    private fun listIsEmpty() = (model as? ItemModel)?.isEmpty != false
}


class SalvoApiItemRender(val project: Project) : ColoredListCellRenderer<SalvoApiItem>() {
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
            append(
                ColoredText.builder().append(
                    "  (${it.routerFileName})",
                    SimpleTextAttributes.GRAY_ATTRIBUTES
                )
                    .build()
            )
            append(
                ColoredText.builder().append(
                    "  [${it.method.name.uppercase(Locale.getDefault())}]",
                    SimpleTextAttributes.GRAY_ATTRIBUTES
                )
                    .build()
            )

        }

        val searchPopupIsShow = SearchEverywhereManager.getInstance(project).isShown
        if (searchPopupIsShow) {
            val text = SearchEverywhereManager.getInstance(project).currentlyShownUI.searchField.text
            if(text.isNotBlank()){
                val textRanges = text.toRegex().findAll(value?.api ?: "").map { it.range }
                    .map { TextRange(it.first, it.last) }.toList()
                SpeedSearchUtil.applySpeedSearchHighlighting(this, textRanges, selected)
            }

        } else {
            SpeedSearchUtil.applySpeedSearchHighlighting(list, this, false, selected)
        }


    }

}
