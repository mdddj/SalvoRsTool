package shop.itbug.salvorstool.window

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.components.BorderLayoutPanel
import shop.itbug.salvorstool.tool.FilesUtil
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class TempFilesWindow(val project: Project) : BorderLayoutPanel(), ListSelectionListener {

    private val tempFiles: List<String> = FilesUtil.files
    private val list = JBList<String>().apply {
        cellRenderer = Layout()
        model = Model(tempFiles)
        addListSelectionListener(this@TempFilesWindow)
    }

    init {
        addToCenter(JBScrollPane(list))
    }

    private inner class Model(data: List<String>): DefaultListModel<String>(){
        init {
            addAll(data)
        }
    }

    private inner class Layout: ColoredListCellRenderer<String>() {
        override fun customizeCellRenderer(
            list: JList<out String>,
            value: String?,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean
        ) {
            value?.let { append(it) }
        }
    }

    override fun valueChanged(e: ListSelectionEvent?) {
        e?.let {
            if(!e.valueIsAdjusting){
                val text = FilesUtil.loadTemplateFileList(list.selectedValue)
                println(text)
                val lf = LightVirtualFile(list.selectedValue,text)
                FileEditorManager.getInstance(project).openFile(lf)
            }
        }
    }

}