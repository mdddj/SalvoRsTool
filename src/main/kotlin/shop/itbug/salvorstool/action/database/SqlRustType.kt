package shop.itbug.salvorstool.action.database

enum class SqlRustType(val typeText: String) {
    Text("text"),
    Integer("integer"),
    Float("real"),
    Int("int"),
    Bool("boolean"),
    Bigint("bigint"),
    Varchar255("varchar(255)"),
    Varchar20("varchar(20)"),
    Varchar("varchar"),
    Datetime("datetime(6)"),
    Longtext("longtext"),
    Bit1("bit(1)"),
    DatetimeText("datetime_text"),
    DateText("date_text"),
    Unknown("unknown");

    fun getRustTypeString(isOption: Boolean): String {
        val desc = when (this) {
            Text, Varchar255, Varchar20, Longtext,Varchar,DatetimeText,DateText -> "String"
            Integer, Bigint -> "i64"
            Unknown -> "Unknown"
            Float -> "f64"
            Int -> "i32"
            Bool,Bit1 -> "bool"
            Datetime -> "NaiveDateTime"
        }
        return if (isOption) "Option<$desc>" else desc
    }
}