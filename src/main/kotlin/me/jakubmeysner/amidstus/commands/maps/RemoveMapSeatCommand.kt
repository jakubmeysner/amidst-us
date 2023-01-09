package me.jakubmeysner.amidstus.commands.maps

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class RemoveMapSeatCommand(val plugin: AmidstUs) : TabExecutor, Named {
    override val name = "removemapseat"

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size != 2) {
            sender.spigot().sendMessage(
                *ComponentBuilder("Usage: /removemapseat <map name> <index>").color(ChatColor.RED).create()
            )
        } else {
            val map = plugin.maps.find { it.name == args[0] }

            if (map == null) {
                sender.spigot().sendMessage(
                    *ComponentBuilder("Could not find any map with this name!").color(ChatColor.RED).create()
                )
            } else {
                val index = args[1].toIntOrNull()

                if (index == null) {
                    sender.spigot().sendMessage(
                        *ComponentBuilder("Index must be a valid integer!").color(ChatColor.RED).create()
                    )
                } else {
                    if (map.seats.size < index + 1) {
                        sender.spigot().sendMessage(
                            *ComponentBuilder("Could not find any seat with this index!").color(ChatColor.RED).create()
                        )
                    } else {
                        map.seats.removeAt(index)
                        sender.spigot().sendMessage(
                            *ComponentBuilder("Remove seat $index from map ${map.displayName}.").color(ChatColor.GREEN)
                                .create()
                        )
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
            1 -> plugin.maps.map { it.name }.filter { it.startsWith(args[0]) }
            2 -> plugin.maps.find { it.name == args[0] }?.seats?.indices?.map { it.toString() }
                ?.filter { it.startsWith(args[1]) } ?: listOf()

            else -> listOf()
        }
    }
}
