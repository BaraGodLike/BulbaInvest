package com.bulbainvest.gateway.config

import io.ktor.server.config.ApplicationConfig

data class AppConfig(
    val services: ServicesConfig,
    val httpClient: HttpClientConfig,
)

data class ServicesConfig(
    val domain: ServiceConfig,
)

data class ServiceConfig(
    val baseUrl: String,
)

data class HttpClientConfig(
    val requestTimeoutMillis: Long,
    val connectTimeoutMillis: Long,
    val socketTimeoutMillis: Long,
)

fun ApplicationConfig.loadAppConfig(): AppConfig = AppConfig(
    services = ServicesConfig(
        domain = ServiceConfig(
            baseUrl = System.getenv("DOMAIN_SERVICE_URL")
                ?: property("services.domain.baseUrl").getString(),
        ),
    ),
    httpClient = HttpClientConfig(
        requestTimeoutMillis = System.getenv("HTTP_CLIENT_REQUEST_TIMEOUT_MS")?.toLong()
            ?: property("httpClient.requestTimeoutMillis").getString().toLong(),
        connectTimeoutMillis = System.getenv("HTTP_CLIENT_CONNECT_TIMEOUT_MS")?.toLong()
            ?: property("httpClient.connectTimeoutMillis").getString().toLong(),
        socketTimeoutMillis = System.getenv("HTTP_CLIENT_SOCKET_TIMEOUT_MS")?.toLong()
            ?: property("httpClient.socketTimeoutMillis").getString().toLong(),
    ),
)
