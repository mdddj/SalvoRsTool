package shop.itbug.salvorstool.service

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class ProjectStartActivity: ProjectActivity {
    override suspend fun execute(project: Project) {
        project.messageBus.connect().subscribe(DumbService.DUMB_MODE, object : DumbService.DumbModeListener {
            override fun exitDumbMode() {
                super.exitDumbMode()
                SalvoApiService.getInstance(project).startScan()
            }
        })
    }
}