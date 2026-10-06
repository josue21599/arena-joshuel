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
    val ARROWS = CardDef("Flechas", 3, 0f, 0f, 0f, 0f, 0f, spell = SpellDef(55f, 120f, 0.35f))
    val POISON = CardDef("Veneno", 4, 0f, 0f, 0f, 0f, 0f, spell = SpellDef(40f, 55f, 0.35f, 8f))

    val LANCER = CardDef("Lancero", 2, 260f, 40f, 55f, 14f, 0.9f, radius = 7f)
    val GUARDIAN = CardDef("Guardián", 4, 1100f, 60f, 35f, 4f, 1.2f, radius = 11f)
    val ALCHEMIST = CardDef("Alquimista", 3, 200f, 55f, 42f, 80f, 1.1f, ranged = true)
    val RUNNER = CardDef("Corredor", 4, 520f, 100f, 85f, 4f, 1.2f, TargetType.BUILDINGS, radius = 8f)

    val ALL = listOf(
        KNIGHT, ARCHER, GIANT, GOBLINS, MUSKETEER, PRINCE, SKELETONS, MINI,
        LANCER, GUARDIAN, ALCHEMIST, RUNNER, ARROWS, POISON,
    )

    /** Cartas desbloqueadas al inicio y mazo inicial (8). */
    val STARTER_UNLOCKED = setOf(KNIGHT, ARCHER, GIANT, GOBLINS, MUSKETEER, PRINCE, SKELETONS, LANCER, ARROWS, POISON)
        .map { it.name }.toSet()
    val STARTER_DECK = listOf(KNIGHT, ARCHER, GIANT, GOBLINS, MUSKETEER, PRINCE, ARROWS, POISON)

    fun byName(name: String) = ALL.firstOrNull { it.name == name }
}
