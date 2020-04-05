@file:MavenDepends("cf.wayzer:PlaceHoldLib:2.0","https://dl.bintray.com/way-zer/maven/")
@file:MavenDepends("com.h2database:h2-mvstore:1.4.200")
@file:ImportByClass("kotlinx.coroutines.GlobalScope")
name="ScriptAgent 库模块"
/*
本模块实现一些平台无关的库
 */

addLibraryByClass("kotlinx.coroutines.GlobalScope")
addLibraryByClass("cf.wayzer.placehold.PlaceHoldApi")
addDefaultImport("coreLibrary.lib.*")
generateHelper()