package shop.itbug.salvorstool.searcheverywhere

import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.util.Processor
import shop.itbug.salvorstool.model.SalvoApiItem
import shop.itbug.salvorstool.service.SalvoApiService
import shop.itbug.salvorstool.window.SalvoApiItemRender
import javax.swing.ListCellRenderer

class SalvoSearchApi : SearchEverywhereContributorFactory<SalvoApiItem> {

    override fun createContributor(initEvent: AnActionEvent): SearchEverywhereContributor<SalvoApiItem> {
        return MySearchEverywhereProvider(initEvent.project!!)
    }

    override fun isAvailable(project: Project?): Boolean {
        return project != null
    }

    private inner class MySearchEverywhereProvider(val project: Project) : SearchEverywhereContributor<SalvoApiItem> {

        var allApi: List<SalvoApiItem> = SalvoApiService.getInstance(project).getApiList()


        override fun getSearchProviderId(): String {
            return MySearchEverywhereProvider::class.java.name
        }

        override fun getGroupName(): String {
            return "Salvo"
        }

        override fun getSortWeight(): Int {
            return Integer.MAX_VALUE
        }

        override fun showInFindResults(): Boolean {
            return true
        }

        override fun getElementsRenderer(): ListCellRenderer<in SalvoApiItem> {
            return SalvoApiItemRender()
        }

        override fun getDataForItem(element: SalvoApiItem, dataId: String): Any? {
            return null
        }

        override fun processSelectedItem(selected: SalvoApiItem, modifiers: Int, searchText: String): Boolean {
            selected.navTo()
            return true
        }

        override fun fetchElements(
            pattern: String,
            progressIndicator: ProgressIndicator,
            consumer: Processor<in SalvoApiItem>
        ) {
            val list = if (pattern.isEmpty()) allApi else allApi.filter { it.api.contains(pattern) }
            list.forEach {
                if (consumer.process(it).not()) {
                    return
                }
            }
        }

        override fun isShownInSeparateTab(): Boolean {
            return true
        }

    }
}