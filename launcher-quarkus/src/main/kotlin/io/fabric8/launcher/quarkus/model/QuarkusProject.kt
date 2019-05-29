package io.fabric8.launcher.quarkus.model

import javax.validation.constraints.NotBlank
import javax.ws.rs.QueryParam


class QuarkusProject {
    @NotBlank
    @QueryParam("groupId")
    val groupId: String? = null

    @NotBlank
    @QueryParam("artifactId")
    val artifactId: String? = null

    @NotBlank
    @QueryParam("name")
    val name: String? = null

    @NotBlank
    @QueryParam("description")
    val description: String? = null

    @NotBlank
    @QueryParam("packageName")
    val packageName: String? = null

    @QueryParam("dependencies")
    val dependencies: List<String> = listOf()
}