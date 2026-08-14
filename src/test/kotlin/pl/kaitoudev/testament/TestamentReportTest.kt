package pl.kaitoudev.testament

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestamentReportTest {

	@Test
	fun `renders nothing when nothing was recorded`() {
		assertEquals("", TestamentReport.render(emptyMap(), emptyList()))
	}

	@Test
	fun `renders per-module totals and a grand total`() {
		val report = TestamentReport.render(
			mapOf(
				"smartcity-core" to TestamentReport.ModuleCounts(passed = 5, skipped = 1, failed = 0),
				"smartcity-api" to TestamentReport.ModuleCounts(passed = 3, skipped = 0, failed = 2),
			),
			emptyList(),
		)

		assertTrue(report.contains("TESTAMENT"))
		assertTrue(report.contains("smartcity-core"))
		assertTrue(report.contains("5 passed, 1 skipped, 0 failed"))
		assertTrue(report.contains("smartcity-api"))
		assertTrue(report.contains("3 passed, 0 skipped, 2 failed"))
		assertTrue(report.contains("8 passed, 1 skipped, 2 failed (11 tests)"))
		assertTrue(!report.contains("FAILURES"))
	}

	@Test
	fun `lists failures with reasons, sorted by module, class and method`() {
		val report = TestamentReport.render(
			mapOf("app" to TestamentReport.ModuleCounts(passed = 0, skipped = 0, failed = 2)),
			listOf(
				TestamentReport.Failure("app", "com.example.CalcTest", "divideByZero", "expected: <4> but was: <0>"),
				TestamentReport.Failure("app", "com.example.AlphaTest", "fails", "boom"),
			),
		)

		assertTrue(report.contains("FAILURES"))
		val alpha = report.indexOf("[app] com.example.AlphaTest.fails")
		val calc = report.indexOf("[app] com.example.CalcTest.divideByZero")
		assertTrue(alpha in 1 until calc, "failures must be sorted by class name")
		assertTrue(report.contains("reason: expected: <4> but was: <0>"))
	}

	@Test
	fun `aligns module names in the table`() {
		val report = TestamentReport.render(
			mapOf(
				"a" to TestamentReport.ModuleCounts(passed = 1, skipped = 0, failed = 0),
				"much-longer-module-name" to TestamentReport.ModuleCounts(passed = 2, skipped = 0, failed = 0),
			),
			emptyList(),
		)

		assertTrue(report.contains(" ${"a".padEnd(23)}  1 passed, 0 skipped, 0 failed"))
	}
}
