package coreBukkit.lib

import cf.wayzer.scriptAgent.Config
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getServer
import org.bukkit.Location
import org.bukkit.entity.Entity
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext

enum class Scheduler {
    AsyncScheduler,
    GlobalRegionScheduler,
    RegionScheduler,
    EntityScheduler
}

class BukkitDispatcher(
    private val type: Scheduler,
    private val location: Location? = null,
    private val entity: Entity? = null
) : CoroutineDispatcher() {
    @Volatile
    private var inBlocking = false
    private var blockingQueue = ConcurrentLinkedQueue<Runnable>()

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return !Bukkit.isPrimaryThread()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (inBlocking) {
            blockingQueue.add(block)
            return
        }

        when (type) {
            Scheduler.AsyncScheduler -> {
                getServer().asyncScheduler.runNow(Config.pluginMain) { block.run() }
            }

            Scheduler.GlobalRegionScheduler -> {
                getServer().globalRegionScheduler.run(Config.pluginMain) { block.run() }
            }

            Scheduler.RegionScheduler -> {
                if (location == null) {
                    throw NullPointerException("use RegionScheduler but no location has been provided")
                }
                getServer().regionScheduler.run(Config.pluginMain, location) { block.run() }
            }

            Scheduler.EntityScheduler -> {
                if (entity == null) {
                    throw NullPointerException("use Entity Scheduler but no Entity has been provided")
                }
                entity.scheduler.run(Config.pluginMain, { block.run() }, block)
            }
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun dispatchYield(context: CoroutineContext, block: Runnable) {
        when (type) {
            Scheduler.AsyncScheduler -> {
                getServer().asyncScheduler.runNow(Config.pluginMain) { block.run() }
            }

            Scheduler.GlobalRegionScheduler -> {
                getServer().globalRegionScheduler.run(Config.pluginMain) { block.run() }
            }

            Scheduler.RegionScheduler -> {
                if (location == null) {
                    throw NullPointerException("use RegionScheduler but no location has been provided")
                }
                getServer().regionScheduler.run(Config.pluginMain, location) { block.run() }
            }

            Scheduler.EntityScheduler -> {
                if (entity == null) {
                    throw NullPointerException("use Entity Scheduler but no Entity has been provided")
                }
                entity.scheduler.run(Config.pluginMain, { block.run() }, block)
            }
        }
    }

    fun <T> safeBlocking(block: suspend CoroutineScope.() -> T): T {
        return if (inBlocking) runBlocking(Dispatchers.game, block)
        else runBlocking {
            inBlocking = true
            launch {
                while (inBlocking || blockingQueue.isNotEmpty()) {
                    blockingQueue.poll()?.run() ?: yield()
                }
            }
            withContext(Dispatchers.game, block).also {
                inBlocking = false
            }
        }
    }

}

@Suppress("unused")
val Dispatchers.game
    get() = BukkitDispatcher(Scheduler.AsyncScheduler)

@Suppress("unused")
val Dispatchers.async
    get() = BukkitDispatcher(Scheduler.AsyncScheduler)

@Suppress("unused")
val Dispatchers.globalRegion
    get() = BukkitDispatcher(Scheduler.GlobalRegionScheduler)

@Suppress("unused")
fun Dispatchers.region(location: Location) = BukkitDispatcher(Scheduler.RegionScheduler, location = location)

@Suppress("unused")
fun Dispatchers.entity(entity: Entity) = BukkitDispatcher(Scheduler.EntityScheduler, entity = entity)
