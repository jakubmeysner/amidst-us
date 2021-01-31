package me.jakubmeysner.amidstus.models

class Game(var map: Map, val type: GameType) {
  var status: GameStatus = GameStatus.PRE_GAME
  val players = mutableListOf<Player>()
}

enum class GameType {
  PUBLIC, PRIVATE
}

enum class GameStatus {
  PRE_GAME, GAME
}
