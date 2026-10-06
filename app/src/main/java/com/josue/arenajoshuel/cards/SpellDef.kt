package com.josue.arenajoshuel.cards

/**
 * Hechizo de área. duration = 0: daño instantáneo (damage total);
 * duration > 0: zona persistente, damage = daño por segundo.
 */
class SpellDef(
    val radius: Float,
    val damage: Float,
    val towerFactor: Float,
    val duration: Float = 0f,
)
