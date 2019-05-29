package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.catalog.enumItemNN
import io.fabric8.launcher.creator.core.resource.BUILDER_JAVA
import io.fabric8.launcher.creator.core.resource.Resources
import io.quarkus.cli.commands.AddExtensions
import io.quarkus.cli.commands.CreateProject
import io.quarkus.cli.commands.writer.FileProjectWriter
import java.io.IOException


interface RuntimeQuarkusProps : LanguageJavaProps, MavenSetupProps {
    val runtime: Runtime

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : LanguageJavaProps.Data(map), RuntimeQuarkusProps {
        override val runtime: Runtime by _map
        override var maven: MavenCoords by _map
    }
}

class RuntimeQuarkus(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val pqprops = RuntimeQuarkusProps.build(props)
        val jarName = pqprops.maven.artifactId + "-runner.jar"
        val newenv = envOf(
            pqprops.env,
            "JAVA_APP_JAR" to jarName,
            "ARTIFACT_COPY_ARGS" to "-p -r lib/ $jarName"
        )
        val lprops = propsOf(
            pqprops,
            "env" to newenv,
            "jarName" to jarName,
            "builderImage" to BUILDER_JAVA,
            "buildArgs" to "-DuberJar=true"
        )

        // Check if the service already exists, so we don't create it twice
        if (resources.service(pqprops.serviceName) == null) {
            val writer = FileProjectWriter(this.targetDir.toFile())
            writer.use {
                println(pqprops.maven)
                val success = CreateProject(writer)
                    .groupId(pqprops.maven.groupId)
                    .artifactId(pqprops.maven.artifactId)
                    .doCreateProject(mutableMapOf())
                if(!success) {
                    throw IOException("Error during Quarkus project creation")
                }
                AddExtensions(writer, "pom.xml")
                    .addExtensions(mutableSetOf("kotlin"))
            }
        }
        //generator(::LanguageJava).apply(resources, lprops, extra)

        val exProps = propsOf(
                "image" to BUILDER_JAVA,
                "enumInfo" to enumItemNN("runtime.name", "quarkus"),
                "service" to pqprops.serviceName,
                "route" to pqprops.routeName
        )
        extra.pathPut("shared.runtimeInfo", exProps)

        return resources
    }
}
