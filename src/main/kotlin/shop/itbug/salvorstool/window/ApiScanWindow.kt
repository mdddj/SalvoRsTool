package shop.itbug.salvorstool.window

import com.intellij.icons.AllIcons
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.ColoredText
import com.intellij.ui.SearchTextField
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBList
import com.intellij.util.ui.components.BorderLayoutPanel
import shop.itbug.salvorstool.i18n.MyI18n
import shop.itbug.salvorstool.messageing.ApiScanMessaging
import shop.itbug.salvorstool.model.SalvoApiItem
import shop.itbug.salvorstool.service.SalvoApiService
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener



class ApiScanWindow(private val myProject: Project, toolWindow: ToolWindow) : BorderLayoutPanel(),
    ListSelectionListener {

    private val list = JBList<SalvoApiItem>().apply {
        this.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        this.selectionMode = ListSelectionModel.SINGLE_SELECTION
        this.addListSelectionListener(this@ApiScanWindow)
        this.addMouseListener(RightMenuAction())
        this.setDataProvider {
            if (it == JListSelectItemDataKey.name) {
                val index = this.getClientProperty(RightSelectKey) as? Int
                if (index != null) {
                    return@setDataProvider model.getElementAt(index)
                }
            }
            return@setDataProvider null
        }
    }

    private var allApis = SalvoApiService.getInstance(myProject).getApiList()
    private var searchTextField = MySearchTextField()
    private val actions = ActionManager.getInstance().getAction("SalvoApiActionList") as DefaultActionGroup
    private val toolbar = ActionManager.getInstance().createActionToolbar("Salvorstool Window", actions, true)

    companion object {
         val JListSelectItemDataKey = DataKey.create<SalvoApiItem?>("listSelectItemDataKey")
        const val RightSelectKey = "RightSelectKey"
    }

    init {
        toolbar.targetComponent = toolWindow.component
        addToTop(BorderLayoutPanel().apply {
            addToCenter(searchTextField)
            addToRight(toolbar.component)
        })
        addToCenter(JScrollPane(list).apply {
            this.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        })
        list.cellRenderer = SalvoApiItemRender()
        SwingUtilities.invokeLater {
            listenChange()
        }


    }


    private fun listenChange() {
        myProject.messageBus.connect().subscribe(ApiScanMessaging.TOPIC, object : ApiScanMessaging {
            override fun apiScanEnd(apiList: List<SalvoApiItem>) {
                allApis = apiList
                list.model = ItemModel(apiList)
            }
        })
    }


    override fun valueChanged(e: ListSelectionEvent?) {
        e?.let {
            if (!it.valueIsAdjusting) {
                val selectedIndex = list.selectedIndex
                if (selectedIndex != -1) {
                    list.selectedValue.navTo()
                }
            }
        }
    }

    fun startFilterApi(search: String) {
        val filterList = if (search.isEmpty()) {
            allApis
        } else {
            allApis.filter { it.api.contains(search) }
        }
        list.model = ItemModel(filterList)
    }


    private inner class MySearchTextField : SearchTextField(false), DocumentListener {

        init {
            addDocumentListener(this)
            textEditor.emptyText.text = MyI18n.getMessage("filter_salvo_api_list")
        }

        override fun insertUpdate(e: DocumentEvent?) {
            handle(e)
        }

        override fun removeUpdate(e: DocumentEvent?) {
            handle(e)
        }

        override fun changedUpdate(e: DocumentEvent?) {
            handle(e)
        }

        private fun handle(e: DocumentEvent?) {
            e?.document?.let {
                val text = it.getText(0, it.length)
                startFilterApi(text)
            }
        }

    }

    //设置模型
    inner class ItemModel(val list: List<SalvoApiItem>) : DefaultListModel<SalvoApiItem>() {
        init {
            addAll(list)
        }
    }

    //右键菜单操作
    private inner class RightMenuAction : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
            e?.let {
                if (SwingUtilities.isRightMouseButton(e)) {
                    val selectIndex = list.locationToIndex(e.point)
                    list.putClientProperty(RightSelectKey, selectIndex)
                    createPopup().show(RelativePoint(list, e.point))
                }
            }
        }

        private fun createPopup(): ListPopup {
            val ctx = DataManager.getInstance().getDataContext(list)
            return JBPopupFactory.getInstance().createActionGroupPopup("操作", getActionGroup(), ctx, false, {
                list.putClientProperty(RightSelectKey, null)
            }, 10)
        }

        private fun getActionGroup(): DefaultActionGroup {
            return ActionManager.getInstance().getAction("SalvoApiRightMenuActionGroup") as DefaultActionGroup
        }
    }

}


class SalvoApiItemRender : ColoredListCellRenderer<SalvoApiItem>() {
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
    }

}

