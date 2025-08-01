package shop.itbug.salvorstool

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.rust.lang.core.psi.RsFile
import shop.itbug.salvorstool.parser.ActixApiModelParser


class ActixApiModelParserTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String? = "src/test/testData"

    val mainRsFile get() = myFixture.configureByFile("main.rs") as RsFile

    // actix 模型列表获取
    fun testParse() {
        val parse = ActixApiModelParser()
        val models = parse.parse(mainRsFile)
        assertTrue(models.isNotEmpty())
        models.forEach {
            assertTrue(it.psiFile != null)
            assertNotNull(it.module)
        }

    }




}
