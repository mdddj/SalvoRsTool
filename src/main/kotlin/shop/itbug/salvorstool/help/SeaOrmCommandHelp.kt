package shop.itbug.salvorstool.help

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessOutput
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ThrowableComputable
import shop.itbug.salvorstool.tool.NotificationUtil
import java.io.File

class SeaOrmCommandHelp(val dirPath: String, val project: Project) {

    private val COMMAND_CLI = "sea-orm-cli"


    ///获取版本
    fun getVersion(): String? {
        val output = runCommand("--version")
        return output?.stdout?.trim()
    }


    ///初始化
    fun migrateInit(customDir: String? = null) {
        val params = mutableSetOf<String>()
        if (customDir != null) {
            params.add("-d")
            params.add(customDir)
        }
        runCommand("init", *params.toTypedArray())
    }


    fun runUp() {
        runCommand("migrate", "up")
    }

    fun runDown() {
        runCommand("migrate", "down")
    }

    fun checkStatus() {
        runCommand("migrate", "status")
    }

    fun runFresh() {
        runCommand("migrate", "fresh")
    }

    fun runRefresh() {
        runCommand("migrate", "refresh")
    }

    fun runReset() {
        runCommand("migrate", "reset")
    }

    ///生成数据表
    fun migrateGenerate(filename: String) {
        runCommand("migrate", "generate", filename)
    }

    ///生成模型
    fun customRunCommand(vararg commands: String) {
        runCommand(*commands)
    }

    ///执行命令
    private fun runCommand(vararg args: String): ProcessOutput? {
        val commandLine = GeneralCommandLine(COMMAND_CLI).apply {
            this.workDirectory = File(dirPath)
            this.addParameters(args.toMutableList().filter { it.isNotBlank() })
        }
        try {
            val result = ProgressManager.getInstance().runProcessWithProgressSynchronously(ThrowableComputable {
                OSProcessHandler(commandLine).startNotify()
                val po = ExecUtil.execAndGetOutput(commandLine)
                po
            }, COMMAND_CLI, true, project)
            if (result.exitCode != 0) {
                NotificationUtil.getInstance(project).seaOrmNotifyError(result.stderr)
            } else {
                NotificationUtil.getInstance(project).seaOrmNotifyInfo(result.stdout.trim())
            }
            return result
        } catch (e: Exception) {
            NotificationUtil.getInstance(project).seaOrmNotifyError(e.localizedMessage)
        }
        return null
    }

}