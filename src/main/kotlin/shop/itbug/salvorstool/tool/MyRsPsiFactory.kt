package shop.itbug.salvorstool.tool

import com.intellij.psi.PsiElement
import org.rust.lang.core.psi.impl.RsStructItemImpl
import shop.itbug.salvorstool.dialog.*


class MyRsPsiFactoryError(message: String) : Exception(message)

object MyRsPsiFactory {

    ///生成对象
    fun generateDto(type: GenerateDtoDialogResultEnum, psiElement: RsStructItemImpl): String {
        psiElement.myManager.structName ?: throw MyRsPsiFactoryError("获取名称失败")
        val tabName = psiElement.myManager.getTableName ?: throw MyRsPsiFactoryError("获取表明失败")
        val generateStructName = type.getStructName(tabName)
        val outer = type.getOuterAttr()
        ///组装
        val sb = StringBuilder()
        sb.appendLine(outer)
        sb.appendLine("pub struct $generateStructName {")
        var fields = psiElement.myManager.fieldList
        fields = type.getFields(fields)
        fields.forEach { field ->
            val pre = type.preview(field)
            if (pre != null) {
                sb.appendLine(pre)
            }
            sb.appendLine("    " + field.myManager.getSimpleText + if (field == fields.lastOrNull()) "" else ",")
        }
        sb.appendLine("}")
        return sb.toString()
    }


    /**
     * service add
     * }
     */
    fun generateServiceWithAdd(psiElement: RsStructItemImpl): String {
        val type = GenerateDtoDialogResultEnum.AddRequest
        val typeResponse = GenerateDtoDialogResultEnum.Response
        val tabName = psiElement.myManager.getTableName ?: throw MyRsPsiFactoryError("获取表名失败")
        val structName = type.getStructName(tabName)
        val responseName = typeResponse.getStructName(tabName)


        ///生成字段
        val sb = StringBuilder()
            val fs = psiElement.myManager.fieldList
        fs.forEach { field ->
            run {
                val manager = field.myManager
                if (manager.isPrimaryKey) {
                    sb.appendLine("\t\t\t${manager.name}: NotSet,")
                } else {
                    if(fs.lastOrNull() == field){
                        sb.append("\t\t\t${manager.name}: Set(req.${manager.name}.clone())")
                    }else{
                        sb.appendLine("\t\t\t${manager.name}: Set(req.${manager.name}.clone()),")
                    }

                }
            }
        }

        ///获取主键字段
        val primaryField = psiElement.myManager.fieldList.find { it.myManager.isPrimaryKey }
            ?: throw MyRsPsiFactoryError("获取主键字段失败")


        ///获取除主键以外的其他字段
        val fields = psiElement.myManager.fieldList.filter { !it.myManager.isPrimaryKey }
        val fsb = StringBuilder()
        fields.forEach { field -> run {
            val m = field.myManager
            if(fields.lastOrNull() == field){
                fsb.append("\t\t${m.name}: req.${m.name}")
            }else{
                fsb.appendLine("\t\t${m.name}: req.${m.name},")
            }

        }}



        val text = """
pub async fn add_$tabName(req: $structName) -> AppResult<$responseName> {
    let db = DB
        .get()
        .ok_or(anyhow::anyhow!("Database connection failed."))?;
    let model = $tabName::ActiveModel {
$sb
    };
    let result = ${tabName.underlineToCamel.capitalizeFirstLetter()}::insert(model).exec(db).await?;
    Ok($responseName {
        ${primaryField.myManager.name}: result.last_insert_id,
$fsb
    })
}""".trimIndent()
        return text
    }

    /**
     * service 修改
     */
    fun generateServiceByUpdate(psiElement: RsStructItemImpl): String {
        val type = GenerateDtoDialogResultEnum.UpdateRequest
        val typeResponse = GenerateDtoDialogResultEnum.Response
        val tabName = psiElement.myManager.getTableName ?: throw MyRsPsiFactoryError("获取表名失败")
        val structName = type.getStructName(tabName)
        val responseName = typeResponse.getStructName(tabName)

        ///获取主键字段
        val primaryField = psiElement.myManager.fieldList.find { it.myManager.isPrimaryKey }
            ?: throw MyRsPsiFactoryError("获取主键字段失败")


        val fields = psiElement.myManager.fieldList.filter { !it.myManager.isPrimaryKey }
        val sb = StringBuilder()
        fields.forEach {
            val m = it.myManager
            sb.appendLine("\tmodel.${m.name} = Set(req.${it.name});")
        }


        val responseFields = typeResponse.getFields(psiElement.myManager.fieldList)
        val fsb = StringBuilder()
        responseFields.forEach {
            val f = it.myManager.name
            fsb.appendLine("\t\t$f: result.${f},")
        }


        val text = """
pub async fn update_$tabName(req: $structName) -> AppResult<$responseName> {
    let db = DB
        .get()
        .ok_or(anyhow::anyhow!("Database connection failed."))?;

    let find = ${tabName.underlineToCamel.capitalizeFirstLetter()}::find_by_id(req.${primaryField.myManager.name}).one(db).await?;
    if find.is_none() {
        return Err(anyhow::anyhow!("${tabName.underlineToCamel.capitalizeFirstLetter()} does not exist.").into());
    }
    let mut model: $tabName::ActiveModel = find.unwrap().into();

$sb

    let result: $tabName::Model = model.update(db).await?;

    Ok($responseName {
$fsb
    })
}""".trimIndent()
        return text
    }


