package com.josue.arenajoshuel.cards

/** Mazo de 8 cartas: mano de 4 y cola de 4 que se repone en ciclo. */
class Deck(cards: List<CardDef>) {
    val hand = ArrayList<CardDef>(cards.take(4))
    private val queue = ArrayDeque(cards.drop(4))

    /** Juega la carta de la mano en [index]; entra la siguiente de la cola. */
    fun play(index: Int): CardDef {
        val card = hand[index]
        hand[index] = queue.removeFirst()
        queue.addLast(card)
        return card
    }
}
