package shop.itbug.salvorstool.dsl

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.LabelPosition
import com.intellij.ui.dsl.builder.Panel
import shop.itbug.salvorstool.model.SalvoApiItem
import shop.itbug.salvorstool.service.SalvoApiService
import javax.swing.DefaultComboBoxModel

typealias SalvoApiFilter = (apis: List<SalvoApiItem>) -> List<SalvoApiItem>
typealias SalvoCellBuilder = (component: Cell<ComboBox<SalvoApiItem>>) -> Unit

object HttpSelectDsl {

    fun apiSelect(project: Project, panel: Panel, filter: SalvoApiFilter? = null, apply: SalvoCellBuilder): Panel {
        var allApis = SalvoApiService.getInstance(project).getApiList()
        allApis = filter?.invoke(allApis) ?: allApis
        panel.row {
            val myComboBox = comboBox<SalvoApiItem>(DefaultComboBoxModel(allApis.toTypedArray()))
            apply.invoke(myComboBox)
        }
        return panel
    }

    fun apiSelect(project: Project, panel: Panel, filter: SalvoApiFilter? = null, label: String, apply: SalvoCellBuilder): Panel {
        return apiSelect(project, panel, filter){
            it.label(label,LabelPosition.TOP)
            apply.invoke(it)
        }
    }
}