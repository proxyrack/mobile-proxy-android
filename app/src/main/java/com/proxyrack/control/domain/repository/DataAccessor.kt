package com.proxyrack.control.domain.repository

interface DataAccessor {
    suspend fun get(): String
    suspend fun set(value: String)
}