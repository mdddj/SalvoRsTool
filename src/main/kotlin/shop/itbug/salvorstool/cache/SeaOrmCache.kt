package shop.itbug.salvorstool.cache

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import shop.itbug.salvorstool.dialog.GenerateEntityDialog

@Service(Service.Level.PROJECT)
@State(name = "SeaOrmGenerateEntityConfig.xml", category = SettingsCategory.PLUGINS)
@Storage(roamingType = RoamingType.DEFAULT)
class SeaOrmCache: SimplePersistentStateComponent<GenerateEntityDialog.Config>(GenerateEntityDialog.Config()) {

    companion object{
        fun getInstance(project: Project): SeaOrmCache = project.service<SeaOrmCache>()
    }
}