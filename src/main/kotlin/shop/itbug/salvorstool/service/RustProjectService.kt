package shop.itbug.salvorstool.service

import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.toml.lang.psi.TomlFile
import org.toml.lang.psi.TomlKeyValue
import shop.itbug.salvorstool.tool.filterByType
import java.io.File

/**
 * Rust项目的一些函数和判断
 */
@Service(Service.Level.PROJECT)
class RustProjectService(val project: Project) {

    /**
     *  协程用法:
     *  检查项目中是否使用了salvo的依赖
     */
    suspend fun hasSalvoDependencies(): Boolean {
        return hasDependencies("salvo")
    }


    /**
     * 检测项目中是否依赖某个包
     */
    suspend fun hasDependencies(name: String): Boolean {
        return getAllDependencies().contains(name)
    }


    /**
     * 获取Cargo.toml文件中的顶级key
     */
    suspend fun getAllDependencies(): List<String> {
        val tomlFile: VirtualFile = getCargoFile() ?: return emptyList()
        val file = readAction { PsiManager.getInstance(project).findFile(tomlFile) } as? TomlFile ?: return emptyList()
        return readAction { file.filterByType<TomlKeyValue>() }.map { it.key.text }
    }

    /**
     * 获取项目的配置文件
     * Cargo.toml
     */
    suspend fun getCargoFile(): VirtualFile? {
        val tomlFilePath = project.basePath + File.separator + "Cargo.toml"
        return readAction { LocalFileSystem.getInstance().findFileByPath(tomlFilePath) }
    }

    companion object {
        fun getInstance(project: Project): RustProjectService = project.getService(RustProjectService::class.java)
    }
}