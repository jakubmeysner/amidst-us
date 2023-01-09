package me.jakubmeysner.amidstus.commands.games

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.inventory.ItemStack
import org.bukkit.ChatColor as BukkitChatColor

class GameOptionsCommand(val plugin: AmidstUs) : TabExecutor, Named {
    companion object {
        val MapItemStack = ItemStack(Material.FILLED_MAP)
        val MaxNumberOfImpostorsItemStack = ItemStack(Material.PLAYER_HEAD)
        val KillCooldownSecondsItemStack = ItemStack(Material.DIAMOND_SWORD)
        val PreviousMapItemStack = ItemStack(Material.RED_WOOL).apply {
            itemMeta = itemMeta?.apply { setDisplayName("${BukkitChatColor.RED}Previous map") }
        }
        val NextMapItemStack = ItemStack(Material.GREEN_WOOL).apply {
            itemMeta = itemMeta?.apply { setDisplayName("${BukkitChatColor.GREEN}Next map") }
        }
        val DecreaseMaxNumberOfImpostorsItemStack = ItemStack(Material.RED_WOOL).apply {
            itemMeta = itemMeta?.apply { setDisplayName("${BukkitChatColor.RED}Decrease max number of impostors") }
        }
        val IncreaseMaxNumberOfImporsItemStack = ItemStack(Material.GREEN_WOOL).apply {
            itemMeta = itemMeta?.apply { setDisplayName("${BukkitChatColor.GREEN}Increase max number of impostors") }
        }
        val DecreaseKillCooldownSecondsItemStack = ItemStack(Material.RED_WOOL).apply {
            itemMeta = itemMeta?.apply { setDisplayName("${BukkitChatColor.RED}Decrease kill cooldown") }
        }
        val IncreaseKillCooldownSecondsItemStack = ItemStack(Material.GREEN_WOOL).apply {
            itemMeta = itemMeta?.apply { setDisplayName("${BukkitChatColor.GREEN}Increase kill cooldown") }
        }
    }

    override val name = "gameoptions"

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val game = plugin.games.find { it.players.any { it.bukkit == sender } }

