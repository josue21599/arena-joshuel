package com.josue.arenajoshuel.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import com.josue.arenajoshuel.cards.ChestReward
import kotlin.math.sin

/** Cofre animado: tiembla, se abre al tocar y revela las cartas una a una. */
class ChestView(
    context: Context,
    private val onOpen: () -> List<ChestReward>,
    private val onDone: () -> Unit,
) : View(context) {
    private companion object {
        const val OPEN_TIME = 0.6f
        const val REVEAL_STEP = 0.9f
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private var phase = 0 // 0 tiembla, 1 abriendo, 2 revelando
    private var t = 0f
    private var last = 0L
    private var rewards: List<ChestReward> = emptyList()

    private val allRevealed: Boolean get() = phase == 2 && t > rewards.size * REVEAL_STEP + 0.4f

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.actionMasked != MotionEvent.ACTION_DOWN) return true
        when {
            phase == 0 -> { rewards = onOpen(); phase = 1; t = 0f }
            allRevealed -> onDone()
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        val now = System.nanoTime()
        val dt = if (last == 0L) 0f else ((now - last) / 1e9f).coerceAtMost(0.05f)
        last = now
        t += dt
        if (phase == 1 && t >= OPEN_TIME) { phase = 2; t = 0f }

        canvas.drawColor(Color.rgb(30, 30, 40))
        val u = width / 360f
        val cx = width / 2f
        val cy = height * 0.6f
        paint.textAlign = Paint.Align.CENTER

        // Resplandor al abrir
        if (phase >= 1) {
            val k = if (phase == 1) t / OPEN_TIME else 1f
            paint.style = Paint.Style.FILL
            paint.color = Color.argb((110 * k).toInt(), 255, 220, 90)
            canvas.drawCircle(cx, cy, 120f * u * k, paint)
        }

        // Tarjetas reveladas
        for ((i, r) in rewards.withIndex()) {
            if (phase != 2) break
            val p = ((t - i * REVEAL_STEP) / 0.5f).coerceIn(0f, 1f)
            if (p <= 0f) continue
            drawCard(canvas, r, cx + (i - 1) * 108f * u, cy - (30f + 150f * p) * u, u, p)
        }

        // Cofre (tiembla en fase 0)
        canvas.save()
        if (phase == 0) canvas.rotate(sin(t * 28f) * 3f, cx, cy + 35f * u)
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(140, 90, 40)
        rect.set(cx - 70f * u, cy, cx + 70f * u, cy + 70f * u)
        canvas.drawRoundRect(rect, 6f * u, 6f * u, paint)
        paint.color = Color.rgb(230, 190, 60)
        canvas.drawRect(cx - 8f * u, cy, cx + 8f * u, cy + 70f * u, paint)
        canvas.save()
        val ang = when (phase) {
            0 -> 0f
            1 -> -75f * t / OPEN_TIME
            else -> -75f
        }
        canvas.rotate(ang, cx - 70f * u, cy)
        paint.color = Color.rgb(165, 108, 50)
        rect.set(cx - 70f * u, cy - 28f * u, cx + 70f * u, cy)
        canvas.drawRoundRect(rect, 8f * u, 8f * u, paint)
        paint.color = Color.rgb(230, 190, 60)
        canvas.drawRect(cx - 8f * u, cy - 28f * u, cx + 8f * u, cy, paint)
        canvas.restore()
        canvas.restore()

        paint.color = Color.WHITE
        paint.textSize = 18f * u
        val hint = when {
            phase == 0 -> "Toca para abrir"
            allRevealed -> "Toca para continuar"
            else -> ""
        }
        canvas.drawText(hint, cx, cy + 120f * u, paint)
        postInvalidateOnAnimation()
    }

    private fun drawCard(canvas: Canvas, r: ChestReward, x: Float, y: Float, u: Float, p: Float) {
        val hw = 48f * u * p
        val hh = 64f * u * p
        paint.style = Paint.Style.FILL
        paint.color = if (r.isNew) Color.rgb(200, 150, 30) else Color.rgb(70, 90, 140)
        rect.set(x - hw, y - hh, x + hw, y + hh)
        canvas.drawRoundRect(rect, 8f * u, 8f * u, paint)
        paint.color = Color.rgb(200, 60, 230)
        canvas.drawCircle(x - hw + 12f * u * p, y - hh + 12f * u * p, 11f * u * p, paint)
        paint.color = Color.WHITE
        paint.textSize = 13f * u * p
        canvas.drawText(r.card.cost.toString(), x - hw + 12f * u * p, y - hh + 17f * u * p, paint)
        paint.textSize = 14f * u * p
        canvas.drawText(r.card.name, x, y + 4f * u * p, paint)
        paint.textSize = 12f * u * p
        canvas.drawText(if (r.isNew) "¡NUEVA!" else "+1 (x${r.copies})", x, y + 44f * u * p, paint)
    }
}
