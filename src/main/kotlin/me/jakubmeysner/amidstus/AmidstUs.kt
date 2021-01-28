package me.jakubmeysner.amidstus

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.jakubmeysner.amidstus.commands.maps.*
import me.jakubmeysner.amidstus.models.Game
import me.jakubmeysner.amidstus.models.Map
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class AmidstUs : JavaPlugin() {
  val maps = mutableListOf<Map>()
  val games = mutableListOf<Game>()

  private val mapsFile = File(dataFolder, "maps.json")

  override fun onEnable() {
    val commands = mapOf(
      "createmap" to CommandCreateMap(this),
      "renamemap" to CommandRenameMap(this),
      "deletemap" to CommandDeleteMap(this),
      "setmapdisplayname" to CommandSetMapDisplayName(this),
      "maps" to CommandMaps(this),
      "map" to CommandMap(this),
      "setmappostgamelocation" to CommandSetMapPostGameLocation(this),
      "loadmaps" to CommandLoadMaps(this),
      "savemaps" to CommandSaveMaps(this),
      "setmappregamelocation" to CommandSetMapPreGameLocation(this),
    )

    for (command in commands) {
      this.getCommand(command.key)?.setExecutor(command.value)
    }

    if (!dataFolder.exists()) {
      Files.createDirectory(Paths.get(dataFolder.path))
    }

    if (!mapsFile.exists()) {
      mapsFile.createNewFile()
      mapsFile.writeText("[]")
    }

    loadMaps()
  }

  override fun onDisable() {
    saveMaps()
  }

  fun loadMaps() {
    maps.clear()
    maps.addAll(Json.decodeFromString<List<Map>>(mapsFile.readText()))
  }

  fun saveMaps() {
    mapsFile.writeText(Json.encodeToString(maps))
  }
}
