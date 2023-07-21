package cong.ext

import coreBukkit.lib.listen

name = "欢迎信息"

listen<org.bukkit.event.player.PlayerJoinEvent>{
    it.player.sendRawMessage("欢迎来到 梦魔短祷 Minecraft 服务器")
}