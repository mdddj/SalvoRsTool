package shop.itbug.salvorstool.service

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class ProjectStartActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        SalvoApiService.getInstance(project).startScan()


        // actix setup
        val actixService = ActixProjectService.getInstance(project)
        if (actixService.isUseActixDeps()) {
            actixService.collectApi()
        }

    }
}