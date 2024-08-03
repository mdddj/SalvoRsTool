package shop.itbug.salvorstool.json

import com.google.common.base.CaseFormat
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import java.math.BigDecimal
import kotlin.reflect.KType
import kotlin.reflect.full.createType

private fun isFloat(jsonPrimitive: JsonPrimitive): Boolean {
    if (jsonPrimitive.isNumber) {
        val stringValue = jsonPrimitive.asString
        try {
            stringValue.toFloat()
            return stringValue.contains(".")
        } catch (e: NumberFormatException) {
            return false
        }
    }
    return false
}

enum class KtRustType(private val ktType: KType, private val rustType: String) {
    //数字类型
    KtNumber(Int::class.java.kotlin.createType(), "i32"),

    //字符类型
    KtString(String::class.java.kotlin.createType(), "String"),

    //布尔类型
    KtBool(Boolean::class.java.kotlin.createType(), "bool"),

    //浮点类型
    KtFloat(BigDecimal::class.java.kotlin.createType(), "f64");

    override fun toString(): String {
        return "kotlin type is $ktType  rust type is $rustType"
    }


    fun getSeaOrmColumnType(): String {
        return when (this) {
            KtNumber -> "integer"
            KtString -> "string"
            KtBool -> "boolean"
            KtFloat -> "float"
        }
    }

    companion object {
        fun fromGsonType(type: JsonPrimitive): KtRustType? {
            if (isFloat(type)) {
                return KtFloat
            } else if (type.isNumber) {
                return KtNumber
            } else if (type.isString) {
                return KtString
            } else if (type.isBoolean) {
                return KtBool
            }
            return null
        }
    }


}


abstract class BaseJsonParse(private val jsonString: String) {

    companion object {
        fun createByJsonString(jsonString: String): BaseJsonParse {
            return object : BaseJsonParse(jsonString) {}
        }
    }


    /**
     * 驼峰转下划线,适合rust的命名规范
     * 例子: testName -> test_name
     */
    fun getRustName(name: String): String {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name)
    }


    fun getPairList(): List<Pair<String, KtRustType?>> {
        val json = JsonParser.parseString(jsonString)
        val jsonObjs = json.asJsonObject
        return jsonObjs.asMap().map { Pair(it.key, KtRustType.fromGsonType(it.value.asJsonPrimitive)) }
    }

}


///生成table
abstract class SeaOrmTableFactory(tableName: String) {

    private var table: String = tableName

    private val rustTableName: String get() = table.rustName()

    private val START = "manager.create_table("
    private val END = ").await"


    fun changeTableName(newTable: String) {
        this.table = newTable
    }

    private fun Pair<String, KtRustType?>.getSeaOrmColumn(): String {
        return ".col(ColumnDef::new(${rustTableName}::${this.first.rustName()}).${this.second?.getSeaOrmColumnType()}().not_null())"
    }

    ///表定义
    private fun getTableColumn(): String {
        return "\tTable::create().table(${rustTableName}::Table).if_not_exists()"
    }

    ///获取表ID定义
    private fun getTableIdColumn(): String {
        return "\t.col(ColumnDef::new($rustTableName::Id).integer().not_null().auto_increment().primary_key())"
    }

    ///获取column定义
    private fun getColumnBy(column: Pair<String, KtRustType?>): String {
        return column.getSeaOrmColumn()
    }

    ///获取column list
    private fun getColumns(columns: List<Pair<String, KtRustType?>>): String {
        val sb = StringBuilder()
        columns.forEach {
            val isLast = columns.last() == it
            sb.appendLine("\t" + getColumnBy(it) + if (isLast) ".to_owned()" else "")
        }
        return sb.toString()
    }

    fun generate(columns: List<Pair<String, KtRustType?>>): String {
        val sb = StringBuilder()
        sb.appendLine(START)
        sb.appendLine(getTableColumn())
        sb.appendLine(getTableIdColumn())
        sb.append(getColumns(columns))
        sb.appendLine(END)
        return sb.toString()
    }

    private fun getMigrationText(): String {
        return """
            #[derive(DeriveMigrationName)]
            pub struct Migration;
        """.trimIndent()
    }


    fun getUpText(columns: List<Pair<String, KtRustType?>>): String {
        val sb = StringBuilder()
        sb.appendLine("async fn up(&self, manager: &SchemaManager) -> Result<(), DbErr> {")
        sb.appendLine(generate(columns))
        sb.appendLine("}")
        return sb.toString()
    }

    fun getDownText(): String {
        return """
            async fn down(&self, manager: &SchemaManager) -> Result<(), DbErr> {
                manager.drop_table(Table::drop().table(${rustTableName}::Table).to_owned()).await
            }
        """.trimIndent()
    }

    fun getEnum(columns: List<Pair<String, KtRustType?>>): String {
        val fields = listOf(getSqlTableName(), getSqlTableId()) + getRustKeyNames(columns)
        val maxLength = fields.maxOf { it.length }
        fun String.padToMaxLength() = this.padEnd(maxLength)
        val fieldsString = fields.joinToString(",\n") { "\t" + it.padToMaxLength() }
        return """
#[derive(Iden)]
enum ${rustTableName}{
$fieldsString
}
        """.trimIndent()
    }

    ///table
    private fun getSqlTableName(): String {
        return "Table"
    }

    ///主键ID
    private fun getSqlTableId(): String {
        return "Id"
    }

    private fun getRustKeyNames(columns: List<Pair<String, KtRustType?>>): List<String> {
        return columns.map { it.first.rustName() }
    }

    fun generateAll(columns: List<Pair<String, KtRustType?>>): String {
        val sb = StringBuilder()
        sb.appendLine("use sea_orm_migration::prelude::*;")
        sb.appendLine()
        sb.appendLine(getMigrationText())
        sb.appendLine()
        sb.appendLine("#[async_trait::async_trait]")
        sb.appendLine("impl MigrationTrait for Migration {")
        sb.appendLine()
        sb.appendLine(getUpText(columns))
        sb.appendLine(getDownText())
        sb.appendLine()
        sb.appendLine("}")

        sb.appendLine()
        sb.appendLine(getEnum(columns))
        return sb.toString()
    }

}

val testJsonString = """
        {
          "name":"Rust",
          "age": 18,
          "isOk":true,
          "price":43.2
        }
    """.trimIndent()

fun main() {


    val test = object : BaseJsonParse(testJsonString) {
    }
    println(test.getRustName("testName"))
    println(test.getPairList())
    val cloumns = test.getPairList()
    val table = object : SeaOrmTableFactory("order") {}
    val all = table.generate(cloumns)
    println(all)
}

/// testName -> TestName 名称转换
private fun String.rustName(): String {
    val f = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, this)
    return f
}