    /**
     * 生成删除的服务
     */
    fun generateServiceByDelete(psiElement: RsStructItemImpl): String {

        val primaryField = psiElement.myManager.fieldList.find { it.myManager.isPrimaryKey }
            ?: throw MyRsPsiFactoryError("获取主键字段失败")
        val tabName = psiElement.myManager.getTableName ?: throw MyRsPsiFactoryError("获取表名失败")
        val text = """
 pub async fn delete_$tabName(${primaryField.myManager.name}: ${primaryField.myManager.typeString}) -> AppResult<()> {
     let db = DB
         .get()
         .ok_or(anyhow::anyhow!("Database connection failed."))?;
     ${tabName.underlineToCamel.capitalizeFirstLetter()}::delete_by_id(${primaryField.myManager.name}).exec(db).await?;
     Ok(())
 }
         """.trimIndent()
        return text
    }


    /**
     * 获取列表
     */
    fun generateServiceByAll(psiElement: RsStructItemImpl): String {
        val tabName = psiElement.myManager.getTableName ?: throw MyRsPsiFactoryError("获取表名失败")
        val typeResponse = GenerateDtoDialogResultEnum.Response
        val typeStructName = typeResponse.getStructName(tabName)
        val responseFields = typeResponse.getFields(psiElement.myManager.fieldList)
        val fsb = StringBuilder()
        responseFields.forEach {
            val f = it.myManager.name
            fsb.appendLine("\t\t\t$f: r.${f},")
        }
        val text = """
pub async fn ${tabName+"_find_all"}() -> AppResult<Vec<$typeStructName>> {
    let db = DB
        .get()
        .ok_or(anyhow::anyhow!("Database connection failed."))?;
    let $tabName = ${tabName.underlineToCamel.capitalizeFirstLetter()}::find().all(db).await?;
    let res = $tabName
        .into_iter()
        .map(|r| $typeStructName {
$fsb
        })
        .collect::<Vec<_>>();
    Ok(res)
}
        """.trimIndent()
        return text
    }


    /**
     * 获取路由
     */
    fun generateRouterFile(psiElement: RsStructItemImpl): String {
        val tabName = psiElement.myManager.getTableName ?: throw MyRsPsiFactoryError("获取表名失败")
        val addReq = GenerateDtoDialogResultEnum.AddRequest.getStructName(tabName)
        val response = GenerateDtoDialogResultEnum.Response.getStructName(tabName)
        val updateReq = GenerateDtoDialogResultEnum.UpdateRequest.getStructName(tabName)
        val primaryField = psiElement.myManager.primaryField ?: throw MyRsPsiFactoryError("获取主键失败")
        val text = """
            use crate::{
                app_writer::{AppResult, AppWriter},
                dtos::$tabName::*,
                services::$tabName,
            };
            use salvo::Writer;
            use salvo::{
                oapi::endpoint,
                oapi::extract::{JsonBody, PathParam},
                Request,
            };
            
            //Router::with_path("/api/$tabName").get(get_${tabName}_all).post(post_add_$tabName).push(Router::with_path("<${primaryField.myManager.name}>").put(put_update_$tabName).delete(delete_$tabName))
            
            #[endpoint(tags("$tabName"))]
            pub async fn post_add_$tabName(new_$tabName: JsonBody<$addReq>) -> AppWriter<$response> {
                let result = $tabName::add_$tabName(new_$tabName.0).await;
                AppWriter(result)
            }

            #[endpoint(tags("$tabName"))]
            pub async fn put_update_$tabName(req: &mut Request) -> AppResult<AppWriter<$response>> {
                let req: $updateReq = req.extract().await?;
                let result = $tabName::update_$tabName(req).await;
                Ok(AppWriter(result))
            }

            #[endpoint(tags("$tabName"))]
            pub async fn delete_$tabName(${primaryField.myManager.name}: PathParam<${primaryField.myManager.typeString}>) -> AppWriter<()> {
                let result = $tabName::delete_$tabName(${primaryField.myManager.name}.0).await;
                AppWriter(result)
            }

            #[endpoint(tags("$tabName"))]
            pub async fn get_${tabName}_all() -> AppWriter<Vec<$response>> {
                let result = $tabName::${tabName}_find_all().await;
                AppWriter(result)
            }

        """.trimIndent()
        return text
    }
}