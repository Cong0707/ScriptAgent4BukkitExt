package main

import org.bukkit.Particle
import org.bukkit.Location

command("particles","粒子效果"){
    body{
        for (i in 0..20){
            delay(300)
            player!!.spawnParticle(
                Particle.LAVA,
                player!!.location,
                10,
                0.5,
                0.5,
                0.5,
                0.3
            )
        }
    }
}

