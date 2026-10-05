package com.josue.arenajoshuel.model

import com.josue.arenajoshuel.ai.CpuPlayer
import com.josue.arenajoshuel.ai.Difficulty
import com.josue.arenajoshuel.cards.CardDef
import com.josue.arenajoshuel.cards.CardDefs
import com.josue.arenajoshuel.cards.Deck
import com.josue.arenajoshuel.cards.ElixirBar
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.abs
import kotlin.math.hypot

enum class Result { WIN, LOSE, DRAW }

class DamageText(val x: Float, val y: Float, val amount: Int, val team: Team, var age: Float = 0f)

/** Explosión de torre destruida. */
class Blast(val x: Float, val y: Float, val big: Boolean, var age: Float = 0f)

class GameState(difficulty: Difficulty = Difficulty.MEDIUM) {
    companion object {
        const val WORLD_W = 360f
        const val WORLD_H = 640f
        const val HUD_H = 128f
        const val TOTAL_H = WORLD_H + HUD_H
        const val RIVER_Y = WORLD_H / 2f
        const val RIVER_HALF = 24f
        const val BRIDGE_HALF = 26f
        const val LANE_LEFT_X = 90f
        const val LANE_RIGHT_X = 270f
        const val SIDE_HP = 1400f
        const val KING_HP = 2400f
        const val DT = 1f / 60f
        const val TOWER_RANGE = 130f
        const val TOWER_DAMAGE = 50f
        const val TOWER_INTERVAL = 0.8f
        const val MATCH_TIME = 180f
    }

    val towers: List<Tower> = listOf(
        Tower(Team.CPU, false, Lane.LEFT, LANE_LEFT_X, 150f, SIDE_HP),
        Tower(Team.CPU, false, Lane.RIGHT, LANE_RIGHT_X, 150f, SIDE_HP),
        Tower(Team.CPU, true, null, WORLD_W / 2f, 60f, KING_HP),
        Tower(Team.PLAYER, false, Lane.LEFT, LANE_LEFT_X, 490f, SIDE_HP),
        Tower(Team.PLAYER, false, Lane.RIGHT, LANE_RIGHT_X, 490f, SIDE_HP),
        Tower(Team.PLAYER, true, null, WORLD_W / 2f, 580f, KING_HP),
    )
    val units = ArrayList<Unit>()
    val projectiles = ArrayList<Projectile>()
    val texts = ArrayList<DamageText>()
    val blasts = ArrayList<Blast>()
    private val wasAlive = HashSet<Tower>(towers)

    class Deploy(val handIndex: Int, val lane: Lane, val y: Float)

    private val deployQueue = ConcurrentLinkedQueue<Deploy>()
    val playerElixir = ElixirBar()
    val playerDeck = Deck(CardDefs.ALL.shuffled())
    @Volatile var selected = -1

    /** Se llama desde el hilo de UI; se procesa en el bucle. */
    fun requestDeploy(handIndex: Int, lane: Lane, y: Float) {
        deployQueue.add(Deploy(handIndex, lane, y))
    }

    fun laneX(lane: Lane) = if (lane == Lane.LEFT) LANE_LEFT_X else LANE_RIGHT_X

    val cpu = CpuPlayer(this, difficulty)
    var elapsed = 0f
    @Volatile var paused = false
    @Volatile var result: Result? = null
    val timeLeft: Float get() = (MATCH_TIME - elapsed).coerceAtLeast(0f)

    /** Coronas = torres enemigas destruidas. */
    fun crowns(team: Team) = towers.count { it.team != team && !it.alive }
    val lastMinute: Boolean get() = elapsed >= 120f

    fun spawn(card: CardDef, team: Team, lane: Lane, y: Float) {
        val x = laneX(lane)
        for (i in 0 until card.count) {
            val ox = (i - (card.count - 1) / 2f) * 12f
            units += Unit(
                team, lane, x + ox, y, card.hp, card.damage, card.speed, card.range,
                card.attackInterval, targets = card.targets, ranged = card.ranged, radius = card.radius,
            )
        }
    }

    fun update() {
        if (paused || result != null) return
        elapsed += DT
        cpu.update()
        playerElixir.update(DT, lastMinute)
        while (true) {
            val d = deployQueue.poll() ?: break
            val card = playerDeck.hand.getOrNull(d.handIndex) ?: continue
            if (!playerElixir.canSpend(card.cost)) continue
            playerDeck.play(d.handIndex)
            playerElixir.spend(card.cost)
            spawn(card, Team.PLAYER, d.lane, d.y)
            selected = -1
        }
        for (u in units) updateUnit(u)
        for (t in towers) updateTower(t)
        for (p in projectiles) updateProjectile(p)
        for (t in towers) if (!t.alive && wasAlive.remove(t)) blasts += Blast(t.x, t.y, t.isKing)
        updateEffects()
        units.removeAll { !it.alive }
        projectiles.removeAll { it.dead }
        checkEnd()
    }

