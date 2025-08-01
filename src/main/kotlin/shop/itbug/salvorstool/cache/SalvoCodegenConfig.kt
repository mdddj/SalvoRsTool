package shop.itbug.salvorstool.cache

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SettingsCategory
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project


class SalvoCodegenState : BaseState() {

    // admin 项目路径 (umi js antd项目)
    var antdProjectPath by property("") { it.isEmpty() }

    // 前端项目代码生成路径,一般是pages目录
    var antdPagesPath by property("") { it.isEmpty() }

    companion object {

        fun fromProject(project: Project): SalvoCodegenState {
            val state = SalvoCodegenState()

            return state
        }
    }

}


/**
 * 代码生成器配置
 */
@Service(Service.Level.PROJECT)
@State(
    name = "SalvoCodegenConfig",
    category = SettingsCategory.PLUGINS,
    storages = [Storage("salvo-codegen.xml")]
)
class SalvoCodegenConfig(val project: Project) :
    SimplePersistentStateComponent<SalvoCodegenState>(SalvoCodegenState.fromProject(project)) {


    companion object {
        fun getInstance(project: Project): SalvoCodegenConfig = project.service<SalvoCodegenConfig>()
    }
}