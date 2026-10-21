package com.example

import com.example.data.files.DiffUtil
import com.example.data.model.DiffLineType
import com.example.data.preferences.PreferencesManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testDiffComputation_additionsAndDeletions() {
    val original = "line1\nline2\nline3"
    val modified = "line1\nline2_modified\nline3\nline4"

    val hunks = DiffUtil.computeDiff(original, modified)
    assertFalse(hunks.isEmpty())

    val lines = hunks.flatMap { it.lines }
    assertTrue(lines.any { it.type == DiffLineType.DELETION && it.text == "line2" })
    assertTrue(lines.any { it.type == DiffLineType.ADDITION && it.text == "line2_modified" })
    assertTrue(lines.any { it.type == DiffLineType.ADDITION && it.text == "line4" })
    assertTrue(lines.any { it.type == DiffLineType.CONTEXT && it.text == "line1" })
  }

  @Test
  fun testApplyBlockReplacement_success() {
    val original = "fun calculate(): Int {\n    return 10\n}"
    val target = "return 10"
    val replacement = "return 42"

    val result = DiffUtil.applyBlockReplacement(original, target, replacement)
    assertTrue(result.isSuccess)
    val updated = result.getOrThrow()
    assertTrue(updated.contains("return 42"))
    assertFalse(updated.contains("return 10"))
  }

  @Test
  fun testApplyBlockReplacement_targetNotFoundFails() {
    val original = "val a = 1"
    val result = DiffUtil.applyBlockReplacement(original, "val b = 2", "val c = 3")
    assertTrue(result.isFailure)
  }

  @Test
  fun testApiKeyRedaction() {
    assertEquals("(Not set)", PreferencesManager.redactApiKey(""))
    assertEquals("********", PreferencesManager.redactApiKey("12345678"))
    assertEquals("sk-a...890z", PreferencesManager.redactApiKey("sk-abcdefgh1234567890z"))
  }
}
