import com.github.furrrlo.jpackage.JPackagePlugin
import com.github.furrrlo.jpackage.WriteJPackageWixOverridesTask
import org.beryx.jlink.data.JPackageData
import org.gradle.api.Action
import org.gradle.api.Project

fun JPackageData.wix(project: Project, action: Action<in WriteJPackageWixOverridesTask>) {
    project.tasks.named(JPackagePlugin.JPACKAGE_WIX_GEN_OVERRIDES, WriteJPackageWixOverridesTask::class.java, action)
}