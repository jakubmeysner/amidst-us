@file:UseSerializers(LocationSerializer::class)
package me.jakubmeysner.amidstus.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.ListSerializer
import me.jakubmeysner.amidstus.serializers.LocationSerializer
import org.bukkit.Location

@Serializable
class Map(var name: String) {
  var displayName: String = name
  var postGameLocation: Location? = null
}
