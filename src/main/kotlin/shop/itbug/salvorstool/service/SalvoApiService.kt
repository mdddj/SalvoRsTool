package shop.itbug.salvorstool.service

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
import com.intellij.psi.util.PsiTreeUtil
import kotlinx.coroutines.*
import org.rust.lang.core.psi.impl.RsFunctionImpl
import shop.itbug.salvorstool.messageing.ApiScanMessaging
import shop.itbug.salvorstool.model.SalvoApiItem
import shop.itbug.salvorstool.tool.myManager
import shop.itbug.salvorstool.tool.rsFileType
import shop.itbug.salvorstool.tool.rsLetDeclImplManager

@Service(Service.Level.PROJECT)
class SalvoApiService(val project: Project) {

    private var projectApiList = mutableListOf<SalvoApiItem>()

    fun getApiList(): List<SalvoApiItem> {
        return projectApiList
    }

    fun doRefresh() {
        startScan()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun startScan() {
        GlobalScope.launch(Dispatchers.EDT) {
             val hasSalvoDeps = RustProjectService.getInstance(project).hasSalvoDependencies()
            if(hasSalvoDeps) {
                val files = readAction { FileTypeIndex.getFiles(rsFileType, SalvoSearchGlobal(project)).toList() }
                val tasks = files.map { file -> async { findRouterRsFunction(file) } }
                val result = tasks.awaitAll().flatten()
                val apis = mutableListOf<SalvoApiItem>()
                result.forEach { r: RsFunctionImpl ->
                    readAction { r.myManager.allLet.map { i -> apis.addAll(i.rsLetDeclImplManager.allApi) } }
                }
                projectApiList = apis
                project.messageBus.syncPublisher(ApiScanMessaging.TOPIC).apiScanEnd(apis)
            }
        }
    }



    private suspend fun findRouterRsFunction(file: VirtualFile): List<RsFunctionImpl> {
        return readAction {
            val psi = PsiManager.getInstance(project).findFile(file)
            var find =
                PsiTreeUtil.findChildrenOfType(psi, RsFunctionImpl::class.java).toList()
            find = find.filter { it.myManager.isReturnRouter }
            return@readAction find
        }
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