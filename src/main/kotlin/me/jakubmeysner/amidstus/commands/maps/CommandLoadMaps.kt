package me.jakubmeysner.amidstus.commands.maps

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class CommandLoadMaps(val plugin: AmidstUs) : TabExecutor, Named {
  override val name = "loadmaps"

  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (args.isNotEmpty()) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Usage: /loadmaps").color(ChatColor.RED).create()
      )
    } else {
      plugin.loadMaps()
      sender.spigot().sendMessage(
        *ComponentBuilder("Loaded maps from file.").color(ChatColor.GREEN).create()
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
