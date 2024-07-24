package shop.itbug.salvorstool.service

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.toml.lang.psi.TomlFile
import org.toml.lang.psi.TomlKeyValue
import shop.itbug.salvorstool.tool.filterByType
import java.io.File

@Service(Service.Level.PROJECT)
class RustProjectService (val project: Project){


    /**
     * 是否使用了salvo的依赖
     */
    fun hasSalvoDependencies() : Boolean {
        val tomlFilePath = project.basePath + File.separator + "Cargo.toml"
        val tomlFile: VirtualFile = LocalFileSystem.getInstance().findFileByPath(tomlFilePath) ?: return false
        val file = ReadAction.compute<PsiFile,Throwable> { PsiManager.getInstance(project).findFile(tomlFile) } as? TomlFile ?: return false
        val keyValues = file.filterByType<TomlKeyValue>()
        return keyValues.any { it.key.text == "salvo" }
    }

    companion object {
        fun getInstance(project: Project): RustProjectService = project.getService(RustProjectService::class.java)
    }
}