[ ![Download](https://api.bintray.com/packages/way-zer/maven/cf.wayzer%3AScriptAgent4Bukkit/images/download.svg) ](https://bintray.com/way-zer/maven/cf.wayzer%3AScriptAgent4Bukkit/_latestVersion)
# ScriptAgent for Bukkit
一个强大的脚本插件,基于kts定义的DSL

## 特性
- 强大,基于kotlin,可以访问所有Java接口(所有插件能干的，脚本都能干)
- 快速,脚本加载完成后，转换为jvm字节码，和java插件没有性能差距
- 灵活,模块与脚本都有完整的生命周期，随时可进行热加载和热重载
- 快速,一大堆开发常用的辅助函数,无需编译,即可快速部署到服务器
- 智能,开发时,拥有IDEA(或AndroidStudio)的智能补全
- 可定制,插件除核心部分外,均使用脚本实现,可根据自己需要进行修改,另外,模块定义脚本也可以为脚本扩充DSL
## 插件安装
1. 下载jar主体，见上方下载标签
2. 安装插件
3. 安装脚本,直接放置(src内的文件和文件夹)到插件配置目录(plugins/ScriptAgent/)下
### 基本指令/ScriptAgent(/sa)
- help 列出所有子指令
- module,reload,list,load (见help,来自main/control脚本,权限: ScriptAgent.admin)
## 开发脚本
1. 拷贝本仓库(或自行配置gradle,见build.gradle.kts)
2. 在IDEA导入该项目(建议导入为Project,避免干扰)
3. 同步Gradle(.metadata文件需要在服务端才能生成,使用现有的也可)
## 基本结构
- main.init.kts (模块定义脚本)
- main(模块根目录)
    - lib(模块库目录,可放置kt,被模块所有脚本所共用,与模块同一生命周期)
    - .metadata(模块元数据,供IDE和其他编译器分析编译使用,插件运行时可以生成)
    - commands.content.kts(普通脚本)
### 两种脚本
#### 通用属性
两种脚本都具有的功能
```kotlin
@file:ImportByClass("org.bukkit.Bukkit") //导入已加载的库(常依赖引用其他插件)
@file:MavenDepends("de.tr7zw:item-nbt-api:2.2.0","https://repo.codemc.org/repository/maven-public/") //导入Maven依赖(不存在时，自动下载，不会解析依赖关系)
@file:ImportScript("") //导入其他源码(常引用模块库外的脚本库,与脚本同生命周期)
//一些属性
name.set("SuperItem 模块")//设置当前脚本名字(仅为了方便辨别)
val enabled:Boolean
sourceFile.get()//获取当前脚本源文件(不建议使用)
//生命周期
onEnable{}
onDisable{}
```
#### init.kts(模块定义脚本)
主要负责为子脚本扩充定义，提供自定义的DSL  
可以使用扩展函数(属性)和DSLKey进行扩展  
再在生命周期函数内,为子脚本注册或取消
```kotlin
import cf.wayzer.script_agent.bukkit.Helper.baseConfig
import cf.wayzer.script_agent.bukkit.Helper.exportClass
addLibraryByClass("de.tr7zw.changeme.nbtapi.NBTItem")//类似ImportByClass,目标为子脚本
addLibrary(File("xxxx"))//为子脚本导入库文件
addLibraryByName("xxxx")//提供名字去查找依赖库,例: kotlin-stdlib
addDefaultImport("superitem.lib.*")//添加默认导入,子脚本无需再import
exportClass(SuperItemEvent::class.java)//暴露类到Bukkit共享域(暂不建议使用)
baseConfig()//Bukkit的一些基础的DSL
generateHelper()//生成元数据(运行时)

children.get() //获取所有子脚本实例
//与子脚本有关的生命周期函数
onBeforeContentEnable{script-> }
onAfterContentEnable{script-> }
onBeforeContentDisable{script-> }
onAfterContentDisable{script-> }
```
#### content.kts(模块内容脚本)
插件功能的主要承担者
```kotlin
module.get() //获取模块定义脚本的实例(不建议使用)
//插件内部接口
import cf.wayzer.script_agent.bukkit.Manager
Manager.pluginMain //获取插件类实例(某些Bukkit接口需要)
Manager.scriptManager //获取脚本主管理器(不建议使用)
//Bukkit基础DSL
this.logger
this.PlaceHoldApi //很有用，可用于在整个插件范围内共享变量或者暴露函数接口
command(name,description,usage="",aliases=emptyList(),sub=true){sender,arg->} //注册指令(sub表示为子指令,注册在/sa 下)
listen<Event>{e-> } //监听事件
registerAsyncTask(name){firstRun->} //注册异步任务,插件管理一个独立线程运行,内部不允许直接访问Bukkit接口
//辅助函数
createBukkitTask{ "DoSomething" } //创建Bukkit Task,需要手动开始
getScheduleTask(name).start(param) //与registerAsyncTask对应

```
### 注意事项
1. 重载脚本后，同一个类不一定相同，注意控制生命周期  
    如需要类似操作,可以在更长的生命周期建立抽象接口存储变量
## 已有模块
- main(主模块，含基础DSL)
- superitem(制作特殊道具的模块, 移植自[SuperItem插件](https://github.com/way-zer/SuperItem))
## 版权
- 插件本体：未经许可禁止转载和用作其他用途
- 脚本：归属脚本制作者，本仓库脚本转载需注明本页面链接