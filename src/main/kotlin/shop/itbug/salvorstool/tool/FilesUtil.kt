package shop.itbug.salvorstool.tool

import org.apache.commons.io.IOUtils

object FilesUtil
{

    val files = listOf("temps/ts_input.tsx","temps/ts_modal.tsx")

    /**
     * 加载模板文件列表
     */
    fun loadTemplateFileList(path: String): String {
        try {
            println("path $path")
            val resource = javaClass.getResource("/$path")
            println(resource)
            resource?.let {
                return IOUtils.toString(it,"UTF-8")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
        return ""
    }

}