    private fun hit(t: Targetable, dmg: Float) {
        if (!t.alive) return
        t.hp -= dmg
        t.flash = 0.15f
        texts += DamageText(t.x + (Math.random().toFloat() - 0.5f) * 14f, t.y - t.radius - 8f, dmg.toInt(), t.team)
    }

    private fun updateEffects() {
        for (u in units) if (u.flash > 0f) u.flash -= DT
        for (t in towers) if (t.flash > 0f) t.flash -= DT
        for (x in texts) x.age += DT
        texts.removeAll { it.age > 0.7f }
        for (b in blasts) b.age += DT
        blasts.removeAll { it.age > 1.2f }
    }

    private fun checkEnd() {
        val cpuKingDown = towers.any { it.isKing && it.team == Team.CPU && !it.alive }
        val myKingDown = towers.any { it.isKing && it.team == Team.PLAYER && !it.alive }
        if (!cpuKingDown && !myKingDown && elapsed < MATCH_TIME) return
        val p = crowns(Team.PLAYER)
        val c = crowns(Team.CPU)
        result = when {
            cpuKingDown && !myKingDown -> Result.WIN
            myKingDown && !cpuKingDown -> Result.LOSE
            p > c -> Result.WIN
            p < c -> Result.LOSE
            else -> Result.DRAW
        }
    }

    private fun dist(ax: Float, ay: Float, b: Targetable) = hypot(b.x - ax, b.y - ay)

    private fun acquire(u: Unit): Targetable? {
        var best: Targetable? = null
        var bestD = u.sight
        if (u.targets == TargetType.GROUND) {
            for (e in units) {
                if (e.team == u.team || !e.alive) continue
                val d = dist(u.x, u.y, e)
                if (d <= bestD) { best = e; bestD = d }
            }
        }
        for (t in towers) {
            if (t.team == u.team || !t.alive) continue
            val d = dist(u.x, u.y, t)
            if (d <= bestD) { best = t; bestD = d }
        }
        if (best != null) return best
        // Sin enemigos a la vista: ir a la torre enemiga más cercana
        var minD = Float.MAX_VALUE
        for (t in towers) {
            if (t.team == u.team || !t.alive) continue
            val d = dist(u.x, u.y, t)
            if (d < minD) { best = t; minD = d }
        }
        return best
    }

    private fun updateUnit(u: Unit) {
        if (u.cooldown > 0f) u.cooldown -= DT
        val tgt = acquire(u)
        u.target = tgt ?: return
        if (dist(u.x, u.y, tgt) - tgt.radius - u.radius <= u.range) {
            if (u.cooldown <= 0f) {
                u.cooldown = u.attackInterval
                if (u.ranged) projectiles += Projectile(u.team, u.x, u.y, tgt, u.damage)
                else hit(tgt, u.damage)
            }
            return
        }
        move(u, tgt)
    }

    private fun move(u: Unit, tgt: Targetable) {
        var ax = tgt.x
        var ay = tgt.y
        val bx = if (abs(u.x - LANE_LEFT_X) <= abs(u.x - LANE_RIGHT_X)) LANE_LEFT_X else LANE_RIGHT_X
        val inBand = abs(u.y - RIVER_Y) < RIVER_HALF
        if (!inBand && (u.y < RIVER_Y) != (tgt.y < RIVER_Y)) {
            ax = bx
            ay = RIVER_Y
        }
        val dx = ax - u.x
        val dy = ay - u.y
        val d = hypot(dx, dy)
        if (d < 0.001f) return
        val step = minOf(u.speed * DT, d)
        u.x += dx / d * step
        u.y += dy / d * step
        if (abs(u.y - RIVER_Y) < RIVER_HALF) u.x = u.x.coerceIn(bx - BRIDGE_HALF, bx + BRIDGE_HALF)
    }

    private fun updateTower(t: Tower) {
        if (!t.alive) return
        if (t.cooldown > 0f) t.cooldown -= DT
        if (t.cooldown > 0f) return
        var best: Unit? = null
        var bestD = TOWER_RANGE
        for (e in units) {
            if (e.team == t.team || !e.alive) continue
            val d = dist(t.x, t.y, e)
            if (d <= bestD) { best = e; bestD = d }
        }
        val e = best ?: return
        t.cooldown = TOWER_INTERVAL
        projectiles += Projectile(t.team, t.x, t.y, e, TOWER_DAMAGE)
    }

    private fun updateProjectile(p: Projectile) {
        if (!p.target.alive) { p.dead = true; return }
        val dx = p.target.x - p.x
        val dy = p.target.y - p.y
        val d = hypot(dx, dy)
        val step = p.speed * DT
        if (d <= step + 2f) {
            hit(p.target, p.damage)
            p.dead = true
        } else {
            p.x += dx / d * step
            p.y += dy / d * step
        }
    }
}
