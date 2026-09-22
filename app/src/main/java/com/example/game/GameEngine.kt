package com.example.game

import kotlin.random.Random

data class Tile(val value: Int, val id: Int, val isMerged: Boolean = false, val isNew: Boolean = false, val mergedFrom: List<Tile> = emptyList(), val x: Int = -1, val y: Int = -1, val prevX: Int = -1, val prevY: Int = -1)

class GameEngine(private val width: Int = 5, private val height: Int = 5) {
    var grid: Array<Array<Tile?>> = Array(width) { Array(height) { null } }
        private set
    var score: Int = 0
        private set
    var moveCount: Int = 0
        private set

    private var previousGrid: Array<Array<Tile?>>? = null
    private var previousScore: Int = 0
    private var previousMoveCount: Int = 0

    private var tileIdCounter = 0

    fun reset() {
        grid = Array(width) { Array(height) { null } }
        score = 0
        moveCount = 0
        tileIdCounter = 0
        previousGrid = null
        spawnTile()
        spawnTile()
    }

    fun canUndo(): Boolean = previousGrid != null

    fun undo(): Boolean {
        val pGrid = previousGrid ?: return false
        grid = pGrid
        score = previousScore
        moveCount = previousMoveCount
        previousGrid = null
        return true
    }

    fun spawnTile(): Boolean {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (x in 0 until width) {
            for (y in 0 until height) {
                if (grid[x][y] == null) {
                    emptyCells.add(Pair(x, y))
                }
            }
        }
        if (emptyCells.isEmpty()) return false

        val (x, y) = emptyCells.random()
        val value = if (Random.nextDouble() < 0.9) 2 else 4
        grid[x][y] = Tile(value, ++tileIdCounter, isNew = true, x = x, y = y, prevX = x, prevY = y)
        return true
    }

    enum class Direction { UP, DOWN, LEFT, RIGHT }

    fun move(direction: Direction): Boolean {
        // Prepare current state for animation: set prevX and prevY
        for (x in 0 until width) {
            for (y in 0 until height) {
                val t = grid[x][y]
                if (t != null) {
                    grid[x][y] = t.copy(prevX = x, prevY = y, isMerged = false, isNew = false, mergedFrom = emptyList(), x = x, y = y)
                }
            }
        }

        val currentGrid = Array(width) { x -> Array(height) { y -> grid[x][y] } }
        val currentScore = score
        val currentMoveCount = moveCount

        var moved = false
        val newGrid = Array(width) { Array<Tile?>(height) { null } }
        var gainedScore = 0

        fun processLine(line: List<Tile?>): List<Tile?> {
            val nonNulls = line.filterNotNull().toMutableList()
            val merged = mutableListOf<Tile?>()
            var i = 0
            while (i < nonNulls.size) {
                if (i + 1 < nonNulls.size && nonNulls[i].value == nonNulls[i+1].value) {
                    val newValue = nonNulls[i].value * 2
                    gainedScore += newValue
                    merged.add(
                        Tile(
                            value = newValue, 
                            id = ++tileIdCounter, 
                            isMerged = true, 
                            mergedFrom = listOf(nonNulls[i], nonNulls[i+1])
                        )
                    )
                    i += 2
                    moved = true
                } else {
                    merged.add(nonNulls[i])
                    i++
                }
            }
            while (merged.size < line.size) {
                merged.add(null)
            }
            return merged
        }

        when (direction) {
            Direction.UP -> {
                for (x in 0 until width) {
                    val col = (0 until height).map { y -> grid[x][y] }
                    val newCol = processLine(col)
                    if (col != newCol) moved = true
                    for (y in 0 until height) newGrid[x][y] = newCol[y]
                }
            }
            Direction.DOWN -> {
                for (x in 0 until width) {
                    val col = (height - 1 downTo 0).map { y -> grid[x][y] }
                    val newCol = processLine(col)
                    if (col != newCol) moved = true
                    for (y in 0 until height) newGrid[x][height - 1 - y] = newCol[y]
                }
            }
            Direction.LEFT -> {
                for (y in 0 until height) {
                    val row = (0 until width).map { x -> grid[x][y] }
                    val newRow = processLine(row)
                    if (row != newRow) moved = true
                    for (x in 0 until width) newGrid[x][y] = newRow[x]
                }
            }
            Direction.RIGHT -> {
                for (y in 0 until height) {
                    val row = (width - 1 downTo 0).map { x -> grid[x][y] }
                    val newRow = processLine(row)
                    if (row != newRow) moved = true
                    for (x in 0 until width) newGrid[width - 1 - x][y] = newRow[x]
                }
            }
        }

        if (moved) {
            for (x in 0 until width) {
                for (y in 0 until height) {
                    val t = newGrid[x][y]
                    if (t != null) {
                        val updatedMergedFrom = t.mergedFrom.map { it.copy(x = x, y = y) }
                        newGrid[x][y] = t.copy(x = x, y = y, mergedFrom = updatedMergedFrom)
                    }
                }
            }
            previousGrid = currentGrid
            previousScore = currentScore
            previousMoveCount = currentMoveCount
            grid = newGrid
            score += gainedScore
            moveCount++
            spawnTile()
        }
        return moved
    }
    fun hasPossibleMoves(): Boolean {
        for (x in 0 until width) {
            for (y in 0 until height) {
                if (grid[x][y] == null) return true
                val current = grid[x][y]?.value ?: continue
                if (x + 1 < width && grid[x+1][y]?.value == current) return true
                if (y + 1 < height && grid[x][y+1]?.value == current) return true
            }
        }
        return false
    }
    
