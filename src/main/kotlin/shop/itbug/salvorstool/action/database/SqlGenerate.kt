package shop.itbug.salvorstool.action.database

import com.intellij.database.model.basic.BasicModTable
import shop.itbug.salvorstool.tool.structName

class SimpleSqlTableGenerate(val table: BasicModTable) : SqlTableGenerate(table)


abstract class SqlTableGenerate(private val table: BasicModTable) : SqlGenerate {
    override fun getColumns(): Array<SqlColumnModel> {
        return table.columns.map { SqlColumnModel(it.name, it.storedType.description, it.isNotNull) }.toTypedArray()
    }

    override fun getTableName(): String {
        return table.name
    }


    override fun generateRustStruct(): String {
        val sb = StringBuilder()
        sb.appendLine("pub struct ${table.name.structName} {")
        getColumns().forEach { column ->
            sb.appendLine("\t${column.generateRustPropertiesString()}")
        }
        sb.appendLine("}")
        return sb.toString()
    }


}

interface SqlGenerate {

    fun getColumns(): Array<SqlColumnModel>
    fun getTableName(): String
    fun generateRustStruct(): String
}


data class SqlColumnModel(

    ///列名称
    val name: String,
    ///列类型
    val type: String,
    ///是否可空
    val isNullable: Boolean,
) {
    ///构建rust属性
    fun generateRustPropertiesString(): String {
        if(rustType == SqlRustType.Unknown){
            println("Unknown :  $type")
        }
        return "pub ${getFinalName()}: ${rustType.getRustTypeString(isNullable)},"
    }

    private val rustType = SqlRustType.entries.find { it.typeText.lowercase() == type.lowercase() } ?: SqlRustType.Unknown

    private fun getFinalName() : String {
        return if(rustKeywords.contains(name)) "r#$name" else name
    }
}




val rustKeywords = arrayOf(
    // 保留关键字（常用）
    "as", "break", "const", "continue", "crate", "else", "enum", "extern",
    "false", "fn", "for", "if", "impl", "in", "let", "loop",
    "match", "mod", "move", "mut", "pub", "ref", "return", "self", "Self",
    "static", "struct", "super", "trait", "true", "type", "unsafe", "use",
    "where", "while",

    // 保留关键字（目前未使用）
    "abstract", "become", "box", "do", "final", "macro", "override",
    "priv", "typeof", "unsized", "virtual", "yield",

    // 2018 Edition 引入的新关键字
    "async", "await", "dyn",

    // 原始标识符关键字（可通过加前缀 `r#` 使用）
    "try", "union"
)