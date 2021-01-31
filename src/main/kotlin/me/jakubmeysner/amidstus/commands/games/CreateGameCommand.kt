package me.jakubmeysner.amidstus.commands.games

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import me.jakubmeysner.amidstus.models.Game
import me.jakubmeysner.amidstus.models.GameType
import me.jakubmeysner.amidstus.models.Player
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class CreateGameCommand(val plugin: AmidstUs) : TabExecutor, Named {
  override val name = "creategame"

  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (plugin.maps.none { it.playable }) {
      sender.spigot().sendMessage(
        *ComponentBuilder("No maps exist or are playable!").color(ChatColor.RED).create()
      )
    } else if (sender !is org.bukkit.entity.Player) {
      sender.spigot().sendMessage(
        *ComponentBuilder("This command can only be used by players!").color(ChatColor.RED).create()
      )
    } else if (plugin.games.any { it.players.any { it.bukkitPlayer == sender } }) {
      sender.spigot().sendMessage(
        *ComponentBuilder("You are already in game!").color(ChatColor.RED).create()
      )
    } else if (args.size != 1) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Usage: /creategame <map name>").color(ChatColor.RED).create()
      )
    } else {
      val map = plugin.maps.find { it.name == args[0] && it.playable }

      if (map == null) {
        sender.spigot().sendMessage(
          *ComponentBuilder("Could not find any map with this name or it is not playable!")
            .color(ChatColor.RED).create()
        )
      } else {
        val game = Game(map, GameType.PRIVATE)
        plugin.games.add(game)
        val player = Player(sender)
        player.joinGame(game, plugin)

        sender.spigot().sendMessage(
          *ComponentBuilder("Created a new game on ${game.map.displayName}!")
            .color(ChatColor.GREEN).create()
        )
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
