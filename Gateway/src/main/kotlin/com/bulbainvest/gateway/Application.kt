package com.bulbainvest.gateway

import com.bulbainvest.gateway.application.ProxyUseCase
import com.bulbainvest.gateway.config.AppConfig
import com.bulbainvest.gateway.config.appModule
import com.bulbainvest.gateway.config.loadAppConfig
import com.bulbainvest.gateway.infrastructure.DefaultDownstreamServiceRegistry
import com.bulbainvest.gateway.infrastructure.KtorDownstreamProxyClient
import com.bulbainvest.gateway.plugins.configureSerialization
import com.bulbainvest.gateway.plugins.configureStatusPages
import com.bulbainvest.gateway.presentation.domainProxyRoutes
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

data class GatewayDependencies(
    val proxyUseCase: ProxyUseCase,
)

@Suppress("unused")
fun Application.module() {
    val cfg: AppConfig = environment.config.loadAppConfig()

    install(Koin) {
        slf4jLogger()
        modules(appModule(cfg))
    }

    gatewayModule(
        GatewayDependencies(
            proxyUseCase = get(),
        )
    )
}

fun Application.gatewayModule(
    dependencies: GatewayDependencies,
) {
    install(CallLogging)
    install(CORS) {
        anyHost()
        allowHeader(io.ktor.http.HttpHeaders.ContentType)
        allowHeader(io.ktor.http.HttpHeaders.Authorization)
        io.ktor.http.HttpMethod.DefaultMethods.forEach { allowMethod(it) }
    }

    configureSerialization()
    configureStatusPages()

    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }

        domainProxyRoutes(dependencies.proxyUseCase)
    }
}
