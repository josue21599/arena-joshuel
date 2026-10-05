package com.josue.arenajoshuel.cards

class ElixirBar(var value: Float = 5f) {
    companion object {
        const val MAX = 10f
        const val SECONDS_PER_ELIXIR = 2.8f
    }

    fun update(dt: Float, doubleRate: Boolean) {
        value = minOf(MAX, value + dt / SECONDS_PER_ELIXIR * (if (doubleRate) 2f else 1f))
    }

    fun canSpend(cost: Int) = value >= cost

    fun spend(cost: Int) { value -= cost }
}
