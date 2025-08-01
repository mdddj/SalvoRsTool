package shop.itbug.salvorstool.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.util.messages.Topic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import shop.itbug.salvorstool.model.ActixApiModel
import shop.itbug.salvorstool.parser.ActixApiModelParser
import shop.itbug.salvorstool.tool.Tools

interface ActixProjectListen {

    fun apiCollectEnd(apiModels: List<ActixApiModel>)
}


@Service(Service.Level.PROJECT)
class ActixProjectService(val project: Project) : Disposable {

    private val scope = CoroutineScope(Job() + Dispatchers.IO)
    private var isInScan = false
    private val psiManager = PsiManager.getInstance(project)
    private val modelParse = ActixApiModelParser()
    private var actixApis: List<ActixApiModel> = emptyList()
    val apiList get() = actixApis


    fun refreshApi(){
        scope.launch {
            collectApi()
        }
    }

    suspend fun collectApi() {
        isInScan = true
        val models = withContext(Dispatchers.IO) {
            val rsFiles = readAction { Tools.getProjectRsFiles(project) }
            val models =
                rsFiles.map {
                    scope.async {
                        readAction { psiManager.findFile(it) }?.run {
                            readAction {
                                modelParse.parse(
                                    this
                                )
                            }
                        }
                    }
                }.awaitAll()
                    .filterNotNull().flatten()
            models
        }
        actixApis = models
        project.messageBus.syncPublisher(TOPIC).apiCollectEnd(actixApis)
        isInScan = false
    }

    suspend fun isUseActixDeps() = RustProjectService.getInstance(project).hasActixWebDependencies()

    override fun dispose() {
        scope.cancel()
    }

    companion object {
        fun getInstance(project: Project) = project.service<ActixProjectService>()

        val TOPIC = Topic.create<ActixProjectListen>("ActixProjectListen", ActixProjectListen::class.java)
    }
}
