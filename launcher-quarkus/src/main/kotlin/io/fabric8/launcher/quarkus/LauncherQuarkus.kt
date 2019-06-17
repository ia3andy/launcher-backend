package io.fabric8.launcher.quarkus

import io.fabric8.launcher.base.Paths
import io.fabric8.launcher.creator.core.ExtendedMavenCoords
import io.fabric8.launcher.creator.core.Runtime
import io.fabric8.launcher.creator.core.deploy.*
import io.fabric8.launcher.creator.core.propsOf
import io.fabric8.launcher.creator.core.propsOfNN
import io.fabric8.launcher.quarkus.model.QuarkusProject
import javax.validation.Valid
import javax.ws.rs.BeanParam
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/quarkus")
class LauncherQuarkus {

    @GET
    @Path("/download")
    @Produces("application/zip")
    fun download(@Valid @BeanParam params: QuarkusProject): Response {
        val deployment = DeploymentDescriptor.build {
            applications = mutableListOf(ApplicationDescriptor.build {
                application = params.name!!
                parts = mutableListOf(PartDescriptor.build {
                    shared = propsOfNN(
                        "runtime" to Runtime.build {
                            name = "quarkus"
                            version = "community"
                        },
                        "maven" to ExtendedMavenCoords.build {
                            artifactId = params.artifactId!!
                            groupId = params.groupId!!
                            description = params.description!!
                            packageName = params.packageName!!
                            dependencies = params.dependencies
                        }
                    )
                    capabilities = mutableListOf(CapabilityDescriptor.build { module = "health" })
                })
            })
        }
        val dir = createTempDir().toPath()
        try {
            applyDeployment(dir, deployment)
            val zip = Paths.zip(params.artifactId, dir)
            return Response
                .ok(zip)
                .type("application/zip")
                .header("Content-Disposition", "attachment; filename=\"${params.artifactId}.zip\"")
                .build()
        } finally {
            Paths.deleteDirectory(dir)
        }

    }

}