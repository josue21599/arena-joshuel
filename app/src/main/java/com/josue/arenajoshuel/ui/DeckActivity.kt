package com.josue.arenajoshuel.ui

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.josue.arenajoshuel.cards.CardDef
import com.josue.arenajoshuel.cards.CardDefs
import com.josue.arenajoshuel.cards.DeckStore

class DeckActivity : AppCompatActivity() {
    private val deck = ArrayList<CardDef>()
    private lateinit var info: TextView
    private lateinit var deckGrid: GridLayout
    private lateinit var collGrid: GridLayout
    private lateinit var unlocked: Set<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        unlocked = DeckStore.unlocked(this)
        deck += DeckStore.savedDeck(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(30, 30, 40))
            setPadding(24, 48, 24, 24)
        }
        info = label(root, 16f)
        deckGrid = grid(root)
        label(root, 16f).text = "Colección (toca para añadir/quitar)"
        collGrid = grid(null)
        root.addView(ScrollView(this).apply { addView(collGrid) }, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(Button(this).apply { text = "Volver"; setOnClickListener { finish() } })
        setContentView(root)
        refresh()
    }

    private fun label(parent: LinearLayout, size: Float) = TextView(this).also {
        it.textSize = size
        it.setTextColor(Color.WHITE)
        it.setPadding(0, 16, 0, 8)
        parent.addView(it)
    }

    private fun grid(parent: LinearLayout?) = GridLayout(this).also {
        it.columnCount = 4
        parent?.addView(it)
    }

    private fun cell(text: String, color: Int, onClick: () -> Unit) = Button(this).apply {
        this.text = text
        textSize = 11f
        setTextColor(Color.WHITE)
        setBackgroundColor(color)
        setOnClickListener { onClick() }
        layoutParams = GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED, 1f), GridLayout.spec(GridLayout.UNDEFINED, 1f)
        ).apply { width = 0; height = 170; setMargins(4, 4, 4, 4) }
    }

    private fun refresh() {
        val avg = if (deck.isEmpty()) 0.0 else deck.sumOf { it.cost } / deck.size.toDouble()
        info.text = "Mazo ${deck.size}/${DeckStore.DECK_SIZE} · Elixir medio: %.1f".format(avg) +
            if (deck.size < DeckStore.DECK_SIZE) "  (faltan cartas para jugar)" else ""
        deckGrid.removeAllViews()
        for (i in 0 until DeckStore.DECK_SIZE) {
            val c = deck.getOrNull(i)
            deckGrid.addView(
                if (c == null) cell("vacío", Color.rgb(50, 50, 60)) {}
                else cell("${c.name}\n${c.cost}", Color.rgb(70, 90, 140)) { toggle(c) }
            )
        }
        collGrid.removeAllViews()
        val copies = DeckStore.copies(this)
        for (c in CardDefs.ALL) {
            val locked = c.name !in unlocked
            val color = when {
                locked -> Color.rgb(60, 60, 60)
                c in deck -> Color.rgb(40, 130, 70)
                else -> Color.rgb(90, 90, 130)
            }
            collGrid.addView(cell(if (locked) "${c.name}\n(bloqueada)" else "${c.name}\n${c.cost} · x${copies[c.name]}", color) { toggle(c) })
        }
    }

    private fun toggle(c: CardDef) {
        when {
            c.name !in unlocked -> { Toast.makeText(this, "Gana partidas para desbloquearla", Toast.LENGTH_SHORT).show(); return }
            c in deck -> deck.remove(c)
            deck.size >= DeckStore.DECK_SIZE -> { Toast.makeText(this, "Mazo lleno: quita una carta", Toast.LENGTH_SHORT).show(); return }
            else -> deck.add(c)
        }
        DeckStore.saveDeck(this, deck)
        refresh()
    }
}
