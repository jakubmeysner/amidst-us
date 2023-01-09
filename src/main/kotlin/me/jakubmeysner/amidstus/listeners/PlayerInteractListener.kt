package me.jakubmeysner.amidstus.listeners

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.models.EmergencyMeeting
import me.jakubmeysner.amidstus.models.Player
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import java.time.Duration
import java.time.Instant

class PlayerInteractListener(val plugin: AmidstUs) : Listener {
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val game = plugin.games.find { it.players.any { it.bukkit == event.player } } ?: return
        val player = game.players.find { it.bukkit == event.player }!!
        event.isCancelled = true

        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            when (event.clickedBlock?.location) {
                game.map.emergencyMeetingButton -> {
                    if (
                        game.emergencyMeetings.isEmpty() ||
                        game.emergencyMeetings.last().phase == EmergencyMeeting.Phase.ENDED
                    ) {
                        if (game.emergencyMeetings.count { it.calledBy == player } >= game.emergencyMeetingsLimit) {
                            player.bukkit.spigot().sendMessage(
                                *ComponentBuilder("You have already used all your emergency meetings!").color(ChatColor.RED)
                                    .create()
                            )
                        } else {
                            val dif =
                                game.emergencyMeetings.lastOrNull()?.let { Duration.between(it.endedAt, Instant.now()) }

                            if (dif == null || dif.seconds >= game.emergencyMeetingsCooldownSeconds) {
                                game.emergencyMeetings.add(
                                    EmergencyMeeting(
                                        plugin,
                                        game,
                                        EmergencyMeeting.Source.CALLED,
                                        player
                                    )
                                )
                            } else {
                                player.bukkit.spigot().sendMessage(
                                    *ComponentBuilder(
                                        "You must wait ${game.emergencyMeetingsCooldownSeconds - dif.seconds}s before " +
                                            "being able to call an emergency meeting!"
                                    ).color(ChatColor.RED).create()
                                )
                            }
                        }

                        return
                    }
                }
            }
        }

        when (event.item) {
            Player.LeaveGameItemStack -> {
                if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
                event.player.performCommand("leavegame")
            }

            Player.ChangeMapOptionsItemStack -> {
                if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
                event.player.performCommand("gameoptions")
            }

            EmergencyMeeting.VoteItemStack -> {
                if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
                event.player.performCommand("vote")
            }

            Player.StartGameItemStack -> {
                if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
                event.player.performCommand("startgame")
            }
        }
    }
}
