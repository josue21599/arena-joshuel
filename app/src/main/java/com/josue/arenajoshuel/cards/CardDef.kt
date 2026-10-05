package com.josue.arenajoshuel.cards

import com.josue.arenajoshuel.model.TargetType

class CardDef(
    val name: String,
    val cost: Int,
    val hp: Float,
    val damage: Float,
    val speed: Float,
    val range: Float,
    val attackInterval: Float,
    val targets: TargetType = TargetType.GROUND,
    val ranged: Boolean = false,
    val count: Int = 1,
    val radius: Float = 8f,
) {
    /** Puede defender: ataca unidades terrestres. */
    val canDefend: Boolean get() = targets == TargetType.GROUND
}
