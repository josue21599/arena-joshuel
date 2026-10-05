package com.josue.arenajoshuel.model

interface Targetable {
    val team: Team
    val x: Float
    val y: Float
    val radius: Float
    val isBuilding: Boolean
    var hp: Float
    var flash: Float
    val alive: Boolean get() = hp > 0f
}
