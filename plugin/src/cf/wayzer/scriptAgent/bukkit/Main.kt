package cf.wayzer.scriptAgent.bukkit

import cf.wayzer.scriptAgent.*
import cf.wayzer.scriptAgent.define.LoaderApi
import cf.wayzer.scriptAgent.define.ScriptInfo
import kotlinx.coroutines.runBlocking
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.JavaPluginLoader
import java.io.File

@Suppress("unused")
@OptIn(LoaderApi::class)
class Main(private val loader: JavaPlugin, file: File) : JavaPlugin(
    loader.pluginLoader as JavaPluginLoader, loader.description, loader.dataFolder, file
) {
    init {
        val classLoader = loader.javaClass.classLoader
        classLoader::class.java.getDeclaredField("remapper").apply {
            isAccessible = true
            set(classLoader, null)
        }
        if (!dataFolder.exists()) dataFolder.mkdirs()
    }

    private var mainScript: ScriptInfo? = null

    override fun onLoad() {
        val defaultMain = "main/bootStrap"
        val version = description.version
        val main = System.getenv("SAMain") ?: defaultMain
        logger.info("SAMain=$main")

        Config.logger = logger
        Config.rootDir = dataFolder
        Config.version = version
        Config.mainScript = main
        Config.pluginMain = loader

        tryExtract("/res/$defaultMain.kts", Config.rootDir.resolve("$defaultMain.kts"))
        tryExtract("/res/$defaultMain.ktc", Config.cacheFile(defaultMain, false))
        ScriptRegistry.scanRoot()

        mainScript = ScriptRegistry.findScriptInfo(main)
        if (mainScript != null) runBlocking {
            ScriptManager.transaction {
                add(mainScript!!);load()
            }
        }
    }

    override fun onEnable() {
        Config.pluginCommand = loader.getCommand("ScriptAgent")

        if (mainScript != null) runBlocking {
            ScriptManager.transaction {
                add(mainScript!!);enable()
            }
        }
        logger.info("===========================")
        logger.info("     ScriptAgent ${Config.version}         ")
        logger.info("           By WayZer    ")
        logger.info("插件官网: https://github.com/way-zer/ScriptAgent4BukkitExt")
        logger.info("QQ交流群: 1033116078")
        if (mainScript == null)
            logger.warning("未找到启动脚本(SAMain=${Config.mainScript}),请下载安装脚本包,以发挥本插件功能")
        else {
            val all = ScriptRegistry.allScripts { true }
            logger.info(
                "共找到${all.size}脚本," +
                        "加载成功${all.count { it.scriptState.loaded }}," +
                        "启用成功${all.count { it.scriptState.enabled }}," +
                        "出错${all.count { it.failReason != null }}"
            )
        }
        logger.info("===========================")
    }

    override fun onDisable() {
        runBlocking {
            ScriptManager.disableAll()
        }
    }

    private fun tryExtract(from: String, to: File) {
        if (to.exists()) return
        to.parentFile.mkdirs()
        val internal = javaClass.classLoader.getResourceAsStream(from) ?: return
        to.writeBytes(internal.readBytes())
    }
}