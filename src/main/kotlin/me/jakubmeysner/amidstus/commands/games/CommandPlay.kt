package me.jakubmeysner.amidstus.commands.games

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import me.jakubmeysner.amidstus.models.*
import me.jakubmeysner.amidstus.models.Map
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class CommandPlay(val plugin: AmidstUs) : TabExecutor, Named {
  override val name = "play"

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
    } else if (args.size > 1) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Usage: /play [map name]").color(ChatColor.RED).create()
      )
    } else {
      if (args.size == 1 && plugin.maps.none { it.playable && it.name == args[0] }) {
        sender.spigot().sendMessage(
          *ComponentBuilder("Could not find any map with this name or it is not playable!")
            .color(ChatColor.RED).create()
        )
      } else {
        val games = plugin.games.filter {
          (args.isEmpty() || it.map.name == args[0]) &&
            it.players.size < it.map.maxNumberOfPlayers &&
            it.status == GameStatus.PRE_GAME
        }.sortedByDescending { it.players.size }

        val game = if (games.isEmpty()) {
          val game = Game(
            if (args.isNotEmpty())
              plugin.maps.find { it.name == args[0] } as Map
            else plugin.maps.filter { it.playable }.shuffled()[0],
            GameType.PUBLIC
          )
          plugin.games.add(game)
          game
        } else {
          games[0]
        }

        val player = Player(sender)
        game.players.add(player)
        player.joinGame(game, plugin)

        sender.spigot().sendMessage(
          *ComponentBuilder("You are now playing on ${game.map.displayName}!")
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
