package me.jakubmeysner.amidstus.models

class Game(var map: Map, val type: Type) {
  enum class Type {
    PUBLIC, PRIVATE
  }

  enum class Status {
    PRE_GAME, GAME
  }

  var status = Status.PRE_GAME
  val players = mutableListOf<Player>()
}
