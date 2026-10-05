package com.josue.arenajoshuel.ai

import com.josue.arenajoshuel.cards.CardDefs
import com.josue.arenajoshuel.cards.Deck
import com.josue.arenajoshuel.cards.ElixirBar
import com.josue.arenajoshuel.model.GameState
import com.josue.arenajoshuel.model.Lane
import com.josue.arenajoshuel.model.Team
import com.josue.arenajoshuel.model.Unit
import kotlin.math.abs
import kotlin.random.Random

enum class Difficulty(val minDelay: Float, val maxDelay: Float, val attackElixir: Int) {
    EASY(1.0f, 2.5f, 9),
    MEDIUM(0.5f, 1.5f, 8),
    HARD(0.3f, 0.9f, 6),
}

class CpuPlayer(private val state: GameState, private val difficulty: Difficulty = Difficulty.MEDIUM) {
    val elixir = ElixirBar()
    val deck = Deck(CardDefs.ALL.shuffled())

    private var delay = -1f // <0: sin reacción pendiente
    private val answered = HashSet<Unit>()

    fun update() {
        elixir.update(GameState.DT, state.lastMinute)
        answered.retainAll { it.alive }

        if (delay < 0f) {
            if (threat() != null || elixir.value >= difficulty.attackElixir) {
                delay = Random.nextFloat() * (difficulty.maxDelay - difficulty.minDelay) + difficulty.minDelay
            }
            return
        }
        delay -= GameState.DT
        if (delay > 0f) return
        delay = -1f
        act()
    }

    /** Unidad enemiga que ya cruzó a la mitad de la CPU y aún no fue respondida. */
    private fun threat(): Unit? =
        state.units.firstOrNull {
            it.team == Team.PLAYER && it.alive && it.y < GameState.RIVER_Y - GameState.RIVER_HALF && it !in answered
        }

    private fun act() {
        val t = threat()
        if (t != null) {
            // Defensa: carta útil más barata que se pueda pagar
            val idx = deck.hand.indices
                .filter { deck.hand[it].canDefend && elixir.canSpend(deck.hand[it].cost) }
                .minByOrNull { deck.hand[it].cost }
            if (idx != null) {
                answered += t
                val lane = if (abs(t.x - GameState.LANE_LEFT_X) <= abs(t.x - GameState.LANE_RIGHT_X)) Lane.LEFT else Lane.RIGHT
                play(idx, lane, 190f)
                return
            }
        }
        if (elixir.value >= difficulty.attackElixir) {
            // Ataque: carril de la torre del jugador más dañada; carta más cara posible
            val idx = deck.hand.indices
                .filter { elixir.canSpend(deck.hand[it].cost) }
                .maxByOrNull { deck.hand[it].cost } ?: return
            play(idx, weakestLane(), 230f)
        }
    }

    private fun weakestLane(): Lane {
        val sides = state.towers.filter { it.team == Team.PLAYER && !it.isKing && it.alive }
        val weakest = sides.minByOrNull { it.hp / it.maxHp } ?: return Lane.entries.random()
        return weakest.lane ?: Lane.entries.random()
    }

    private fun play(index: Int, lane: Lane, y: Float) {
        val card = deck.play(index)
        elixir.spend(card.cost)
        state.spawn(card, Team.CPU, lane, y)
    }
}
