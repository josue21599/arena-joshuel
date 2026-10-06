package com.josue.arenajoshuel.ui

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.josue.arenajoshuel.cards.DeckStore
import com.josue.arenajoshuel.render.ChestView

class ChestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ChestView(this, { DeckStore.openChest(this) }) {
            setResult(Activity.RESULT_OK)
            finish()
        })
    }
}
