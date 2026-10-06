package com.josue.arenajoshuel.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.josue.arenajoshuel.cards.ElixirBar
import com.josue.arenajoshuel.model.GameState
import com.josue.arenajoshuel.model.Team
import com.josue.arenajoshuel.model.Unit as GameUnit

class Renderer {
    companion object {
        private const val CARD_W = 78f
        private const val CARD_H = 66f
        private const val CARD_GAP = 6f
        private const val CARD_X0 = 15f
        private const val CARD_Y = GameState.WORLD_H + 36f

        /** Índice de la carta bajo (x, y) en coordenadas de mundo, o -1. */
        fun cardAt(x: Float, y: Float): Int {
            for (i in 0 until 4) {
                val l = CARD_X0 + i * (CARD_W + CARD_GAP)
                if (x in l..l + CARD_W && y in CARD_Y..CARD_Y + CARD_H) return i
            }
            return -1
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    fun draw(canvas: Canvas, s: GameState) {
        val w = GameState.WORLD_W
        val h = GameState.WORLD_H
        canvas.drawColor(Color.BLACK)
        val scale = minOf(canvas.width / w, canvas.height / GameState.TOTAL_H)
        canvas.save()
        canvas.translate((canvas.width - w * scale) / 2f, (canvas.height - GameState.TOTAL_H * scale) / 2f)
        canvas.scale(scale, scale)

        // Césped
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(96, 160, 72)
        canvas.drawRect(0f, 0f, w, h, paint)

        // Carriles
        paint.color = Color.rgb(120, 180, 90)
        for (x in listOf(GameState.LANE_LEFT_X, GameState.LANE_RIGHT_X)) {
            canvas.drawRect(x - 35f, 0f, x + 35f, h, paint)
        }

        // Río
        val ry = GameState.RIVER_Y
        paint.color = Color.rgb(60, 130, 210)
        canvas.drawRect(0f, ry - 24f, w, ry + 24f, paint)

        // Puentes
        paint.color = Color.rgb(140, 100, 60)
        for (x in listOf(GameState.LANE_LEFT_X, GameState.LANE_RIGHT_X)) {
            canvas.drawRect(x - 28f, ry - 28f, x + 28f, ry + 28f, paint)
        }

        for (z in s.zones) if (z.spell.duration > 0f) drawZone(canvas, z)
        for (t in s.towers) drawTower(canvas, t)
        for (u in s.units) drawUnit(canvas, u)
        for (z in s.zones) if (z.spell.duration <= 0f) drawZone(canvas, z)
        paint.style = Paint.Style.FILL
        paint.color = Color.YELLOW
        for (p in s.projectiles) canvas.drawCircle(p.x, p.y, 3f, paint)
        for (b in s.blasts) drawBlast(canvas, b)
        for (d in s.texts) {
            val a = (1f - d.age / 0.7f).coerceIn(0f, 1f)
            paint.style = Paint.Style.FILL
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 12f
            paint.color = Color.argb((255 * a).toInt(), 255, if (d.team == Team.PLAYER) 90 else 240, 60)
            canvas.drawText(d.amount.toString(), d.x, d.y - d.age * 30f, paint)
        }
        drawHud(canvas, s)
        drawTopInfo(canvas, s)
        if (s.paused) {
            paint.style = Paint.Style.FILL
            paint.color = Color.argb(160, 0, 0, 0)
            canvas.drawRect(0f, 0f, w, GameState.TOTAL_H, paint)
            paint.color = Color.WHITE
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 28f
            canvas.drawText("PAUSA", w / 2f, h / 2f, paint)
            paint.textSize = 14f
            canvas.drawText("Toca para continuar", w / 2f, h / 2f + 26f, paint)
        }
        canvas.restore()
    }

    private fun drawTopInfo(canvas: Canvas, s: GameState) {
        val t = Math.ceil(s.timeLeft.toDouble()).toInt()
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(150, 0, 0, 0)
        rect.set(4f, 4f, 76f, 46f)
        canvas.drawRoundRect(rect, 6f, 6f, paint)
        rect.set(GameState.WORLD_W - 76f, 4f, GameState.WORLD_W - 4f, 46f)
        canvas.drawRoundRect(rect, 6f, 6f, paint)
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 18f
        canvas.drawText("%d:%02d".format(t / 60, t % 60), 40f, 24f, paint)
        if (s.lastMinute) {
            paint.color = Color.rgb(230, 120, 255)
            paint.textSize = 12f
            canvas.drawText("ELIXIR x2", 40f, 40f, paint)
        }
        paint.color = Color.WHITE
        paint.textSize = 12f
        canvas.drawText("Coronas", GameState.WORLD_W - 40f, 18f, paint)
        paint.textSize = 18f
        canvas.drawText("${s.crowns(Team.PLAYER)} - ${s.crowns(Team.CPU)}", GameState.WORLD_W - 40f, 40f, paint)
    }

    private fun drawHud(canvas: Canvas, s: GameState) {
        val w = GameState.WORLD_W
        val top = GameState.WORLD_H
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(30, 30, 40)
        canvas.drawRect(0f, top, w, GameState.TOTAL_H, paint)

        // Barra de elixir
        val ex = CARD_X0
        val ey = top + 10f
        val ew = w - 2 * CARD_X0
        paint.color = Color.DKGRAY
        rect.set(ex, ey, ex + ew, ey + 18f)
        canvas.drawRoundRect(rect, 6f, 6f, paint)
        paint.color = Color.rgb(200, 60, 230)
        rect.set(ex, ey, ex + ew * s.playerElixir.value / ElixirBar.MAX, ey + 18f)
        canvas.drawRoundRect(rect, 6f, 6f, paint)
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 14f
        canvas.drawText(s.playerElixir.value.toInt().toString(), ex + ew / 2f, ey + 14f, paint)

        // Mano de 4 cartas
        val sel = s.selected
        for ((i, c) in s.playerDeck.hand.withIndex()) {
            val l = CARD_X0 + i * (CARD_W + CARD_GAP)
            val ok = s.playerElixir.canSpend(c.cost)
            paint.style = Paint.Style.FILL
            paint.color = if (ok) Color.rgb(70, 90, 140) else Color.rgb(60, 60, 70)
            rect.set(l, CARD_Y, l + CARD_W, CARD_Y + CARD_H)
            canvas.drawRoundRect(rect, 8f, 8f, paint)
            if (i == sel) {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                paint.color = Color.YELLOW
                canvas.drawRoundRect(rect, 8f, 8f, paint)
                paint.style = Paint.Style.FILL
            }
            paint.color = if (ok) Color.WHITE else Color.LTGRAY
            paint.textSize = 12f
            canvas.drawText(c.name, l + CARD_W / 2f, CARD_Y + CARD_H / 2f + 4f, paint)
            paint.color = Color.rgb(200, 60, 230)
            canvas.drawCircle(l + 13f, CARD_Y + 13f, 11f, paint)
            paint.color = Color.WHITE
            paint.textSize = 14f
            canvas.drawText(c.cost.toString(), l + 13f, CARD_Y + 18f, paint)
        }
    }

    private fun drawUnit(canvas: Canvas, u: GameUnit) {
        paint.style = Paint.Style.FILL
        paint.color = if (u.team == Team.PLAYER) Color.rgb(90, 150, 255) else Color.rgb(255, 110, 110)
        if (u.flash > 0f) paint.color = Color.WHITE
        canvas.drawCircle(u.x, u.y, u.radius, paint)
        if (u.ranged) {
            paint.color = Color.WHITE
            canvas.drawCircle(u.x, u.y, 3f, paint)
        }
        val by = u.y - u.radius - 7f
        paint.color = Color.DKGRAY
        rect.set(u.x - u.radius, by, u.x + u.radius, by + 4f)
        canvas.drawRect(rect, paint)
        paint.color = Color.rgb(60, 220, 80)
        rect.set(u.x - u.radius, by, u.x - u.radius + u.radius * 2f * (u.hp / u.maxHp).coerceIn(0f, 1f), by + 4f)
        canvas.drawRect(rect, paint)
    }

    private fun drawZone(canvas: Canvas, z: com.josue.arenajoshuel.model.SpellZone) {
        val r = z.spell.radius
        paint.style = Paint.Style.FILL
        if (z.spell.duration > 0f) {
            val fade = ((z.life - z.age) / 1f).coerceIn(0f, 1f)
            val pulse = 1f + 0.04f * Math.sin(z.age * 6.0).toFloat()
            paint.color = Color.argb((90 * fade).toInt(), 60, 200, 60)
            canvas.drawCircle(z.x, z.y, r * pulse, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            paint.color = Color.argb((255 * fade).toInt(), 40, 160, 40)
            canvas.drawCircle(z.x, z.y, r * pulse, paint)
            return
        }
        // Flechas: caen desde la torre rey hacia el objetivo
        val k = (z.age / 0.5f).coerceIn(0f, 1f)
        paint.color = Color.argb(((1f - k) * 70).toInt(), 255, 230, 120)
        canvas.drawCircle(z.x, z.y, r, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.argb(((1f - k) * 255).toInt(), 240, 230, 200)
        val f = (k * 2f).coerceAtMost(1f)
        for (i in 0 until 7) {
            val a = i * 0.9
            val tx = z.x + (Math.cos(a) * r * 0.7 * (i % 3 + 1) / 3).toFloat()
            val ty = z.y + (Math.sin(a) * r * 0.7 * (i % 3 + 1) / 3).toFloat()
            val hx = z.fromX + (tx - z.fromX) * f
            val hy = z.fromY + (ty - z.fromY) * f
            val tl = maxOf(f - 0.12f, 0f)
            canvas.drawLine(z.fromX + (tx - z.fromX) * tl, z.fromY + (ty - z.fromY) * tl, hx, hy, paint)
        }
    }

    private fun drawBlast(canvas: Canvas, b: com.josue.arenajoshuel.model.Blast) {
        val k = (b.age / 1.2f).coerceIn(0f, 1f)
        val r = (if (b.big) 70f else 48f) * k
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(((1f - k) * 200).toInt(), 255, 170, 40)
        canvas.drawCircle(b.x, b.y, r, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.argb(((1f - k) * 255).toInt(), 255, 240, 200)
        canvas.drawCircle(b.x, b.y, r * 1.2f, paint)
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(((1f - k) * 255).toInt(), 90, 90, 90)
        for (i in 0 until 8) {
            val a = i * Math.PI / 4
            canvas.drawCircle(b.x + (Math.cos(a) * r * 1.4).toFloat(), b.y + (Math.sin(a) * r * 1.4).toFloat(), 4f * (1f - k) + 1f, paint)
        }
    }

    private fun drawTower(canvas: Canvas, t: com.josue.arenajoshuel.model.Tower) {
        val half = if (t.isKing) 30f else 22f
        paint.style = Paint.Style.FILL
        paint.color = if (!t.alive) Color.GRAY
        else if (t.team == Team.PLAYER) Color.rgb(40, 100, 220) else Color.rgb(210, 50, 50)
        if (t.flash > 0f) paint.color = Color.WHITE
        rect.set(t.x - half, t.y - half, t.x + half, t.y + half)
        canvas.drawRoundRect(rect, 6f, 6f, paint)
        if (t.isKing) {
            paint.color = Color.rgb(255, 215, 0)
            canvas.drawCircle(t.x, t.y, 9f, paint)
        }

        // Barra de vida
        val bw = half * 2f
        val by = t.y - half - 10f
        paint.color = Color.DKGRAY
        rect.set(t.x - half, by, t.x - half + bw, by + 6f)
        canvas.drawRect(rect, paint)
        paint.color = Color.rgb(60, 220, 80)
        rect.set(t.x - half, by, t.x - half + bw * (t.hp / t.maxHp).coerceIn(0f, 1f), by + 6f)
        canvas.drawRect(rect, paint)
    }
}
