package io.initialcapacity.model

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ResultRow

data class Round(
        val id: Long? = null,
        val battleId: Long,
        val movie1Id: Long? = null,
        val movie2Id: Long? = null,
        val winnerId: Long? = null
)

object RoundTable : LongIdTable("rounds") {
    val battleId = long("battle_id")
    val movie1Id = long("movie1_id")
    val movie2Id = long("movie2_id")
    val winnerId = long("winner_id")
}

fun ResultRow.toRound() = Round(
        id = this[RoundTable.id].value,
        battleId = this[RoundTable.battleId],
        movie1Id = this[RoundTable.movie1Id],
        movie2Id = this[RoundTable.movie2Id],
        winnerId = this[RoundTable.winnerId]
)