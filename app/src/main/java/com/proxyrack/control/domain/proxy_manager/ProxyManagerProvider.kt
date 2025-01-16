package com.proxyrack.control.domain.proxy_manager

interface ProxyManagerProvider {
    fun new(): ProxyManager
}