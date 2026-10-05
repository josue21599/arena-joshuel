package com.josue.arenajoshuel.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.content.Intent
import com.josue.arenajoshuel.ai.Difficulty
import com.josue.arenajoshuel.engine.GameView

class MainActivity : AppCompatActivity() {
    private var view: GameView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        val diff = Difficulty.valueOf(intent.getStringExtra(EXTRA_DIFFICULTY) ?: Difficulty.MEDIUM.name)
        val v = GameView(this, diff) { r ->
            startActivity(
                Intent(this, ResultActivity::class.java)
                    .putExtra(ResultActivity.EXTRA_RESULT, r.name)
                    .putExtra(EXTRA_DIFFICULTY, diff.name)
            )
            finish()
        }
        view = v
        setContentView(v)
    }

    override fun onPause() {
        view?.pause()
        super.onPause()
    }

    companion object { const val EXTRA_DIFFICULTY = "difficulty" }
}
