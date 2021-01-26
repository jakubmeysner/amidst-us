package me.jakubmeysner.amidstus

import me.jakubmeysner.amidstus.commands.CommandCreateMap
import me.jakubmeysner.amidstus.commands.CommandDeleteMap
import me.jakubmeysner.amidstus.commands.CommandRenameMap
import me.jakubmeysner.amidstus.models.Game
import me.jakubmeysner.amidstus.models.Map
import org.bukkit.plugin.java.JavaPlugin

class AmidstUs : JavaPlugin() {
  val maps = mutableListOf<Map>()
  val games = mutableListOf<Game>()

  override fun onEnable() {
    this.getCommand("createmap")?.setExecutor(CommandCreateMap(this))
    this.getCommand("renamemap")?.setExecutor(CommandRenameMap(this))
    this.getCommand("deletemap")?.setExecutor(CommandDeleteMap(this))
  }
}
