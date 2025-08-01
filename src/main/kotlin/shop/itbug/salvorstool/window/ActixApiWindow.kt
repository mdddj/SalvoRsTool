package shop.itbug.salvorstool.window

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.Project
import com.intellij.ui.*
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.filterField.*
import com.intellij.ui.speedSearch.SpeedSearchUtil
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListUiUtil
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel
import kotlinx.coroutines.*
import shop.itbug.salvorstool.model.ActixApiModel
import shop.itbug.salvorstool.model.ActixApiModelMethod
import shop.itbug.salvorstool.service.ActixProjectListen
import shop.itbug.salvorstool.service.ActixProjectService
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

// Actix 窗口布局
class ActixApiWindow(val project: Project) : BorderLayoutPanel(), Disposable, ActixProjectListen {

    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private val actixService = ActixProjectService.getInstance(project)

    // API列表
    private val apiListModel = DefaultListModel<ActixApiModel>()
    private val apiList = JBList(apiListModel)

    // 当前选中的API
    private var selectedApi: ActixApiModel? = null

    private val moduleFilter = object : FilterField("Module") {
        private var selected: String? = null
        override fun buildActions(): Collection<AnAction> {
            // 获取所有模块名称
            val moduleNames = apiListModel.elements().asSequence()
                .mapNotNull { it.module?.name }
                .distinct()
                .sorted()
                .toList()

            // 如果没有模块，只显示项目名称
            if (moduleNames.isEmpty()) {
                return listOf(
                    FilterValueAction(
                        attribute = project.name,
                        title = project.name,
                        value = project.name,
                        applier = object : FilterApplier {
                            override fun applyFilter(
                                attribute: String,
                                values: Collection<String>
                            ) {
                                selected = values.firstOrNull()
                                filterApiList()
                                invalidate()
                                repaint()
                            }
                        }
                    )
                )
            }

            // 为每个模块创建筛选动作
            val actions = moduleNames.map { moduleName ->
                FilterValueAction(
                    attribute = moduleName,
                    title = moduleName,
                    value = moduleName,
                    applier = object : FilterApplier {
                        override fun applyFilter(
                            attribute: String,
                            values: Collection<String>
                        ) {
                            selected = values.firstOrNull()
                            filterApiList()
                            invalidate()
                            repaint()
                        }
                    }
                )
            }.toMutableList()

            // 添加"全部"选项
            actions.add(
                0, FilterValueAction(
                attribute = "All",
                title = "All",
                value = "All",
                applier = object : FilterApplier {
                    override fun applyFilter(
                        attribute: String,
                        values: Collection<String>
                    ) {
                        selected = if (values.firstOrNull() == "All") null else values.firstOrNull()
                        filterApiList()
                        invalidate()
                        repaint()
                    }
                }
            ))

            return actions
        }

        override fun getCurrentText(): String? {
            return selected
        }

        fun clean(){
            selected = null
            invalidate()
            repaint()
            updateUI()
        }
    }

    private val methodFilter = object : FilterField("Method") {
        var select: Collection<String>? = null
        override fun buildActions(): Collection<AnAction> {
            // 为每个HTTP方法创建筛选动作
            val actions = ActixApiModelMethod.entries.map { method ->
                FilterValueAction(
                    attribute = method.name,
                    title = method.name,
                    value = method.name,
                    applier = object : FilterApplier {
                        override fun applyFilter(
                            attribute: String,
                            values: Collection<String>
                        ) {
                            select = values
                            filterApiList()
                            invalidate()
                            repaint()
                        }
                    }
                )
            }.toMutableList()

            // 添加"全部"选项
            actions.add(
                0, FilterValueAction(
                attribute = "All",
                title = "All",
                value = "All",
                applier = object : FilterApplier {
                    override fun applyFilter(
                        attribute: String,
                        values: Collection<String>
                    ) {
                        select = if (values.firstOrNull() == "All") null else values
                        filterApiList()
                        invalidate()
                        repaint()
                    }
                }
            ))

            return actions
        }

        override fun getCurrentText(): String? {
            return if (select?.contains("All") == true || select.isNullOrEmpty()) {
                null
            } else {
                select?.joinToString(",")
            }
        }

        fun clean(){
            select = null
            invalidate()
            repaint()
            updateUI()
        }
    }

