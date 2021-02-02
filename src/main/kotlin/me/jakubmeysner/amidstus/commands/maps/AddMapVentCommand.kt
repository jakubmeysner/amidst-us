package me.jakubmeysner.amidstus.commands.maps

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import me.jakubmeysner.amidstus.models.Vent
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.LivingEntity

class AddMapVentCommand(val plugin: AmidstUs) : TabExecutor, Named {
  override val name = "addmapvent"

  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (args.size != 1) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Usage: /addmapvent <map name>").color(ChatColor.RED).create()
      )
    } else if (sender !is LivingEntity) {
      sender.spigot().sendMessage(
        *ComponentBuilder("This command can only be used by living entities!").color(ChatColor.RED).create()
      )
    } else {
      val map = plugin.maps.find { it.name == args[0] }

      if (map == null) {
        sender.spigot().sendMessage(
          *ComponentBuilder("Could not find any map with this name!").color(ChatColor.RED).create()
        )
      } else {
        val targetExactBlock = sender.getTargetBlockExact(12)

        if (targetExactBlock == null) {
          sender.spigot().sendMessage(
            *ComponentBuilder("You are not looking at any block right now!").color(ChatColor.RED).create()
          )
        } else if (plugin.maps.any { it.vents.any { it.location == targetExactBlock.location } }) {
          sender.spigot().sendMessage(
            *ComponentBuilder("There is already a vent in designated location!").color(ChatColor.RED).create()
          )
        } else {
          val vent = Vent(targetExactBlock.location)
          map.vents.add(vent)

          sender.spigot().sendMessage(
            *ComponentBuilder("Succesfully added a new vent ${vent.id} to map ${map.name}.").color(ChatColor.GREEN).create()
          )
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
      else -> listOf()
    }
  }
}
