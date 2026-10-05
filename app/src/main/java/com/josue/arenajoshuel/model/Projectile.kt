package com.josue.arenajoshuel.model

class Projectile(
    val team: Team,
    var x: Float,
    var y: Float,
    val target: Targetable,
    val damage: Float,
    val speed: Float = 220f,
) {
    var dead = false
}
