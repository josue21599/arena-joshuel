package com.josue.arenajoshuel.cards

import android.content.Context

/** Persistencia local (SharedPreferences): mazo del jugador y cartas desbloqueadas. */
object DeckStore {
    const val DECK_SIZE = 8
    private const val PREFS = "arena_deck"
    private const val KEY_DECK = "deck"
    private const val KEY_UNLOCKED = "unlocked"
    private const val KEY_COPIES = "copies"

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun unlocked(c: Context): Set<String> =
        prefs(c).getString(KEY_UNLOCKED, null)?.split('|')?.toSet() ?: CardDefs.STARTER_UNLOCKED

    /** Mazo guardado (puede estar incompleto mientras se edita). */
    fun savedDeck(c: Context): List<CardDef> {
        val s = prefs(c).getString(KEY_DECK, null) ?: return CardDefs.STARTER_DECK
        val un = unlocked(c)
        return s.split('|').mapNotNull { CardDefs.byName(it) }.filter { it.name in un }.distinct().take(DECK_SIZE)
    }

    /** Mazo listo para jugar, o null si no tiene 8 cartas. */
    fun playableDeck(c: Context): List<CardDef>? = savedDeck(c).takeIf { it.size == DECK_SIZE }

    fun saveDeck(c: Context, deck: List<CardDef>) {
        prefs(c).edit().putString(KEY_DECK, deck.joinToString("|") { it.name }).apply()
    }

    /** Copias por carta: las desbloqueadas valen al menos 1. */
    fun copies(c: Context): Map<String, Int> {
        val saved = prefs(c).getString(KEY_COPIES, null)?.split('|')?.mapNotNull {
            val p = it.split(':')
            if (p.size == 2) p[0] to (p[1].toIntOrNull() ?: 0) else null
        }?.toMap() ?: emptyMap()
        return unlocked(c).associateWith { maxOf(saved[it] ?: 0, 1) }
    }

    /** Abre un cofre: 3 cartas (las bloqueadas pesan más). Desbloquea o suma copias y guarda. */
    fun openChest(c: Context): List<ChestReward> {
        val un = unlocked(c).toMutableSet()
        val copies = copies(c).toMutableMap()
        val rewards = List(3) {
            val bag = CardDefs.ALL.flatMap { card -> List(if (card.name in un) 1 else 3) { card } }
            val card = bag.random()
            val isNew = un.add(card.name)
            copies[card.name] = if (isNew) 1 else (copies[card.name] ?: 1) + 1
            ChestReward(card, isNew, copies[card.name]!!)
        }
        prefs(c).edit()
            .putString(KEY_UNLOCKED, un.joinToString("|"))
            .putString(KEY_COPIES, copies.entries.joinToString("|") { "${it.key}:${it.value}" })
            .apply()
        return rewards
    }
}

class ChestReward(val card: CardDef, val isNew: Boolean, val copies: Int)