        if (game == null) {
            sender.spigot().sendMessage(
                *ComponentBuilder("You are not in game at the moment!").color(ChatColor.RED).create()
            )
        } else {
            val player = game.players.find { it.bukkit == sender }!!

            if (args.isEmpty()) {
                player.bukkit.openInventory(
                    Bukkit.createInventory(null, 27, "${BukkitChatColor.BLUE}Change map options").apply {
                        setItem(9, MapItemStack.clone().apply {
                            itemMeta = itemMeta?.clone()?.apply {
                                setDisplayName("${BukkitChatColor.BLUE}Map:${BukkitChatColor.RESET} ${game.map.displayName}")
                            }
                        })

                        if (plugin.maps.size > 1) {
                            setItem(0, PreviousMapItemStack)
                            setItem(18, NextMapItemStack)
                        }

                        setItem(10, MaxNumberOfImpostorsItemStack.clone().apply {
                            itemMeta = itemMeta?.clone()?.apply {
                                setDisplayName("${BukkitChatColor.BLUE}Max number of impostors:${BukkitChatColor.RESET} ${game.maxNumberOfImpostors}")
                            }
                        })

                        if (game.maxNumberOfImpostors > 1) setItem(1, DecreaseMaxNumberOfImpostorsItemStack)
                        if (game.maxNumberOfImpostors < game.map.maxNumberOfImpostors) setItem(
                            19,
                            IncreaseMaxNumberOfImporsItemStack
                        )

                        setItem(11, KillCooldownSecondsItemStack.clone().apply {
                            itemMeta = itemMeta?.clone()?.apply {
                                setDisplayName("${BukkitChatColor.BLUE}Kill cooldown:${BukkitChatColor.RESET} ${game.killCooldownSeconds}s")
                            }
                        })

                        if (game.killCooldownSeconds > 10) setItem(2, DecreaseKillCooldownSecondsItemStack)
                        if (game.killCooldownSeconds < 60) setItem(20, IncreaseKillCooldownSecondsItemStack)
                    })
            } else if (args.size == 1) {
                when (args[0]) {
                    "map" -> {
                        sender.spigot().sendMessage(
                            *ComponentBuilder("Map is ${game.map.displayName}.").create()
                        )
                    }

                    "maxnoimpostors" -> {
                        sender.spigot().sendMessage(
                            *ComponentBuilder("Max number of impostors is ${game.maxNumberOfImpostors}.").create()
                        )
                    }

                    "killcooldownseconds" -> {
                        sender.spigot().sendMessage(
                            *ComponentBuilder("Kill cooldown seconds is ${game.killCooldownSeconds}.").create()
                        )
                    }
                }
            } else {
                if (!player.promoted) {
                    sender.spigot().sendMessage(
                        *ComponentBuilder("To be able to modify game options you need to be promoted!")
                            .color(ChatColor.RED).create()
                    )
                } else {
                    when (args[0]) {
                        "map" -> {
                            val map = plugin.maps.find { it.name == args[1] }

                            if (map == null || !map.playable) {
                                sender.spigot().sendMessage(
                                    *ComponentBuilder("Could not find any map with this name or it is not playable!")
                                        .color(ChatColor.RED).create()
                                )
                            } else if (map == game.map) {
                                sender.spigot().sendMessage(
                                    *ComponentBuilder("You are already playing on this map!")
                                        .color(ChatColor.RED).create()
                                )
                            } else {
                                game.map = map
                                game.maxNumberOfImpostors =
                                    minOf(game.maxNumberOfImpostors, game.map.maxNumberOfImpostors)

                                for (itPlayer in game.players) {
                                    itPlayer.bukkit.teleport(game.map.preGameLocation!!)

                                    if (map.time != null) {
                                        itPlayer.bukkit.setPlayerTime(map.time!!.toLong(), false)
                                    } else {
                                        itPlayer.bukkit.resetPlayerTime()
                                    }
                                }

                                sender.spigot().sendMessage(
                                    *ComponentBuilder("Changed map to ${map.displayName}.").color(ChatColor.GREEN)
                                        .create()
                                )
                            }
                        }

                        "maxnoimpostors" -> {
                            val newValue = args[1].toIntOrNull()

                            if (args.size != 2 || newValue == null || newValue !in 1..game.map.maxNumberOfImpostors) {
                                sender.spigot().sendMessage(
                                    *ComponentBuilder(
                                        "New value must be a valid integer between 1 and " +
                                            "${game.map.maxNumberOfImpostors}!"
                                    ).color(ChatColor.RED).create()
                                )
                            } else {
                                game.maxNumberOfImpostors = newValue
                                sender.spigot().sendMessage(
                                    *ComponentBuilder("Max number of impostors has been set to ${game.maxNumberOfImpostors}.")
                                        .color(ChatColor.GREEN).create()
                                )
                            }
                        }

                        "killcooldownseconds" -> {
                            val newValue = args[1].toIntOrNull()

                            if (args.size != 2 || newValue == null || newValue !in 10..60) {
                                sender.spigot().sendMessage(
                                    *ComponentBuilder("New value must be a valid integer between 1 and 60!")
                                        .color(ChatColor.RED).create()
                                )
                            } else {
                                game.killCooldownSeconds = newValue
                                sender.spigot().sendMessage(
                                    *ComponentBuilder("Kill cooldown seconds has been set to ${game.killCooldownSeconds}.")
                                        .color(ChatColor.GREEN).create()
                                )
                            }
                        }
                    }
                }
            }
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> listOf(
                "map",
                "maxnoimpostors",
                "killcooldownseconds",
            )

            2 -> when (args[0]) {
                "map" -> plugin.maps.filter { it.playable }.map { it.name }.filter { it.startsWith(args[1]) }
                "maxnoimpostors" -> plugin.games.find { it.players.any { it.bukkit == sender } }?.map?.let {
                    (1..it.maxNumberOfImpostors).map { it.toString() }.filter { it.startsWith(args[1]) }
                } ?: (1..6).map { it.toString() }.filter { it.startsWith(args[1]) }

                "killcooldownseconds" -> (10..60).map { it.toString() }.filter { it.startsWith(args[1]) }
                else -> listOf()
            }

            else -> listOf()
        }
    }
}
