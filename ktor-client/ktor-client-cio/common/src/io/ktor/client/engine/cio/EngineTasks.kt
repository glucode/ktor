/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.client.engine.cio

import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.date.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

internal data class RequestTask(
    val request: HttpRequestData,
    val response: CompletableDeferred<HttpResponseData>,
    val context: CoroutineContext
)

internal fun HttpRequestData.requiresDedicatedConnection(): Boolean = listOf(headers, body.headers).any {
    it[HttpHeaders.Connection] == "close" || it.contains(HttpHeaders.Upgrade)
} || method !in listOf(HttpMethod.Get, HttpMethod.Head) || containsCustomTimeouts()

internal data class ConnectionResponseTask(
    val requestTime: GMTDate,
    val task: RequestTask
)

/**
 * Return true if request task contains timeout attributes specified using [HttpTimeout] plugin.
 */
private fun HttpRequestData.containsCustomTimeouts() = getCapabilityOrNull(HttpTimeout)?.let {
    it.connectTimeoutMillis != null || it.socketTimeoutMillis != null
} == true
