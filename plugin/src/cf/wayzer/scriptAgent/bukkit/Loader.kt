package cf.wayzer.scriptAgent.bukkit

import cf.wayzer.libraryManager.Dependency
import cf.wayzer.libraryManager.LibraryManager
import cf.wayzer.scriptAgent.ScriptAgent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class Loader : JavaPlugin() {
    private val impl

    init {
        val libraryPath = Paths.get("libs")
        //Copy from ScriptAgent.loadUseClassLoader, with added self first support for guava
        val stdlib = Dependency("org.jetbrains.kotlin:kotlin-stdlib:${ScriptAgent.kotlinVersion}")
        val classLoader = LibraryManager(libraryPath).apply {
            addAliYunMirror()
            require(stdlib)
            require(Dependency("org.jetbrains.kotlin:kotlin-reflect:${ScriptAgent.kotlinVersion}"))
        }.createSelfFirstClassloader(null) {
            if (it == null) return@createSelfFirstClassloader false
            (it as java.lang.String).startsWith("cf.wayzer.scriptAgent")
                    || (it as java.lang.String).startsWith("com.google.common")
                    || (it as java.lang.String).startsWith("io.netty")
        }?.apply {
            loadClass("cf.wayzer.scriptAgent.ScriptAgent")
                .getMethod("afterStdLib", Path::class.java)
                .invoke(null, libraryPath)
        }
        impl = classLoader?.loadClass("cf.wayzer.scriptAgent.bukkit.Main")

            ?.getConstructor(JavaPlugin::class.java, ClassLoader::class.java)

            ?.newInstance(this, classLoader)
    }

    override fun onLoad() {
        impl.onLoad()
    }

    override fun onEnable() {
        impl.onEnable()
    }

    override fun onDisable() {
        impl.onDisable()
    }
}
