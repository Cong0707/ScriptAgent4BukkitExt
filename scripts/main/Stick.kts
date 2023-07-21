import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.math.cos
import kotlin.math.sin


listen<PlayerInteractEvent> {
    if (it.action == Action.RIGHT_CLICK_AIR || it.action == Action.RIGHT_CLICK_AIR || it.item != null) {
        val item = it.item!!
        if (item.itemMeta.hasDisplayName()) {
            if (item.itemMeta.displayNameComponent[0].toPlainText().toString() == "小木棍") {

                val world = it.player.location.world
                val yaw = it.player.location.yaw
                val pitch = it.player.location.pitch
                val ty = cos(Math.toRadians((0 - pitch.toDouble())))

                val vx = sin(Math.toRadians(0 - yaw.toDouble())) * ty * 50
                val vy = sin(Math.toRadians((0 - pitch.toDouble()))) * 50
                val vz = cos(Math.toRadians(yaw.toDouble())) * ty * 50

                var x = it.player.location.x()
                var y = it.player.location.y() + 1.5
                var z = it.player.location.z()

                var d = 100

                //提前非阻塞计算阻挡物
                var tex = x
                var tey = y
                var tez = z

                for (i in 0..10000){
                    tex += vx * 0.02 / 100
                    tey += vy * 0.02 / 100
                    tez += vz * 0.02 / 100
                    if (world.getBlockAt(tex.toInt() - 1, tey.toInt(), tez.toInt()).type != Material.AIR) {
                        d = i / 100
                        break
                    }
                }

                launch{
                    for (i in 0..d) {
                        Thread.sleep(20)

                        x += vx * 0.02
                        y += vy * 0.02
                        z += vz * 0.02

                        for (n in 0..3) {
                            val px = x
                            val py = y
                            val pz = z
                            world.spawnParticle(
                                Particle.ENCHANTMENT_TABLE,
                                Location(
                                    world,
                                    px,
                                    py,
                                    pz
                                ),
                                1,
                                0.0,
                                0.0,
                                0.0,
                                0.0
                            )
                        }
                        launch(Dispatchers.region(Location(world, x, y, z))) {
                            val rotation = 1.0
                            val entities = Location(world, x, y, z).getNearbyEntities(rotation, rotation, rotation)
                            if (!entities.isEmpty() && i >= 2) {
                                for (entity in entities) {
                                    if (entity is Player) {
                                        entity.damage(100.0, it.player)
                                    } else if (entity is LivingEntity) {
                                        entity.damage(100.0, it.player)
                                    }
                                }
                            }
                        }

                    }

                    for (i in 0..10) {
                        world.spawnParticle(
                            Particle.VILLAGER_HAPPY,
                            Location(
                                world,
                                x,
                                y,
                                z
                            ),
                            1,
                            0.0,
                            0.0,
                            0.0,
                            0.0
                        )
                    }
                }

            }
        }
    }
}