package com.bulbainvest.gateway.config

import com.bulbainvest.gateway.application.ProxyUseCase
import com.bulbainvest.gateway.domain.DownstreamProxyClient
import com.bulbainvest.gateway.domain.DownstreamServiceRegistry
import com.bulbainvest.gateway.infrastructure.DefaultDownstreamServiceRegistry
import com.bulbainvest.gateway.infrastructure.KtorDownstreamProxyClient
import org.koin.dsl.module

fun appModule(cfg: AppConfig) = module {
    single { cfg }
    single<DownstreamServiceRegistry> { DefaultDownstreamServiceRegistry(cfg) }
    single<DownstreamProxyClient> { KtorDownstreamProxyClient(cfg.httpClient) }
    single { ProxyUseCase(get(), get()) }
}
