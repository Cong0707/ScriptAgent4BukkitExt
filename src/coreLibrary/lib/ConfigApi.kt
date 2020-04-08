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
import cf.wayzer.script_agent.IContentScript
import cf.wayzer.script_agent.util.DSLBuilder
import com.typesafe.config.*
import com.typesafe.config.Config
import io.github.config4k.ClassContainer
import io.github.config4k.TypeReference
import io.github.config4k.readers.SelectReader
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

open class ConfigBuilder(private val path: String) {
    /**
     * @param desc only display the first line using command
     */
    data class ConfigKey<T : Any>(val path: String, val default: T, val desc: List<String>) {
        fun get(): T {
            val v = fileConfig.extract(default::class,path) ?: return default
            if(default.javaClass.isInstance(v))
                return default.javaClass.cast(v)
            error("Wrong config type: $path get $v")
        }

        fun set(v: T) {
            fileConfig = if (v == default) {
                if (!fileConfig.hasPath(path)) return
                fileConfig.withoutPath(path)
            } else {
                fileConfig.withValue(path, ConfigValueFactory.fromAnyRef(v)
                        .withOrigin(ConfigOriginFactory.newSimple().withComments(desc)))
            }
            saveFile()
        }
        fun getString(): String {
            return ConfigFactory.parseMap(mapOf(path to get())).getValue(path).render()
        }

        /**
         * @return format like [getString]
         * @throws IllegalArgumentException when parse fail
         */
        fun setString(strV:String):String{
            val str = "$path = $strV"
            val v = ConfigFactory.parseString(str).extract(default::class,path)
            if(default.javaClass.isInstance(v)){
                set(default.javaClass.cast(v))
                return str
            }
            throw IllegalArgumentException("Parse \"$str\" fail: get $v")
        }

        operator fun getValue(thisRef: Any?, prop: KProperty<*>) = get()
        operator fun setValue(thisRef: Any?, prop: KProperty<*>, v: T) = set(v)
        companion object{
            /**
             * Copy from config4k as can't use reified param
             */
            fun <T:Any> Config.extract(cls: KClass<T>, path: String):Any?{
                if(!hasPath(path))return null
                val genericType = object : TypeReference<T>() {}.genericType()
                return SelectReader.getReader(ClassContainer(cls, genericType)).invoke(this,path)
            }
        }
    }

    fun child(sub: String) = ConfigBuilder("$path.$sub")
    fun <T:Any> key(default: T, vararg desc: String) = DSLBuilder.Companion.ProvideDelegate<IBaseScript,ConfigKey<T>>{script,name->
        val key = ConfigKey("$path.$name", default, desc.toList())
        script.configs.add(key)
        return@ProvideDelegate key
    }

    companion object {
        val IBaseScript.configs by DSLBuilder.dataKeyWithDefault { mutableSetOf<ConfigKey<*>>() }
        private lateinit var configFile: File
        private lateinit var fileConfig: Config
        fun init(configFile: File) {
            this.configFile = configFile
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
val IContentScript.config get() = ConfigBuilder("scripts.${clsName}")