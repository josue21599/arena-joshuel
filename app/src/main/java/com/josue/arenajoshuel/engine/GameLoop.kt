package com.josue.arenajoshuel.engine

class GameLoop(
    private val update: () -> Unit,
    private val draw: () -> Unit,
) : Thread("GameLoop") {
    @Volatile var running = false

    override fun run() {
        val step = 1_000_000_000L / UPS
        var last = System.nanoTime()
        var acc = 0L
        while (running) {
            val now = System.nanoTime()
            acc += now - last
            last = now
            if (acc > step * 5) acc = step * 5
            while (acc >= step) {
                update()
                acc -= step
            }
            draw()
            val rest = (step - acc - (System.nanoTime() - now)) / 1_000_000L
            if (rest > 0) sleep(rest)
        }
    }

    companion object { const val UPS = 60 }
}
