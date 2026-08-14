package pl.kaitoudev.testament

/**
 * Pure rendering of the Testament summary. Kept free of Gradle types so it can be unit-tested.
 */
object TestamentReport {

	data class ModuleCounts(
		val passed: Long,
		val skipped: Long,
		val failed: Long,
	)

	data class Failure(
		val module: String,
		val className: String,
		val method: String,
		val reason: String,
	)

	fun render(modules: Map<String, ModuleCounts>, failures: List<Failure>): String {
		if (modules.isEmpty() && failures.isEmpty()) {
			return ""
		}

		val moduleNames = modules.keys.sorted()
		val nameWidth = (moduleNames.maxOfOrNull { it.length } ?: 0).coerceAtLeast(5)

		val totalPassed = modules.values.sumOf { it.passed }
		val totalSkipped = modules.values.sumOf { it.skipped }
		val totalFailed = modules.values.sumOf { it.failed }
		val total = totalPassed + totalSkipped + totalFailed

		val sb = StringBuilder()
		sb.appendLine()
		sb.appendLine("==========================================================================")
		sb.appendLine(" TESTAMENT")
		sb.appendLine("==========================================================================")
		for (name in moduleNames) {
			val counts = modules.getValue(name)
			sb.appendLine(
				" ${name.padEnd(nameWidth)}  ${counts.passed} passed, " +
					"${counts.skipped} skipped, ${counts.failed} failed"
			)
		}
		sb.appendLine("--------------------------------------------------------------------------")
		sb.appendLine(
			" ${"TOTAL".padEnd(nameWidth)}  $totalPassed passed, $totalSkipped skipped, $totalFailed failed ($total tests)"
		)
		sb.appendLine("==========================================================================")

		if (failures.isNotEmpty()) {
			sb.appendLine()
			sb.appendLine(" FAILURES")
			sb.appendLine("--------------------------------------------------------------------------")
			for (failure in failures.sortedWith(compareBy({ it.module }, { it.className }, { it.method }))) {
				sb.appendLine(" [${failure.module}] ${failure.className}.${failure.method}")
				sb.appendLine("     reason: ${failure.reason}")
			}
			sb.appendLine("--------------------------------------------------------------------------")
		}
		return sb.toString()
	}
}
