package me.jakubmeysner.amidstus

import me.jakubmeysner.amidstus.commands.maps.*
import me.jakubmeysner.amidstus.models.Game
import me.jakubmeysner.amidstus.models.Map
import org.bukkit.plugin.java.JavaPlugin

class AmidstUs : JavaPlugin() {
  val maps = mutableListOf<Map>()
  val games = mutableListOf<Game>()

  override fun onEnable() {
    val commands = mapOf(
      "createmap" to CommandCreateMap(this),
      "renamemap" to CommandRenameMap(this),
      "deletemap" to CommandDeleteMap(this),
      "setmapdisplayname" to CommandSetMapDisplayName(this),
      "maps" to CommandMaps(this),
      "map" to CommandMap(this),
    )

    for (command in commands) {
      this.getCommand(command.key)?.setExecutor(command.value)
    }
  }
}
