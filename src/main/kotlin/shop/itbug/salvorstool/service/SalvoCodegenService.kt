package shop.itbug.salvorstool.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * 代码生成器
 * salvo 代码生成器服务
 * 一键生成前端和后端代码
 */
@Service(Service.Level.PROJECT)
class SalvoCodegenService(val project: Project) : Disposable {

    private val jobContext = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun dispose() {
        jobContext.cancel()
    }

    fun runJob(call: (scope: CoroutineScope) -> Unit) {
        jobContext.launch(Dispatchers.IO) {
            call.invoke(this)
        }
    }

    companion object {
        fun getInstance(project: Project): SalvoCodegenService {
            return project.service()
        }
    }

}