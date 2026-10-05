package com.josue.arenajoshuel.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.josue.arenajoshuel.ai.Difficulty

class MenuActivity : AppCompatActivity() {
    private var difficulty = Difficulty.MEDIUM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val names = mapOf(Difficulty.EASY to "Fácil", Difficulty.MEDIUM to "Media", Difficulty.HARD to "Difícil")
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(30, 30, 40))
            setPadding(48, 48, 48, 48)
        }
        root.addView(TextView(this).apply {
            text = "Arena Duel"
            textSize = 36f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        })
        val diffBtn = Button(this)
        fun refresh() { diffBtn.text = "Dificultad: ${names[difficulty]}" }
        refresh()
        diffBtn.setOnClickListener {
            difficulty = Difficulty.values()[(difficulty.ordinal + 1) % Difficulty.values().size]
            refresh()
        }
        val play = Button(this).apply {
            text = "Jugar"
            setOnClickListener {
                startActivity(
                    Intent(this@MenuActivity, MainActivity::class.java)
                        .putExtra(MainActivity.EXTRA_DIFFICULTY, difficulty.name)
                )
            }
        }
        root.addView(play)
        root.addView(diffBtn)
        setContentView(root)
    }
}
