package shop.itbug.salvorstool.action.database

import com.intellij.database.model.basic.BasicModTable
import com.intellij.util.containers.JBIterable

object SqlGenerateFactory {

    fun from(selects: JBIterable<BasicModTable>): List<SqlTableGenerate> {
        return selects.map { SimpleSqlTableGenerate(it) }.toList()
    }
}