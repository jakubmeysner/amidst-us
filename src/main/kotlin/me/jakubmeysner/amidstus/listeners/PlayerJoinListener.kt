package me.jakubmeysner.amidstus.listeners

import me.jakubmeysner.amidstus.AmidstUs
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(val plugin: AmidstUs) : Listener {
    companion object {
        val nameBlacklist = listOf("skip")
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (nameBlacklist.contains(event.player.name.toLowerCase())) {
            event.player.kickPlayer("${ChatColor.RED}Your name has been blacklisted!")
            return
        }

        val players = plugin.games.map { it.players }.flatten().filter { !it.pending }.map { it.bukkit }

        for (player in players) {
            player.hidePlayer(plugin, event.player)
            event.player.hidePlayer(plugin, player)
        }
    }
}
