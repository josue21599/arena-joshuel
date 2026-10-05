package com.josue.arenajoshuel.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.josue.arenajoshuel.model.Result

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val result = Result.valueOf(intent.getStringExtra(EXTRA_RESULT) ?: Result.DRAW.name)
        val diff = intent.getStringExtra(MainActivity.EXTRA_DIFFICULTY)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(30, 30, 40))
            setPadding(48, 48, 48, 48)
        }
        root.addView(TextView(this).apply {
            text = when (result) { Result.WIN -> "¡VICTORIA!"; Result.LOSE -> "DERROTA"; Result.DRAW -> "EMPATE" }
            textSize = 40f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        })
        root.addView(Button(this).apply {
            text = "Revancha"
            setOnClickListener {
                startActivity(
                    Intent(this@ResultActivity, MainActivity::class.java)
                        .putExtra(MainActivity.EXTRA_DIFFICULTY, diff)
                )
                finish()
            }
        })
        root.addView(Button(this).apply {
            text = "Menú"
            setOnClickListener { finish() }
        })
        setContentView(root)
    }

    companion object { const val EXTRA_RESULT = "result" }
}
