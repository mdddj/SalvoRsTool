package shop.itbug.salvorstool.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import kotlinx.coroutines.*
import shop.itbug.salvorstool.messageing.ApiScanMessaging
import shop.itbug.salvorstool.model.SalvoApiItem
import shop.itbug.salvorstool.tool.SalvoRouterParser
import shop.itbug.salvorstool.tool.rsFileType

@Service(Service.Level.PROJECT)
class SalvoApiService(val project: Project) : Disposable {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.EDT)

    private var projectApiList = mutableListOf<SalvoApiItem>()

    fun getApiList(): List<SalvoApiItem> {
        return projectApiList
    }

    //重新检查salvo接口
    fun doRefresh() {
        startScan(true)
    }

    fun startScan(isRefresh: Boolean = false) {
        scope.launch {
            val files = readAction { FileTypeIndex.getFiles(rsFileType, SalvoSearchGlobal(project)).toList() }
            val tasks = files.map { file -> async { parseFileApis(file) } }
            val apis = tasks.awaitAll().flatten()
            projectApiList = apis.toMutableList()
            project.messageBus.syncPublisher(ApiScanMessaging.TOPIC).apiScanEnd(apis, isRefresh)
        }
    }


    private suspend fun parseFileApis(file: VirtualFile): List<SalvoApiItem> {
        return readAction {
            val psi = PsiManager.getInstance(project).findFile(file) ?: return@readAction emptyList()
            return@readAction SalvoRouterParser(project).parseFile(psi)
        }
    }

    override fun dispose() {
        scope.cancel()
    }

    companion object {

        fun getInstance(project: Project): SalvoApiService {
            return project.getService(SalvoApiService::class.java)
        }
    }

}

class SalvoSearchGlobal(myProject: Project) : GlobalSearchScope(myProject) {

    private val fileIndex: ProjectFileIndex = ProjectRootManager.getInstance(myProject).fileIndex

    override fun contains(file: VirtualFile): Boolean {
        return fileIndex.isInContent(file)
    }

    override fun isSearchInModuleContent(aModule: Module): Boolean {
        return false
    }

    override fun isSearchInLibraries(): Boolean {
        return false
    }

}
