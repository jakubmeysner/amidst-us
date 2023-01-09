package me.jakubmeysner.amidstus.commands.games

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import me.jakubmeysner.amidstus.models.Player
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player as BukkitPlayer

class PlayCommand(val plugin: AmidstUs) : TabExecutor, Named {
    override val name = "play"

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (plugin.maps.none { it.playable }) {
            sender.spigot().sendMessage(
                *ComponentBuilder("No maps exist or are playable!").color(ChatColor.RED).create()
            )
        } else if (sender !is BukkitPlayer) {
            sender.spigot().sendMessage(
                *ComponentBuilder("This command can only be used by players!").color(ChatColor.RED).create()
            )
        } else if (plugin.games.any { it.players.any { it.bukkit == sender } }) {
            sender.spigot().sendMessage(
                *ComponentBuilder("You are already in game!").color(ChatColor.RED).create()
            )
        } else if (args.size > 1) {
            sender.spigot().sendMessage(
                *ComponentBuilder("Usage: /play [map name]").color(ChatColor.RED).create()
            )
        } else {
            val map = plugin.maps.find { it.playable && it.name == args.getOrNull(0) }

            if (args.size == 1 && map == null) {
                sender.spigot().sendMessage(
                    *ComponentBuilder("Could not find any map with this name or it is not playable!")
                        .color(ChatColor.RED).create()
                )
            } else {
                Player.playPublicGame(plugin, map, sender)
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
            1 -> plugin.maps.map { it.name }.filter { it.startsWith(args[0]) }
            else -> listOf()
        }
    }
}
