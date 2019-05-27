package io.fabric8.launcher.creator.catalog.generators

import io.fabric8.launcher.creator.core.*
import io.fabric8.launcher.creator.core.catalog.BaseGenerator
import io.fabric8.launcher.creator.core.catalog.CatalogItemContext
import io.fabric8.launcher.creator.core.catalog.enumItemNN
import io.fabric8.launcher.creator.core.resource.*
import io.fabric8.launcher.creator.core.template.transformers.cases

interface PlatformReactProps : LanguageNodejsProps {
    val runtime: Runtime
    val nodejs: NodejsCoords

    companion object {
        fun build(_map: Properties = propsOf(), block: Data.() -> Unit = {}) =
            BaseProperties.build(::Data, _map, block)
    }

    open class Data(map: Properties = propsOf()) : LanguageNodejsProps.Data(map), PlatformReactProps {
        override val runtime: Runtime by _map
        override val nodejs: NodejsCoords by _map
    }
}

class PlatformReact(ctx: CatalogItemContext) : BaseGenerator(ctx) {
    override fun apply(resources: Resources, props: Properties, extra: Properties): Resources {
        val prprops = PlatformReactProps.build(props)
        val lprops = LanguageNodejsProps.build(prprops) {
            builderImage = BUILDER_NODEJS_WEB
        }

        // Check if the service already exists, so we don't create it twice
        if (resources.service(prprops.serviceName) == null) {
            generator(::PlatformBaseSupport).apply(resources, prprops, extra)
            copy()
            transform(listOf("package.json"), cases(prprops))
        }
        val res = generator(::LanguageNodejs).apply(resources, lprops, extra)
        setMemoryLimit(resources, "100Mi", prprops.serviceName)
        setCpuLimit(resources, "200m", prprops.serviceName)
        setPathHealthChecks(resources, "/", "/", prprops.serviceName)

        val exProps = propsOf(
                "image" to BUILDER_NODEJS_WEB,
                "enumInfo" to enumItemNN("runtime.name", "react"),
                "service" to prprops.serviceName,
                "route" to prprops.routeName
        )
        extra.pathPut("shared.runtimeInfo", exProps)

        return res
    }
}
