package pl.kaitoudev.testament

import org.gradle.api.logging.Logging
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.tasks.testing.TestResult
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong

/**
 * Shared build service that accumulates test results across all modules of the build and
 * renders the final summary. Being a shared build service keeps the state scoped to a single
 * build invocation and makes it resolvable from both test listeners and the end-of-build
 * flow action, including with the configuration cache enabled.
 */
abstract class TestamentService : BuildService<BuildServiceParameters.None> {

	private class ModuleCounts {
		val passed = AtomicLong(0)
		val skipped = AtomicLong(0)
		val failed = AtomicLong(0)
	}

	private val modules = ConcurrentHashMap<String, ModuleCounts>()
	private val failures = ConcurrentHashMap<String, CopyOnWriteArrayList<TestamentReport.Failure>>()

	private val logger = Logging.getLogger(TestamentService::class.java)

	fun recordModule(module: String, result: TestResult) {
		val counts = modules.computeIfAbsent(module) { ModuleCounts() }
		counts.passed.set(result.successfulTestCount)
		counts.skipped.set(result.skippedTestCount)
		counts.failed.set(result.failedTestCount)

		logger.lifecycle(
			"Tests for '$module' finished: ${result.successfulTestCount} passed, " +
				"${result.skippedTestCount} skipped, ${result.failedTestCount} failed"
		)
	}

	fun recordFailure(module: String, className: String?, method: String?, reason: String) {
		failures.computeIfAbsent(module) { CopyOnWriteArrayList() }
			.add(TestamentReport.Failure(module, className ?: "<unknown>", method ?: "<unknown>", reason))
	}

	fun printSummary() {
		val report = TestamentReport.render(snapshotModules(), failures.values.flatten())
		if (report.isNotBlank()) {
			logger.lifecycle(report)
		}
	}

	private fun snapshotModules(): Map<String, TestamentReport.ModuleCounts> =
		modules.entries.associate { (name, counts) ->
			name to TestamentReport.ModuleCounts(counts.passed.get(), counts.skipped.get(), counts.failed.get())
		}
}
