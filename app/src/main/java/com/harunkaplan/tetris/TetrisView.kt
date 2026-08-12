package com.harunkaplan.tetris

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.random.Random

class TetrisView(context: Context) : View(context) {
    private val cols = 10
    private val rows = 20
    private val board = Array(rows) { IntArray(cols) }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val colors = intArrayOf(0, 0xff4dd0e1.toInt(), 0xff1976d2.toInt(), 0xffff9800.toInt(), 0xffffeb3b.toInt(), 0xff43a047.toInt(), 0xffab47bc.toInt(), 0xffef5350.toInt())
    private val shapes = arrayOf(
        arrayOf("1111"), arrayOf("100","111"), arrayOf("001","111"), arrayOf("11","11"),
        arrayOf("011","110"), arrayOf("010","111"), arrayOf("110","011")
    )
    private var shape = shapes[0]
    private var pieceX = 3
    private var pieceY = 0
    private var pieceColor = 1
    private var score = 0
    private var lines = 0
    private var level = 1
    private var gameOver = false
    private var lastDrop = System.currentTimeMillis()
    private var downTouch = false

    init {
        paint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        spawn()
        isFocusable = true
    }

    private fun spawn() {
        shape = shapes[Random.nextInt(shapes.size)]
        pieceColor = Random.nextInt(1, colors.size)
        pieceX = (cols - shape[0].length) / 2
        pieceY = 0
        if (!canPlace(pieceX, pieceY, shape)) gameOver = true
    }

    private fun canPlace(x: Int, y: Int, s: Array<String>): Boolean {
        for (r in s.indices) for (c in s[r].indices) if (s[r][c] == '1') {
            val bx = x + c; val by = y + r
            if (bx !in 0 until cols || by !in 0 until rows || board[by][bx] != 0) return false
        }
        return true
    }

    private fun move(dx: Int) { if (!gameOver && canPlace(pieceX + dx, pieceY, shape)) pieceX += dx }

    private fun rotate() {
        if (gameOver) return
        val h = shape.size; val w = shape[0].length
        val rotated = Array(w) { StringBuilder(h) }
        for (r in 0 until h) for (c in 0 until w) rotated[c].append(shape[h - 1 - r][c])
        val next = Array(w) { rotated[it].toString() }
        if (canPlace(pieceX, pieceY, next)) shape = next
    }

    private fun drop() {
        if (gameOver) return
        if (canPlace(pieceX, pieceY + 1, shape)) pieceY++ else lock()
    }

    private fun lock() {
        for (r in shape.indices) for (c in shape[r].indices) if (shape[r][c] == '1') board[pieceY + r][pieceX + c] = pieceColor
        var cleared = 0
        for (r in rows - 1 downTo 0) if (board[r].all { it != 0 }) {
            for (y in r downTo 1) board[y] = board[y - 1].clone()
            board[0] = IntArray(cols); cleared++
        }
        if (cleared > 0) {
            lines += cleared
            score += when (cleared) { 1 -> 100; 2 -> 300; 3 -> 500; else -> 800 } * level
            level = max(1, lines / 10 + 1)
        }
        spawn()
    }

    private fun reset() {
        for (r in 0 until rows) board[r].fill(0)
        score = 0; lines = 0; level = 1; gameOver = false; spawn(); lastDrop = System.currentTimeMillis()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(0xff10131a.toInt())
        val cell = minOf(width / 12f, height / 23f)
        val left = (width - cols * cell) / 2f
        val top = 85f
        paint.style = Paint.Style.FILL
        paint.color = 0xff1b202b.toInt()
        canvas.drawRect(left, top, left + cols * cell, top + rows * cell, paint)
        paint.style = Paint.Style.STROKE; paint.strokeWidth = 1f; paint.color = 0xff303746.toInt()
        for (x in 0..cols) canvas.drawLine(left + x * cell, top, left + x * cell, top + rows * cell, paint)
        for (y in 0..rows) canvas.drawLine(left, top + y * cell, left + cols * cell, top + y * cell, paint)
        paint.style = Paint.Style.FILL
        for (r in 0 until rows) for (c in 0 until cols) if (board[r][c] != 0) drawCell(canvas, left + c * cell, top + r * cell, cell, colors[board[r][c]])
        if (!gameOver) for (r in shape.indices) for (c in shape[r].indices) if (shape[r][c] == '1') drawCell(canvas, left + (pieceX + c) * cell, top + (pieceY + r) * cell, cell, colors[pieceColor])

        paint.color = 0xffffffff.toInt(); paint.textAlign = Paint.Align.CENTER; paint.textSize = 30f
        canvas.drawText("TETRIS", width / 2f, 38f, paint)
        paint.textSize = 17f; paint.textAlign = Paint.Align.LEFT
        canvas.drawText("PUAN: $score", 18f, 38f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("LEVEL: $level", width - 18f, 38f, paint)

        paint.textAlign = Paint.Align.CENTER; paint.textSize = 18f
        canvas.drawText("◀", width * .18f, height - 42f, paint)
        canvas.drawText("▼", width * .50f, height - 42f, paint)
        canvas.drawText("▶", width * .82f, height - 42f, paint)
        canvas.drawText("↻", width * .50f, height - 82f, paint)
        if (gameOver) {
            paint.color = 0xcc000000.toInt(); canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.color = 0xffffffff.toInt(); paint.textSize = 32f
            canvas.drawText("OYUN BİTTİ", width / 2f, height / 2f - 20f, paint)
            paint.textSize = 20f; canvas.drawText("Puan: $score", width / 2f, height / 2f + 20f, paint)
            canvas.drawText("Tekrar başlamak için dokun", width / 2f, height / 2f + 65f, paint)
        }
        if (!gameOver && System.currentTimeMillis() - lastDrop > max(120L, 700L - (level - 1) * 55L)) { drop(); lastDrop = System.currentTimeMillis(); invalidate() }
        postInvalidateDelayed(40)
    }

    private fun drawCell(canvas: Canvas, x: Float, y: Float, size: Float, color: Int) {
        paint.color = color; paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(x + 1, y + 1, x + size - 1, y + size - 1), 5f, 5f, paint)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_DOWN) {
            if (gameOver) { reset(); invalidate(); return true }
            downTouch = true; return true
        }
        if (e.action == MotionEvent.ACTION_UP && downTouch) {
            downTouch = false
            val x = e.x; val y = e.y
            if (y > height - 115) {
                when {
                    x < width * .33f -> move(-1)
                    x > width * .67f -> move(1)
                    else -> drop()
                }
            } else if (y > height - 160 && x in width * .35f..width * .65f) rotate()
            else if (x < width / 2f) move(-1) else move(1)
            invalidate(); return true
        }
        return true
    }
}
