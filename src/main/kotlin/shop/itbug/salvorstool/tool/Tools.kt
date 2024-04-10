package shop.itbug.salvorstool.tool

import com.intellij.lang.Language
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import org.rust.lang.RsLanguage
import org.rust.lang.core.psi.impl.RsStructItemImpl
import java.io.File

object Tools {


    /**
     * 获取dto目录
     */
    fun getDtoFolder(project: Project) : VirtualFile? {
        val rootDir = project.guessProjectDir()
        if(rootDir != null) {
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
    fun getServiceFolder(project: Project) : VirtualFile? {
        val rootDir = project.guessProjectDir()
        if(rootDir != null) {
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
    fun getRouterFolder(project: Project) : VirtualFile? {
        val rootDir = project.guessProjectDir()
        if(rootDir != null) {
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
    val getDtoImportPackagesText: String  get() {
        return """
            use salvo::prelude::{Extractible, ToSchema};
            use serde::{Deserialize, Serialize};
            use validator::Validate;
        """.trimIndent()
    }

    fun getServiceImportPackages(psiElement: RsStructItemImpl) : String {
        val tabName = psiElement.myManager.getTableName ?: throw MyRsPsiFactoryError("获取表名失败")
        return """
            use crate::{
                app_writer::AppResult,
                db::DB,
                dtos::$tabName::*,
                entities::*,
            };
            use sea_orm::{ActiveModelTrait, ColumnTrait, EntityTrait, QueryFilter, Set, NotSet};
            use crate::dtos::$tabName::*;
            use crate::entities::prelude::${tabName.capitalizeFirstLetter()};
        """.trimIndent()
    }

    /**
     * 创建rs文件
     */
    fun createRsPsiFile(fileName: String,text: String,project: Project) : PsiFile {
        return PsiFileFactory.getInstance(project)
            .createFileFromText("$fileName.rs", Language.findInstance(RsLanguage::class.java), text)
    }

    fun saveTo(project: Project,psiFile: PsiFile,dirPath: String) {
        val dirFile = LocalFileSystem.getInstance().findFileByPath(dirPath) ?: throw IllegalStateException("Can't find save file")
        val dir = PsiManager.getInstance(project).findDirectory(dirFile) ?: throw IllegalStateException("Can't find directory")
        runWriteAction {
            try{
                dir.add(psiFile)
                println("saved success")
            }catch (e:Exception){
                throw e
            }
        }
    }
}