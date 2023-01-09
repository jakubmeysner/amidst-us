package me.jakubmeysner.amidstus.commands.maps

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class MapsCommand(val plugin: AmidstUs) : TabExecutor, Named {
    override val name = "maps"

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (plugin.maps.size == 0) {
            sender.spigot().sendMessage(
                *ComponentBuilder("Couldn't find any maps!").color(ChatColor.GRAY).create()
            )
        } else {
            sender.spigot().sendMessage(
                *ComponentBuilder("Maps:\n").color(ChatColor.BLUE).create(),
                *plugin.maps.flatMap {
                    ComponentBuilder("- ${it.displayName} (${it.name})${if (it != plugin.maps.last()) "\n" else ""}")
                        .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/map ${it.name}"))
                        .create().toList()
                }.toTypedArray()
            )
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return listOf()
    }
}
