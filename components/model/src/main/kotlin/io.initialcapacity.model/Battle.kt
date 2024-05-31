package io.initialcapacity.model

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ResultRow

data class Battle(
        val id: Long? = null,
        val winnerId: Long? = null
)

object BattleTable : LongIdTable("battles") {
    val winnerId = long("winner_id")
}

fun ResultRow.toBattle() = Battle(
        id = this[BattleTable.id].value,
        winnerId = this[BattleTable.winnerId]
)