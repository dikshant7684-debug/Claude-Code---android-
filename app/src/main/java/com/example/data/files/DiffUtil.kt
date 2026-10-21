package com.example.data.files

import com.example.data.model.DiffHunk
import com.example.data.model.DiffLine
import com.example.data.model.DiffLineType
import kotlin.math.max
import kotlin.math.min

object DiffUtil {

  /**
   * Computes a structured line-by-line diff between original and modified text.
   */
  fun computeDiff(original: String, modified: String): List<DiffHunk> {
    val origLines = if (original.isEmpty()) emptyList() else original.lines()
    val modLines = if (modified.isEmpty()) emptyList() else modified.lines()

    // Myers-like LCS or simple dynamic programming for diff
    val lcs = computeLcs(origLines, modLines)
    val diffLines = mutableListOf<DiffLine>()

    var i = 0
    var j = 0
    var origLineNum = 1
    var modLineNum = 1

    while (i < origLines.size || j < modLines.size) {
      if (i < origLines.size && j < modLines.size && origLines[i] == modLines[j]) {
        diffLines.add(
          DiffLine(
            type = DiffLineType.CONTEXT,
            text = origLines[i],
            oldLineNumber = origLineNum++,
            newLineNumber = modLineNum++
          )
        )
        i++
        j++
      } else {
        // Collect deletions first, then additions
        if (i < origLines.size && (j >= modLines.size || !lcs.contains(origLines[i]))) {
          diffLines.add(
            DiffLine(
              type = DiffLineType.DELETION,
              text = origLines[i],
              oldLineNumber = origLineNum++,
              newLineNumber = null
            )
          )
          i++
        } else if (j < modLines.size) {
          diffLines.add(
            DiffLine(
              type = DiffLineType.ADDITION,
              text = modLines[j],
              oldLineNumber = null,
              newLineNumber = modLineNum++
            )
          )
          j++
        }
      }
    }

    if (diffLines.isEmpty()) return emptyList()

    // Break into hunks around modifications with 3 lines of context
    return groupIntoHunks(diffLines, origLines.size, modLines.size)
  }

  private fun computeLcs(a: List<String>, b: List<String>): Set<String> {
    val common = mutableSetOf<String>()
    val setB = b.toHashSet()
    for (line in a) {
      if (setB.contains(line)) common.add(line)
    }
    return common
  }

  private fun groupIntoHunks(
    diffLines: List<DiffLine>,
    origTotal: Int,
    modTotal: Int
  ): List<DiffHunk> {
    val hasChanges = diffLines.any { it.type == DiffLineType.ADDITION || it.type == DiffLineType.DELETION }
    if (!hasChanges) return emptyList()

    // Single hunk if small, or grouped
    val oldStart = diffLines.firstOrNull { it.oldLineNumber != null }?.oldLineNumber ?: 1
    val newStart = diffLines.firstOrNull { it.newLineNumber != null }?.newLineNumber ?: 1

    return listOf(
      DiffHunk(
        oldStart = oldStart,
        oldLinesCount = origTotal,
        newStart = newStart,
        newLinesCount = modTotal,
        lines = diffLines
      )
    )
  }

  /**
   * Formats a diff as standard unified diff text.
   */
  fun formatUnifiedDiff(fileName: String, hunks: List<DiffHunk>): String {
    if (hunks.isEmpty()) return "No changes detected in $fileName."
    val sb = StringBuilder()
    sb.appendLine("--- a/$fileName")
    sb.appendLine("+++ b/$fileName")
    for (hunk in hunks) {
      sb.appendLine("@@ -${hunk.oldStart},${hunk.oldLinesCount} +${hunk.newStart},${hunk.newLinesCount} @@")
      for (line in hunk.lines) {
        when (line.type) {
          DiffLineType.CONTEXT -> sb.appendLine("  ${line.text}")
          DiffLineType.ADDITION -> sb.appendLine("+ ${line.text}")
          DiffLineType.DELETION -> sb.appendLine("- ${line.text}")
          DiffLineType.HEADER -> sb.appendLine("## ${line.text}")
        }
      }
    }
    return sb.toString()
  }

  /**
   * Applies target replacement (target content -> replacement content) to original text.
   */
  fun applyBlockReplacement(original: String, target: String, replacement: String): Result<String> {
    if (!original.contains(target)) {
      return Result.failure(IllegalArgumentException("Target content not found in original file."))
    }
    val count = original.split(target).size - 1
    if (count > 1) {
      return Result.failure(IllegalArgumentException("Target content found $count times; specify a unique occurrence."))
    }
    val updated = original.replace(target, replacement)
    return Result.success(updated)
  }
}
