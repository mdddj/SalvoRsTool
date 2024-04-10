package shop.itbug.salvorstool.i18n

import com.intellij.AbstractBundle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method
import java.util.*


object MyI18n: MyPluginBundle(){
    override fun getMessage(key: String, vararg params: Any?): String {
        return super.getMessage(key, *params)
    }
}

open class MyPluginBundle : AbstractBundle("messages.pluginBundle") {


    override fun findBundle(
        pathToBundle: String,
        loader: ClassLoader,
        control: ResourceBundle.Control
    ): ResourceBundle {
        val base = ResourceBundle.getBundle(pathToBundle)
        try {

            val lang = Locale.getDefault().language
            val localPath = pathToBundle+"_${lang}.properties"

            val findBundle = super.findBundle(localPath, MyPluginBundle::class.java.classLoader, control)
            if(base != findBundle){
                setParent(findBundle,base)
                return findBundle
            }
            if(lang == "zh"){
                return base
            }
            return base
        } catch (e: Exception) {
            return super.findBundle(pathToBundle+"_en.properties", MyPluginBundle::class.java.classLoader, control)//找不到就用英语
        }
    }

    /**
     * 从com.intellij中借用代码。DynamicBundle使用反射设置父捆绑包。
     */
    private fun setParent(localeBundle: ResourceBundle, base: ResourceBundle) {
        try {
            val method: Method = ResourceBundle::class.java.getDeclaredMethod("setParent", ResourceBundle::class.java)
            method.isAccessible = true
            MethodHandles.lookup().unreflect(method).bindTo(localeBundle).invoke(base)
        } catch (e: Throwable) {
            // ignored, better handle this in production code
        }
    }
}
