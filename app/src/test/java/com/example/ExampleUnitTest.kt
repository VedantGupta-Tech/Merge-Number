package com.example

import com.example.game.GameEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testGameEngineInitialization() {
    val engine = GameEngine(5, 5)
    engine.reset()
    assertEquals(0, engine.score)
    assertEquals(0, engine.moveCount)
    var tileCount = 0
    for (x in 0 until 5) {
      for (y in 0 until 5) {
        if (engine.grid[x][y] != null) {
          tileCount++
        }
      }
    }
    assertEquals(2, tileCount)
  }

  @Test
  fun testGameEngineUndo() {
    val engine = GameEngine(5, 5)
    engine.reset()
    assertFalse(engine.canUndo())
    engine.move(GameEngine.Direction.DOWN)
    assertTrue(engine.canUndo())
    assertTrue(engine.undo())
    assertEquals(0, engine.score)
    assertFalse(engine.canUndo())
  }
}
