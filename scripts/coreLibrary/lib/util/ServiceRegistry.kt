package coreLibrary.lib.util

import cf.wayzer.scriptAgent.define.Script
import cf.wayzer.scriptAgent.emitAsync
import cf.wayzer.scriptAgent.util.DSLBuilder
import coreLibrary.lib.event.ServiceProvidedEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.properties.ReadOnlyProperty

/**
 * 模块化服务提供工具库
 */

@Suppress("unused")
open class ServiceRegistry<T : Any> {
    private val impl = MutableSharedFlow<T>(1, 0, BufferOverflow.DROP_OLDEST)
    private val mutex = Mutex()

    suspend fun provide(script: Script, inst: T) {
        script.providedService.add(this to inst)
        this.impl.emit(inst)
        ServiceProvidedEvent(inst, script).emitAsync()

        @OptIn(ExperimentalCoroutinesApi::class)
        script.onDisable {
            if (getOrNull() == inst)
                impl.resetReplayCache()
        }
    }

    fun getOrNull() = impl.replayCache.firstOrNull()
    fun get() = getOrNull() ?: error("No Provider for ${this.javaClass.canonicalName}")

    val provided get() = getOrNull() != null
    fun toFlow() = impl.asSharedFlow()

    suspend fun awaitInit() = impl.first()

    @JvmOverloads
    fun subscribe(scope: CoroutineScope, async: Boolean = false, body: suspend (T) -> Unit) {
        impl.onEach {
            if (async) body(it)
            else mutex.withLock { body(it) }
        }.launchIn(scope)
    }

    val nullable get() = ReadOnlyProperty<Any?, T?> { _, _ -> getOrNull() }
    val notNull get() = ReadOnlyProperty<Any?, T> { _, _ -> get() }

    companion object {
        val Script.providedService by DSLBuilder.dataKeyWithDefault { mutableSetOf<Pair<ServiceRegistry<*>, *>>() }
    }
}