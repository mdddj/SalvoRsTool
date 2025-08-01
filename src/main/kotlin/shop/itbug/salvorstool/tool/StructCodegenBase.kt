package shop.itbug.salvorstool.tool

import com.intellij.lang.javascript.JSXFileType
import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiFileFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.rust.lang.core.psi.impl.RsStructItemImpl
import shop.itbug.salvorstool.dialog.SalvoCodegenDialogConfig
import shop.itbug.salvorstool.i18n.MyI18n
import shop.itbug.salvorstool.model.SalvoApiItem
import kotlin.io.path.Path

class CodegenError(msg: String) : RuntimeException(msg)

class StructCodegenBase(project: Project, rsStruct: RsStructItemImpl) {
    private val structManager = rsStruct.structItemManager

    private val tsInterfaceName = structManager.tsModelName //类模型名称
    private val dtoName = tsInterfaceName

    //获取列表api
    fun generateGetApi(item: SalvoApiItem): String {
        return item.apiWithFindAll(dtoName)
    }

    //新增模型
    fun generatePostApi(item: SalvoApiItem): String {
        return item.apiWithAdd(dtoName)
    }

    //修改类型
    fun generatePutApi(item: SalvoApiItem): String {
        return item.apiWithUpdatePut(dtoName)
    }

    //删除 api
    fun generateDeleteApi(item: SalvoApiItem): String {
        return item.apiWithDelete()
    }

    //生成对应的模块
    fun generateModelDirectory(pagesPath: String, modelName: String): VirtualFile {
        val fileSystem = LocalFileSystem.getInstance()
        val pagesDirector = fileSystem.findFileByPath(pagesPath) ?: throw CodegenError(MyI18n.folderIsNotFound)
        val modelFile = pagesDirector.findChild(modelName)
        ApplicationManager.getApplication().runWriteAction {
            modelFile?.delete(this)
        }
        val newDirectory = ApplicationManager.getApplication()
            .runWriteAction<VirtualFile> { fileSystem.createChildDirectory(this, pagesDirector, modelName) }
        println("create directory: ${newDirectory.path}")
        val dir =
            VirtualFileManager.getInstance().refreshAndFindFileByNioPath(Path(newDirectory.path)) ?: throw CodegenError(
                MyI18n.folderIsNotFound
            )
        println(dir.path + "  is directory: ${dir.isDirectory}")
        return dir
    }

    //获取模块目录
    suspend fun findModuleDirectory(pagesPath: String, modelName: String): VirtualFile? {
        return withContext(Dispatchers.IO) {
            val fileSystem = LocalFileSystem.getInstance()
            val pagesDirector = fileSystem.findFileByPath(pagesPath) ?: return@withContext null
            return@withContext pagesDirector.findChild(modelName)
        }
    }

    //生成 API文件
    suspend fun generateApiFile(moduleFile: VirtualFile, dialogConfig: SalvoCodegenDialogConfig, project: Project) {
        val sb = StringBuilder()

        sb.appendLine("import { request } from '@umijs/max';")

        sb.appendLine("import { ApiResult, $dtoName } from './Model';")

        dialogConfig.getApi?.let {
            sb.appendLine(generateGetApi(it))
        }
        dialogConfig.postApi?.let {
            sb.appendLine(generatePostApi(it))
        }
        dialogConfig.updateApi?.let {
            sb.appendLine(generatePutApi(it))
        }
        dialogConfig.deleteApi?.let {
            sb.appendLine(generateDeleteApi(it))
        }
        val tsFile = readAction {
            PsiFileFactory.getInstance(project).createFileFromText("Api.ts", TypeScriptFileType.language, sb.toString())
        }
        Tools.saveTo(project, tsFile, moduleFile)
    }

    suspend fun generateModelFile(
        moduleFile: VirtualFile,
        dialogConfig: SalvoCodegenDialogConfig,
        rsStructManager: MyRsStructManager,
        project: Project
    ) {
        val modelTSText = readAction { rsStructManager.getTSInterfaceWithCodegen }
        val sb = StringBuilder()

        sb.appendLine(
            """
export interface ApiResult<T> {
  code: number,
  data: T,
  msg: string
}
        """.trimMargin()
        )
        sb.appendLine("")
        sb.appendLine(modelTSText)
        val tsFile = readAction {
            PsiFileFactory.getInstance(project)
                .createFileFromText("Model.ts", TypeScriptFileType.language, sb.toString())
        }
        Tools.saveTo(project, tsFile, moduleFile)

    }

    //生成首页代码
    suspend fun generateIndexFile(
        moduleFile: VirtualFile,
        dialogConfig: SalvoCodegenDialogConfig,
        rsStructManager: MyRsStructManager,
        project: Project
    ) {
        val getListApi =
            dialogConfig.getApi?.getGetApiFunctionName() ?: throw CodegenError("get api function name not found")

        val deleteApi = dialogConfig.deleteApi?.getDeleteApiFunctionName()
            ?: throw CodegenError("delete api function name not found")

        val text = readAction { rsStructManager.genIndexPageString(getListApi, deleteApi) }
        val file = readAction {
            PsiFileFactory.getInstance(project).createFileFromText("index.tsx", JSXFileType.language, text)
        }
        Tools.saveTo(project, file, moduleFile)
    }

    //生成新增和修改的代码
    suspend fun generateAddOrUpdateFile(
        moduleFile: VirtualFile,
        dialogConfig: SalvoCodegenDialogConfig,
        rsStructManager: MyRsStructManager,
        project: Project
    ) {
        val addApi =
            dialogConfig.postApi?.getPostApiFunctionName() ?: throw CodegenError("post api function name not found")
        val updateApi =
            dialogConfig.updateApi?.getPutApiFunctionName() ?: throw CodegenError("update api function name not found")
        val text = readAction { rsStructManager.genWithEditOrAddString(addApi, updateApi) }
        val file = readAction {
            PsiFileFactory.getInstance(project).createFileFromText("add_or_update.tsx", JSXFileType.language, text)
        }
        Tools.saveTo(project, file, moduleFile)
    }
}
