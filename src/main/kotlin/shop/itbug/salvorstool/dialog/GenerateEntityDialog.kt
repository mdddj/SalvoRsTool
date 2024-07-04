package shop.itbug.salvorstool.dialog

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.Alarm
import shop.itbug.salvorstool.cache.SeaOrmCache
import shop.itbug.salvorstool.help.MyWebHelpProvider
import shop.itbug.salvorstool.i18n.MyI18n
import java.util.*
import javax.swing.JComponent
import javax.swing.SwingUtilities


///生成实体弹窗
class GenerateEntityDialog(val project: Project, val runCommand: (Config, String) -> Unit) :
    DialogWrapper(project, true) {

    data class Config(
        var databaseUrl: String = "",
        var databaseSchema: String = "",
        var outputDir: String = "",
        var verbose: Boolean = false,
        var libs: Boolean = false,
        var includeHiddenTables: Boolean = false,
        var ignoreTables: String = "",
        var compactFormat: Boolean = true,
        var expandedFormat: Boolean = false,
        var serde: Serde = Serde.None,
        var skipDeserializingPrimaryKey: Boolean = false,
        var skipHiddenColumn: Boolean = false,
        var timeCrate: TimeCrate = TimeCrate.Chrono,
        var maxConnections: Int = 1,
        var modelExtraDerives: Boolean = false,
        var modelExtraAttributes: Boolean = false,
        var enumExtraDerives: Boolean = false,
        var enumExtraAttributes: Boolean = false,
        var seaography: Boolean = false
    ) : BaseState()

    enum class Serde {
        None, Serialize, Deserialize, Both
    }

    enum class TimeCrate {
        Chrono, Time
    }

    private lateinit var panel: DialogPanel
    private lateinit var preview: Cell<ExpandableTextField>
    private val config = SeaOrmCache.getInstance(project).state
    private val alarm = Alarm(disposable)

    init {
        super.init()
        setOKButtonText(MyI18n.getMessage("run"))
        SwingUtilities.invokeLater {
            updatePreviewLabel()
            preview.component.text = "sea-orm-cli " + config.generateCommand()
        }
    }

    private fun updatePreviewLabel() {
        if (this.isDisposed) {
            return
        }
        alarm.addRequest({
            if (panel.isModified()) {
                panel.apply()
                SeaOrmCache.getInstance(project).loadState(config)
                preview.component.text = "sea-orm-cli " + config.generateCommand()
            }
            updatePreviewLabel()
        }, 1000)
    }

    override fun createCenterPanel(): JComponent {
        this.panel = panel {
            row("Database url") {
                textField()
                    .align(Align.FILL)
                    .bindText(config::databaseUrl)
                    .comment("Database URL (default: DATABASE_URL specified in ENV)")
            }
            row("Database schema") {
                textField()
                    .align(Align.FILL)
                    .bindText(config::databaseSchema)
                    .comment("Database schema (default: DATABASE_SCHEMA specified in ENV)")
            }
            row("Output dir") {
                textFieldWithBrowseButton(
                    "Select Folder", project,
                    FileChooserDescriptorFactory.createSingleFolderDescriptor()
                ) { it.path }
                    .align(Align.FILL)
                    .bindText(
                        config::outputDir
                    )
            }
            row("Include hidden tables") {
                checkBox("Generate entity files from hidden tables (tables with names starting with an underscore are hidden and ignored by default)").bindSelected(
                    config::includeHiddenTables
                )
            }

            row("Lib") {
                checkBox("Generate index file as lib.rs instead of mod.rs").bindSelected(config::libs)

            }
            row("Verbose") {
                checkBox("Print debug messages").bindSelected(config::verbose)

            }
            row("Compact format") {
                checkBox("Generate entity file of compact format (default: true)").bindSelected(config::compactFormat)
            }

            row("Ignore tables") {
                textField().bindText(config::ignoreTables)
                    .comment("Skip generating entity file for specified tables (default: seaql_migrations)")
            }

            row("Expanded Format") {
                checkBox("Generate entity file of expanded format").bindSelected(config::expandedFormat)
            }
            buttonsGroup("Serde") {
                row {
                    Serde.entries.forEach {
                        radioButton(it.name, it)
                    }
                }.comment("Automatically derive serde Serialize / Deserialize traits for the entity (none, serialize, deserialize, both) (default: none)")

            }.bind(config::serde)
            row("Serde skip deserializing primary key") {
                checkBox("Generate entity model with primary key field labeled as #[serde(skip_deserializing)]").bindSelected(
                    config::skipDeserializingPrimaryKey
                )
            }
            row("Skip hidden column") {
                checkBox("Generate entity model with hidden column (column name starts with _) field labeled as #[serde(skip)]").bindSelected(
                    config::skipHiddenColumn
                )
            }
            buttonsGroup("Time crate") {
                row {
                    TimeCrate.entries.forEach {
                        radioButton(it.name, it)
                    }
                }.comment("The datetime crate to use for generating entities (chrono, time) (default: chrono)")
            }.bind(config::timeCrate)
            row("Max Connections") {
                spinner(1..100).bindIntValue(config::maxConnections)
                    .comment("Maximum number of database connections to be initialized in the connection pool (default: 1)")
            }
            row("Model extra attributes") {
                checkBox("Append extra attributes to generated model struct").bindSelected(config::modelExtraAttributes)
            }
            row("Model extra derives") {
                checkBox("Append extra derive macros to the generated model struct").bindSelected(config::modelExtraDerives)
            }
            row("Enum Extra derives") {
                checkBox("Append extra derive macros to generated enums").bindSelected(config::enumExtraDerives)
            }
            row("Enum extra attributes") {
                checkBox("Append extra attributes to generated enums").bindSelected(config::enumExtraAttributes)
            }
            row("Sea ography") {
                checkBox("Generate addition structs in entities for seaography integration").bindSelected(config::seaography)
            }
            row("Preview Command") {
                preview = expandableTextField().align(Align.FILL)
            }
        }
        return this.panel
    }

    //生成命令
    private fun Config.generateCommand(): String {
        val sb = StringBuilder()
        sb.append("generate entity")
        if (databaseUrl.isNotBlank()) {
            sb.append(" -u $databaseUrl")
        }
        if (databaseSchema.isNotBlank()) {
            sb.append(" -s $databaseSchema")
        }
        if (outputDir.isNotBlank()) {
            sb.append(" -o $outputDir")
        }
        if (verbose) {
            sb.append(" -v")
        }
        if (libs) {
            sb.append(" -l")
        }
        if (includeHiddenTables) {
            sb.append(" --include-hidden-tables ")
        }
        if (ignoreTables.isNotBlank()) {
            sb.append(" --ignore-tables $ignoreTables ")
        }
        if (compactFormat) {
            sb.append(" --compact-format ")
        }
        if (expandedFormat) {
            sb.append(" --expanded-format ")
        }
        if (serde != Serde.None) {
            sb.append(" --with-serde ${serde.name.lowercase(Locale.getDefault())}")
        }

        if (skipDeserializingPrimaryKey) {
            sb.append(" --serde-skip-deserializing-primary-key ")
        }
        if (skipHiddenColumn) {
            sb.append(" --serde-skip-hidden-column ")
        }
        if (timeCrate != TimeCrate.Chrono) {
            sb.append(" --date-time-create ${timeCrate.name.lowercase(Locale.getDefault())}")
        }

        if (maxConnections > 0 && maxConnections != 1) {
            sb.append(" --max-connections $maxConnections")
        }
        if (modelExtraDerives) {
            sb.append(" --model-extra-derives ")
        }
        if (modelExtraAttributes) {
            sb.append(" --model-extra-attributes ")
        }
        if (enumExtraDerives) {
            sb.append(" --enum-extra-derives ")
        }
        if (enumExtraAttributes) {
            sb.append(" --enum-extra-attributes ")
        }
        if (seaography) {
            sb.append(" --seaography ")
        }
        return sb.toString()
    }

    override fun doOKAction() {
        super.doOKAction()
        SeaOrmCache.getInstance(project).loadState(config)
        runCommand(config, config.generateCommand())
    }

    override fun getHelpId(): String? {
        return MyWebHelpProvider.SEA_ORM_COMMAND
    }
}