    fun hasReachedTarget(target: Int): Boolean {
        for (x in 0 until width) {
            for (y in 0 until height) {
                if ((grid[x][y]?.value ?: 0) >= target) return true
            }
        }
        return false
    }

    fun getHint(): Direction? {
        val directions = listOf(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)
        var bestDirection: Direction? = null
        var maxGain = -1

        for (dir in directions) {
            var gainedScore = 0
            var moved = false

            fun simulateProcessLine(line: List<Tile?>) {
                val nonNulls = line.filterNotNull()
                var i = 0
                while (i < nonNulls.size) {
                    if (i + 1 < nonNulls.size && nonNulls[i].value == nonNulls[i+1].value) {
                        gainedScore += nonNulls[i].value * 2
                        i += 2
                        moved = true
                    } else {
                        i++
                    }
                }
                if (nonNulls.size < line.size) {
                    val justShifted = line.dropLastWhile { it == null }.any { it == null }
                    if (justShifted) moved = true
                }
            }

            when (dir) {
                Direction.UP -> {
                    for (x in 0 until width) {
                        val col = (0 until height).map { y -> grid[x][y] }
                        simulateProcessLine(col)
                        // Also check if things shift because of empty spaces
                        if (col.filterNotNull().size < height && col.filterNotNull().isNotEmpty()) {
                            val firstNull = col.indexOfFirst { it == null }
                            val lastNonNull = col.indexOfLast { it != null }
                            if (firstNull in 0 until lastNonNull) moved = true
                        }
                    }
                }
                Direction.DOWN -> {
                    for (x in 0 until width) {
                        val col = (height - 1 downTo 0).map { y -> grid[x][y] }
                        simulateProcessLine(col)
                        if (col.filterNotNull().size < height && col.filterNotNull().isNotEmpty()) {
                            val firstNull = col.indexOfFirst { it == null }
                            val lastNonNull = col.indexOfLast { it != null }
                            if (firstNull in 0 until lastNonNull) moved = true
                        }
                    }
                }
                Direction.LEFT -> {
                    for (y in 0 until height) {
                        val row = (0 until width).map { x -> grid[x][y] }
                        simulateProcessLine(row)
                        if (row.filterNotNull().size < width && row.filterNotNull().isNotEmpty()) {
                            val firstNull = row.indexOfFirst { it == null }
                            val lastNonNull = row.indexOfLast { it != null }
                            if (firstNull in 0 until lastNonNull) moved = true
                        }
                    }
                }
                Direction.RIGHT -> {
                    for (y in 0 until height) {
                        val row = (width - 1 downTo 0).map { x -> grid[x][y] }
                        simulateProcessLine(row)
                        if (row.filterNotNull().size < width && row.filterNotNull().isNotEmpty()) {
                            val firstNull = row.indexOfFirst { it == null }
                            val lastNonNull = row.indexOfLast { it != null }
                            if (firstNull in 0 until lastNonNull) moved = true
                        }
                    }
                }
            }

            if (moved && gainedScore > maxGain) {
                maxGain = gainedScore
                bestDirection = dir
            } else if (moved && maxGain == -1) {
                maxGain = 0
                bestDirection = dir
            }
        }
        return bestDirection
    }
}
