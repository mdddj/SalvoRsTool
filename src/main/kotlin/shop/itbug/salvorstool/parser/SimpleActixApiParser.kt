package shop.itbug.salvorstool.parser

import com.intellij.psi.PsiFile
import shop.itbug.salvorstool.model.ActixApiModel

class SimpleActixApiParser {

    fun parse(file: PsiFile): List<ActixApiModel> {
        val apiModels = mutableListOf<ActixApiModel>()

        // This is a simplified parser implementation
        // In a real implementation, you would traverse the PSI tree
        // and extract the relevant information

        return apiModels
    }
}
