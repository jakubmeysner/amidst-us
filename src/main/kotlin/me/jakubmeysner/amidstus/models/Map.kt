@file:UseSerializers(LocationSerializer::class)

package me.jakubmeysner.amidstus.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import me.jakubmeysner.amidstus.serializers.LocationSerializer
import org.bukkit.Location

@Serializable
class Map(var name: String) {
  var displayName: String = name
  var postGameLocation: Location? = null
  var preGameLocation: Location? = null
  var minNumberOfPlayers = 4
  var maxNumberOfPlayers = 10
  var maxNumberOfImpostors = 2
  val seats = mutableListOf<Location>()

  val playable: Boolean
    get() = listOf(
      postGameLocation != null,
      preGameLocation != null,
      minNumberOfPlayers in 4..25,
      maxNumberOfPlayers in 4..25,
      maxNumberOfImpostors in 1..6,
      seats.size >= maxNumberOfPlayers,
    ).all { it }
}
