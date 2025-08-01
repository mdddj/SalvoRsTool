package shop.itbug.salvorstool.tool

import com.intellij.lang.Language
import com.intellij.lang.javascript.dialects.TypeScriptJSXLanguageDialect
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.rust.lang.RsFileType
import org.rust.lang.RsLanguage
import org.rust.lang.core.psi.RsFile
import org.rust.lang.core.psi.impl.RsStructItemImpl
import java.io.File
import javax.swing.BorderFactory
import javax.swing.border.Border

object Tools {


    val jsxLanguage: TypeScriptJSXLanguageDialect = Language.findInstance(
        TypeScriptJSXLanguageDialect::class.java
    )
    val rustLanguage: RsLanguage = Language.findInstance(RsLanguage::class.java)

    /**
     * 获取dto目录
     */
    fun getDtoFolder(project: Project): VirtualFile? {
        val rootDir = project.guessProjectDir()
        if (rootDir != null) {
            var path = rootDir.path
            path = path + File.separator + "src" + File.separator + "dtos"
            val findFileByPath = LocalFileSystem.getInstance().findFileByPath(path)
            return findFileByPath
        }
        return null
    }


    /**
     * 获取service目录
     */
    fun getServiceFolder(project: Project): VirtualFile? {
        val rootDir = project.guessProjectDir()
        if (rootDir != null) {
            var path = rootDir.path
            path = path + File.separator + "src" + File.separator + "services"
            val findFileByPath = LocalFileSystem.getInstance().findFileByPath(path)
            return findFileByPath
        }
        return null
    }

    /**
     * 获取路由目录
     */
    fun getRouterFolder(project: Project): VirtualFile? {
        val rootDir = project.guessProjectDir()
        if (rootDir != null) {
            var path = rootDir.path
            path = path + File.separator + "src" + File.separator + "routers"
            val findFileByPath = LocalFileSystem.getInstance().findFileByPath(path)
            return findFileByPath
        }
        return null
    }


    /**
     * 获取dto基础导包
     */
    val getDtoImportPackagesText: String
        get() {
            return """
            use salvo::prelude::{Extractible, ToSchema};
            use serde::{Deserialize, Serialize};
            use validator::Validate;
        """.trimIndent()
        }

    fun getServiceImportPackages(psiElement: RsStructItemImpl): String {
        val tabName = psiElement.structItemManager.getTableName ?: throw MyRsPsiFactoryError("获取表名失败")
        return """
            use crate::{
                app_writer::AppResult,
                db::DB,
                dtos::$tabName::*,
                entities::*,
            };
            use sea_orm::{ActiveModelTrait, ColumnTrait, EntityTrait, QueryFilter, Set, NotSet};
            use crate::dtos::$tabName::*;
            use crate::entities::prelude::${tabName.underlineToCamel.capitalizeFirstLetter()};
        """.trimIndent()
    }

    /**
     * 创建rs文件
     */
    fun createRsPsiFile(fileName: String, text: String, project: Project): PsiFile {
        return PsiFileFactory.getInstance(project)
            .createFileFromText("$fileName.rs", Language.findInstance(RsLanguage::class.java), text)
    }

    fun saveTo(project: Project, psiFile: PsiFile, dirPath: String) {
        val dirFile =
            LocalFileSystem.getInstance().findFileByPath(dirPath) ?: throw IllegalStateException("Can't find save file")
        val dir = PsiManager.getInstance(project).findDirectory(dirFile)
            ?: throw IllegalStateException("Can't find directory")
        runWriteAction {
            try {
                dir.add(psiFile)
                println("saved success")
            } catch (e: Exception) {
                throw e
            }
        }
    }

    suspend fun saveTo(project: Project, psiFile: PsiFile, virtualFile: VirtualFile) {
        println("find file :${virtualFile.path}")
        withContext(Dispatchers.EDT) {
            val psiDirectory = ApplicationManager.getApplication().executeOnPooledThread<PsiDirectory> {
                ApplicationManager.getApplication()
                    .runReadAction<PsiDirectory> { PsiManager.getInstance(project).findDirectory(virtualFile) }
            }.get()
                ?: return@withContext
            val newPsiFile =
                ApplicationManager.getApplication().runWriteAction<PsiElement> { psiDirectory.add(psiFile) }
            val document = PsiDocumentManager.getInstance(project).getDocument(newPsiFile.containingFile)
            document?.let {
                PsiDocumentManager.getInstance(project).commitDocument(it) // 同步PSI与文档内容
                FileDocumentManager.getInstance().saveDocument(it) // 将文档保存到磁盘

            }
            //格式化代码
            WriteCommandAction.runWriteCommandAction(project) {
                CodeStyleManager.getInstance(project).reformat(newPsiFile)
            }
        }
    }

    fun emptyBorder(): Border = BorderFactory.createEmptyBorder(0, 0, 0, 0)


    /**
     * 高亮代码段转html
     */
    fun highlightCodeToHtml(code: String, project: Project, lang: Language): String {
        val s = StringBuilder()
        val sb = HtmlSyntaxInfoUtil.appendHighlightedByLexerAndEncodedAsHtmlCodeSnippet(
            s,
            project,
            lang,
            code,
            true,
            0.65f
        )
        return sb.toString()
    }


    fun getProjectRsFiles(project: Project): List<VirtualFile> {
        val files = FileTypeIndex.getFiles(
            FileTypeManager.getInstance().findFileTypeByLanguage(rustLanguage)!!,
            GlobalSearchScope.projectScope(project)
        )
        return files.toList()
    }
}



