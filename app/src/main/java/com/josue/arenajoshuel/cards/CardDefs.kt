package com.josue.arenajoshuel.cards

import com.josue.arenajoshuel.model.TargetType

object CardDefs {
    val KNIGHT = CardDef("Caballero", 3, 650f, 80f, 45f, 4f, 1.1f, radius = 9f)
    val ARCHER = CardDef("Arquera", 3, 240f, 40f, 45f, 70f, 0.9f, ranged = true)
    val GIANT = CardDef("Gigante", 5, 1800f, 110f, 30f, 4f, 1.5f, TargetType.BUILDINGS, radius = 12f)
    val GOBLINS = CardDef("Duendes", 2, 110f, 28f, 65f, 4f, 0.8f, count = 3, radius = 6f)
    val MUSKETEER = CardDef("Mosquetera", 4, 340f, 80f, 40f, 90f, 1.0f, ranged = true)
    val PRINCE = CardDef("Príncipe", 5, 850f, 130f, 50f, 4f, 1.4f, radius = 10f)
    val SKELETONS = CardDef("Esqueletos", 1, 60f, 25f, 60f, 4f, 0.9f, count = 3, radius = 5f)
    val MINI = CardDef("Mini", 3, 380f, 90f, 55f, 4f, 1.0f)

    val ALL = listOf(KNIGHT, ARCHER, GIANT, GOBLINS, MUSKETEER, PRINCE, SKELETONS, MINI)
}
