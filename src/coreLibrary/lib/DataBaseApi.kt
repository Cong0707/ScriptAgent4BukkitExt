package coreLibrary.lib

import cf.wayzer.script_agent.IInitScript
import cf.wayzer.script_agent.util.DSLBuilder
import coreLibrary.lib.DataBaseApi.registeredTable
import coreLibrary.lib.util.Provider
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

object DataBaseApi {
    val IInitScript.registeredTable by DSLBuilder.dataKeyWithDefault { mutableSetOf<Table>() }
    val db = Provider<Database>()
}

/**
 * 为模块注册表格
 * 注册时不一定立刻运行
 * 会等[DataBaseApi.db]初始化后统一注册
 */
fun IInitScript.registerTable(vararg t: Table) {
    registeredTable.addAll(t)
    DataBaseApi.db.listenWithAutoCancel(this) {
        transaction(it) {
            SchemaUtils.createMissingTablesAndColumns(*t)
        }
    }
}