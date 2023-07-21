package cong.ext

import cf.wayzer.placehold.PlaceHoldApi.with
import coreBukkit.lib.command
import coreBukkit.lib.player

name = "史莱姆区块查询器"

command("slime","查询史莱姆区块"){
    body{
        val slimeChunk = player!!.location.chunk.isSlimeChunk
        if (slimeChunk) {
            reply("此区块为史莱姆区块".with())
        }else{
            reply("此区块非史莱姆区块".with())
        }
    }
}

