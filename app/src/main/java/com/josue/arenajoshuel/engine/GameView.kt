package com.josue.arenajoshuel.engine

import android.content.Context
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.josue.arenajoshuel.ai.Difficulty
import com.josue.arenajoshuel.model.GameState
import com.josue.arenajoshuel.model.Lane
import com.josue.arenajoshuel.model.Result
import com.josue.arenajoshuel.render.Renderer

class GameView(
    context: Context,
    difficulty: Difficulty,
    private val onResult: (Result) -> Unit,
) : SurfaceView(context), SurfaceHolder.Callback {
    private val state = GameState(difficulty)
    private var reported = false
    private val renderer = Renderer()
    private var loop: GameLoop? = null

    init { holder.addCallback(this) }

    override fun surfaceCreated(holder: SurfaceHolder) {
        val l = GameLoop(::update, ::drawFrame)
        l.running = true
        loop = l
        l.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        val l = loop ?: return
        l.running = false
        l.join()
        loop = null
    }

    fun pause() { state.paused = true }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked != MotionEvent.ACTION_DOWN) return true
        if (state.paused) { state.paused = false; return true }
        val scale = minOf(width / GameState.WORLD_W, height / GameState.TOTAL_H)
        val wx = (event.x - (width - GameState.WORLD_W * scale) / 2f) / scale
        val wy = (event.y - (height - GameState.TOTAL_H * scale) / 2f) / scale
        if (wy >= GameState.WORLD_H) {
            val i = Renderer.cardAt(wx, wy)
            if (i >= 0) state.selected = if (state.selected == i) -1 else i
        } else if (state.selected >= 0 && wx in 0f..GameState.WORLD_W &&
            wy > GameState.RIVER_Y + GameState.RIVER_HALF
        ) {
            val lane = if (wx < GameState.WORLD_W / 2f) Lane.LEFT else Lane.RIGHT
            state.requestDeploy(state.selected, lane, wy.coerceIn(GameState.RIVER_Y + 40f, 540f))
        }
        return true
    }

    private fun update() {
        state.update()
        val r = state.result
        if (r != null && !reported) {
            reported = true
            post { onResult(r) }
        }
    }

    private fun drawFrame() {
        val canvas = holder.lockCanvas() ?: return
        try {
            renderer.draw(canvas, state)
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }
}
