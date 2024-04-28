package shop.itbug.salvorstool.model

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import org.rust.lang.core.psi.RsMethodCall




enum class SalvoApiItemMethod {
    Get,
    Post,
    Update,
    Delete,
    Put
}

data class SalvoApiItem(val api: String,val method: SalvoApiItemMethod,val rsMethodPsiElement: RsMethodCall){
    override fun toString(): String {
        return "\n${api} - $method"
    }
}

fun SalvoApiItem.navTo(project: Project) {
    val navigationElement = rsMethodPsiElement.navigationElement
    ApplicationManager.getApplication().invokeLater {
        if (navigationElement != null && navigationElement is Navigatable && (navigationElement as Navigatable).canNavigate()) {
            (navigationElement as Navigatable).navigate(true)
        }else{
            FileEditorManager.getInstance(project).openFile(rsMethodPsiElement.containingFile.virtualFile)
        }
    }
}