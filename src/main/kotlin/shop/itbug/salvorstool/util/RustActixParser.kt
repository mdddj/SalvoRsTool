package shop.itbug.salvorstool.util

import java.io.File
import shop.itbug.salvorstool.model.ActixApiModel
import shop.itbug.salvorstool.model.ActixApiModelMethod

class RustActixParser {

    fun parseActixApis(rustCode: String): List<ActixApiModel> {
        val apis = mutableListOf<ActixApiModel>()

        // Split the code into lines for easier processing
        val lines = rustCode.lines()

        var currentFunctionName = ""
        var currentUrl = ""
        var currentMethod = ActixApiModelMethod.Unknown
        var inFunction = false
        var functionStartLine = 0

        for (i in lines.indices) {
            val line = lines[i].trim()

            // Check for HTTP method annotations
            if (line.startsWith("#[") && line.contains("(") && line.contains(")")) {
                val methodName = extractMethodName(line)
                val url = extractUrl(line)

                if (methodName != null && url != null) {
                    currentMethod = getMethodEnum(methodName)
                    currentUrl = url
                }
            }

            // Check for function definitions
            if (line.startsWith("async fn") || line.startsWith("fn")) {
                inFunction = true
                functionStartLine = i
                currentFunctionName = extractFunctionName(line) ?: ""
            }

            // End of function detection
            if (inFunction && line == "}") {
                // Add the API endpoint if we have all the information
                if (currentFunctionName.isNotEmpty() && currentUrl.isNotEmpty()) {
                    apis.add(
                            ActixApiModel(
                                    url = currentUrl,
                                    method = currentMethod,
                                    functionElement = null,
                                    comment = null
                            )
                    )
                }

                // Reset for next function
                currentFunctionName = ""
                currentUrl = ""
                currentMethod = ActixApiModelMethod.Unknown
                inFunction = false
            }
        }

        return apis
    }

    private fun extractMethodName(line: String): String? {
        val pattern = """#\[(get|post|put|delete|patch|head)\(""".toRegex()
        val match = pattern.find(line)
        return match?.groupValues?.get(1)
    }

    private fun extractUrl(line: String): String? {
        val pattern = """"([^"]+)"""".toRegex()
        val match = pattern.find(line)
        return match?.groupValues?.get(1)
    }

    private fun extractFunctionName(line: String): String? {
        val pattern = """fn\s+(\w+)""".toRegex()
        val match = pattern.find(line)
        return match?.groupValues?.get(1)
    }

    private fun getMethodEnum(methodName: String): ActixApiModelMethod {
        return when (methodName.lowercase()) {
            "get" -> ActixApiModelMethod.Get
            "post" -> ActixApiModelMethod.Post
            "put" -> ActixApiModelMethod.Put
            "delete" -> ActixApiModelMethod.Delete
            "patch" -> ActixApiModelMethod.Patch
            "head" -> ActixApiModelMethod.Head
            else -> ActixApiModelMethod.Unknown
        }
    }

    fun parseFromFile(filePath: String): List<ActixApiModel> {
        val file = File(filePath)
        if (!file.exists()) {
            return emptyList()
        }
        return parseActixApis(file.readText())
    }
}