    init {
        initializeUI()
        loadData()
        project.messageBus.connect(parentDisposable = this).subscribe(ActixProjectService.TOPIC, this)
    }

    private fun initializeUI() {
        // 创建顶部工具栏
        val toolbar = createToolbar()
        addToTop(BorderLayoutPanel().apply {

            val box = Box.createHorizontalBox()

            box.add(moduleFilter)

            box.add(methodFilter)

            addToLeft(toolbar)
            addToRight(box)
        })


        // 创建主内容区域
        val contentPanel = createContentPanel()
        addToCenter(contentPanel)

        // 设置样式
        setupStyles()
    }

    private fun createToolbar(): JComponent {

        val actions = DefaultActionGroup()
        // 刷新按钮
        val refreshAction =
            object : AnAction("Refresh", "Refresh API list", AllIcons.Actions.Refresh) {
                override fun actionPerformed(e: AnActionEvent) {
                    moduleFilter.clean()
                    methodFilter.clean()
                    ActixProjectService.getInstance(project).refreshApi()
                }
            }
        actions.add(refreshAction)

        val myToolBar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, actions, true)
        myToolBar.targetComponent = this
        return myToolBar.component
    }

    private fun createContentPanel(): JPanel {
        val contentPanel = BorderLayoutPanel()

        // 创建API列表
        setupApiList()
        val listScrollPane = JBScrollPane(apiList).apply {
            border = BorderFactory.createEmptyBorder()
        }
        listScrollPane.preferredSize = Dimension(400, 300)

        // 创建分割面板
        val splitPane = OnePixelSplitter(true)
        splitPane.firstComponent = listScrollPane

        contentPanel.add(splitPane, BorderLayout.CENTER)
        return contentPanel
    }

    private fun setupApiList() {
        apiList.cellRenderer = ActixApiListCellRenderer()
        apiList.selectionMode = ListSelectionModel.SINGLE_SELECTION

        // 添加选择监听
        apiList.addListSelectionListener { e ->
            if (!e.valueIsAdjusting) {
                selectedApi = apiList.selectedValue
            }
        }

        // 添加右键菜单
        val popupMenu = createPopupMenu()
        apiList.componentPopupMenu = popupMenu

        // 添加双击监听
        apiList.addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.clickCount == 2) {
                        selectedApi?.let { navigateToCode(it) }
                    }
                }
            }
        )

        // 设置空状态文本
        ListUiUtil.Selection.installSelectionOnFocus(apiList)
        TreeUIHelper.getInstance().installListSpeedSearch(apiList) { api ->
            "${api.method.name} ${api.url}"
        }
    }


    private fun createPopupMenu(): JPopupMenu {
        val popupMenu = JPopupMenu()

        val goToDefinition = JMenuItem("Go to Definition")
        goToDefinition.addActionListener { selectedApi?.let { navigateToCode(it) } }

        val copyUrl = JMenuItem("Copy URL")
        copyUrl.addActionListener {
            selectedApi?.let {
                val url = it.url
                val stringSelection = StringSelection(url)
                Toolkit.getDefaultToolkit().systemClipboard.setContents(stringSelection, null)
            }
        }


        popupMenu.add(goToDefinition)
        popupMenu.addSeparator()
        popupMenu.add(copyUrl)

        return popupMenu
    }

    private fun setupStyles() {
        background = UIUtil.getPanelBackground()

        // 设置空状态
        if (apiListModel.isEmpty) {
            setEmptyState()
        }
    }

    private fun setEmptyState() {
        val emptyText = (apiList as JBList<*>).emptyText
        emptyText.text = "No Actix-Web endpoints found"
        emptyText.appendLine(
            "What is Actix-Web?",
            SimpleTextAttributes(
                SimpleTextAttributes.STYLE_HOVERED,
                JBUI.CurrentTheme.Link.Foreground.ENABLED
            )
        ) { BrowserUtil.browse("https://actix.rs/") }
    }

    private fun loadData() {
        scope.launch {
            try {
                if (actixService.isUseActixDeps()) {
                    val apis = actixService.apiList

                    SwingUtilities.invokeLater {
                        apiListModel.clear()
                        apis.forEach { apiListModel.addElement(it) }

                        // 应用筛选器
                        filterApiList()

                        if (apis.isEmpty()) {
                            setEmptyState()
                        }
                    }
                } else {
                    SwingUtilities.invokeLater {
                        apiListModel.clear()
                        setEmptyState()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun navigateToCode(api: ActixApiModel) {
        api.functionElement?.element?.navigate(true)
    }

    override fun dispose() {
        scope.cancel()
    }

    override fun apiCollectEnd(apiModels: List<ActixApiModel>) {
        loadData()
    }

    /**
     * 根据模块和方法筛选器过滤API列表
     */
    private fun filterApiList() {
        // 获取所有API
        val allApis = actixService.apiList

        // 应用模块筛选
        val moduleFiltered = if (moduleFilter.getCurrentText() != null && moduleFilter.getCurrentText() != "All") {
            allApis.filter { api ->
                api.module?.name == moduleFilter.getCurrentText()
            }
        } else {
            allApis
        }

        // 应用方法筛选
        val methodFiltered = if (methodFilter.select != null && methodFilter.select!!.isNotEmpty()) {
            moduleFiltered.filter { api ->
                api.method.name in methodFilter.select!!
            }
        } else {
            moduleFiltered
        }

        // 更新列表模型
        SwingUtilities.invokeLater {
            apiListModel.clear()
            methodFiltered.forEach { apiListModel.addElement(it) }
        }
    }

    // API列表渲染器
    private inner class ActixApiListCellRenderer : ColoredListCellRenderer<ActixApiModel>() {
        override fun customizeCellRenderer(
            list: JList<out ActixApiModel>,
            value: ActixApiModel?,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean
        ) {
            value?.let { api ->
                icon = this.getMethodIcon()

                append(api.url)

                append("  ")
                append(
                    "[${api.method.name.uppercase()}]",
                    SimpleTextAttributes(
                        SimpleTextAttributes.STYLE_BOLD,
                        this.getMethodColor(api.method)
                    )
                )

                api.psiFile?.let { file ->
                    append("  ")
                    append("(${file.name})", SimpleTextAttributes.GRAY_ATTRIBUTES)
                }

                api.comment?.let { comment ->
                    if (comment.isNotBlank()) {
                        append("  ")
                        append("// $comment", SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES)
                    }
                }
            }

            SpeedSearchUtil.applySpeedSearchHighlighting(list, this, false, selected)
        }

        fun getMethodIcon(): Icon {
            return AllIcons.Nodes.Method
        }

        fun getMethodColor(method: ActixApiModelMethod): Color {
            return when (method) {
                ActixApiModelMethod.Get -> JBColor(0x61AFFE, 0x61AFFE) // 蓝色
                ActixApiModelMethod.Post -> JBColor(0x49CC90, 0x49CC90) // 绿色
                ActixApiModelMethod.Put -> JBColor(0xFCA130, 0xFCA130) // 橙色
                ActixApiModelMethod.Delete -> JBColor(0xF93E3E, 0xF93E3E) // 红色
                ActixApiModelMethod.Patch -> JBColor(0x50E3C2, 0x50E3C2) // 青色
                ActixApiModelMethod.Head -> JBColor(0x9013FE, 0x9013FE) // 紫色
                else -> JBColor.GRAY
            }
        }
    }
}
