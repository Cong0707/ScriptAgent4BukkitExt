@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package coreLibrary.lib

/**
 * 配置Api
 * 用于定义脚本的配置项
 * 配置项可在文件中或者使用指令修改
 * @sample
 * val welcomeMsg by config.key("Hello Steve","The message show when player join")
 * println(welcomeMsg)
 */
import cf.wayzer.script_agent.IBaseScript
import cf.wayzer.script_agent.util.DSLBuilder
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigOriginFactory
import com.typesafe.config.ConfigRenderOptions
import io.github.config4k.ClassContainer
import io.github.config4k.TypeReference
import io.github.config4k.readers.SelectReader
import io.github.config4k.toConfig
import java.io.File
import kotlin.reflect.KProperty

open class ConfigBuilder(private val path: String) {
    /**
     * @param desc only display the first line using command
     */
    data class ConfigKey<T : Any>(val path: String, val cls: ClassContainer, val default: T, val desc: List<String>) {

        fun get(): T {
            val v = fileConfig.extract(cls, path) ?: return default
            @Suppress("UNCHECKED_CAST")
            if (cls.mapperClass.isInstance(v))
                return v as T
            error("Wrong config type: $path get $v")
        }

        fun set(v: T) {
            fileConfig=fileConfig.withValue(path, v.toConfig(path).getValue(path)
                    .withOrigin(ConfigOriginFactory.newSimple().withComments(desc)))
            saveFile()
        }

        /**
         * 清除设定值
         */
        fun reset() {
            if (!fileConfig.hasPath(path)) return
            fileConfig = fileConfig.withoutPath(path)
            saveFile()
        }

        /**
         * 写入默认值到文件中
         */
        fun writeDefault() {
            set(default)
        }

        fun getString(): String {
            return get().toConfig(path).getValue(path).render()
        }

        /**
         * @return format like [getString]
         * @throws IllegalArgumentException when parse fail
         */
        fun setString(strV: String): String {
            val str = "$path = $strV"
            val v = ConfigFactory.parseString(str).extract(cls, path)
            if (cls.mapperClass.isInstance(v)) {
                @Suppress("UNCHECKED_CAST")
                set(v as T)
                return str
            }
            throw IllegalArgumentException("Parse \"$str\" fail: get $v")
        }

        operator fun getValue(thisRef: Any?, prop: KProperty<*>) = get()
        operator fun setValue(thisRef: Any?, prop: KProperty<*>, v: T) = set(v)

        companion object {
            /**
             * Copy from config4k as can't use reified param
             */
            fun Config.extract(cls: ClassContainer, path: String): Any? {
                if (!hasPath(path)) return null
                return SelectReader.getReader(cls).invoke(this, path)
            }
        }
    }

    fun child(sub: String) = ConfigBuilder("$path.$sub")
    fun <T : Any> key(cls: ClassContainer, default: T, vararg desc: String) = DSLBuilder.Companion.ProvideDelegate<IBaseScript, ConfigKey<T>> { script, name ->
        val key = ConfigKey("$path.$name", cls, default, desc.toList())
        script.configs.add(key)
        all[key.path] = key
        return@ProvideDelegate key
    }

    inline fun <reified T : Any> key(default: T, vararg desc: String): DSLBuilder.Companion.ProvideDelegate<IBaseScript, ConfigKey<T>> {
        val genericType = object : TypeReference<T>() {}.genericType()
        return key(ClassContainer(T::class, genericType), default, *desc)
    }

    companion object {
        val IBaseScript.configs by DSLBuilder.dataKeyWithDefault { mutableSetOf<ConfigKey<*>>() }
        val all = mutableMapOf<String, ConfigKey<*>>()
        var configFile: File = cf.wayzer.script_agent.Config.dataDirectory.resolve("config.conf")
        private lateinit var fileConfig: Config
        init {
            reloadFile()
        }

        fun reloadFile() {
            fileConfig = ConfigFactory.parseFile(configFile)
        }

        fun saveFile() {
            configFile.writeText(fileConfig.root().render(ConfigRenderOptions.defaults().setOriginComments(false)))
        }
    }
}

val globalConfig = ConfigBuilder("global")
val IBaseScript.config get() = ConfigBuilder(id.replace('/', '.'))