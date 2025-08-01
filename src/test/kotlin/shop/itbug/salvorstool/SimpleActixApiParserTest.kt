package shop.itbug.salvorstool

import org.junit.Test
import shop.itbug.salvorstool.parser.SimpleActixApiParser

class SimpleActixApiParserTest {

    @Test
    fun testParserExists() {
        // This is a simple test to verify the parser class exists and can be instantiated
        val parser = SimpleActixApiParser()
        assert(parser != null)
    }
}
