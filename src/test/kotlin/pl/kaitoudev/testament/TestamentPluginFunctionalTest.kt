package pl.kaitoudev.testament

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestamentPluginFunctionalTest {

	@TempDir
	lateinit var projectDir: Path

	@BeforeEach
	fun setUp() {
		writeSettings()
	}

	@Test
	fun `prints a consolidated summary across modules and is free of deprecation warnings`() {
		writeModule("core", listOf(PassingTest("core.PassingCoreTest")))
		writeModule("app", listOf(PassingTest("app.PassingAppTest"), DisabledTest("app.SkippedAppTest")))

		val result = runner("test", "--warning-mode", "all").build()

		assertEquals(TaskOutcome.SUCCESS, result.task(":core:test")?.outcome)
		val output = result.output
		assertContains(output, "TESTAMENT")
		assertContains(output, " app    2 passed, 1 skipped, 0 failed")
		assertContains(output, " core   2 passed, 0 skipped, 0 failed")
		assertContains(output, "TOTAL")
		assertContains(output, "4 passed, 1 skipped, 0 failed (5 tests)")
		assertFalse(output.contains("FAILURES"))
		assertFalse(output.contains("has been deprecated"), "plugin must not trigger deprecation warnings")
	}

	@Test
	fun `lists failures with their reasons and fails the build`() {
		writeModule("core", listOf(PassingTest("core.PassingCoreTest")))
		writeModule("app", listOf(FailingTest("app.FailingAppTest")))

		val result = runner("test").buildAndFail()

		assertEquals(TaskOutcome.FAILED, result.task(":app:test")?.outcome)
		val output = result.output
		assertContains(output, "TESTAMENT")
		assertContains(output, " app    0 passed, 0 skipped, 1 failed")
		assertContains(output, "FAILURES")
		assertContains(output, "[app] app.FailingAppTest.failsWithAssertion")
		assertContains(output, "reason: expected: <4> but was: <0>")
	}

	@Test
	fun `prints nothing when no tests ran`() {
		writeModule("core", listOf(PassingTest("core.PassingCoreTest")))
		writeModule("app", listOf(PassingTest("app.PassingAppTest")))

		val result = runner("help").build()

		assertFalse(result.output.contains("TESTAMENT"))
	}

	private fun runner(vararg args: String): GradleRunner =
		GradleRunner.create()
			.withProjectDir(projectDir.toFile())
			.withArguments(*args)
			.withPluginClasspath()

	private fun writeSettings() {
		Files.writeString(
			projectDir.resolve("settings.gradle"),
			"""
			rootProject.name = "testament-func"
			include("core", "app")
			""".trimIndent() + "\n",
		)
		Files.writeString(
			projectDir.resolve("build.gradle"),
			"""
			plugins {
				id "pl.kaitoudev.testament"
			}
			allprojects {
				apply plugin: "java"
				repositories { mavenCentral() }
				dependencies {
					testImplementation "org.junit.jupiter:junit-jupiter:5.13.4"
					testRuntimeOnly "org.junit.platform:junit-platform-launcher"
				}
				tasks.withType(Test) { useJUnitPlatform() }
			}
			""".trimIndent() + "\n",
		)
	}

	private fun writeModule(name: String, tests: List<TestSource>) {
		val moduleDir = Files.createDirectories(projectDir.resolve(name))
		Files.writeString(moduleDir.resolve("build.gradle"), "group = \"test\"\n")
		for (test in tests) {
			val packageDir = test.className.substringBeforeLast('.')
			val dir = Files.createDirectories(
				moduleDir.resolve("src/test/java").resolve(packageDir.replace('.', '/'))
			)
			Files.writeString(dir.resolve("${test.className.substringAfterLast('.')}.java"), test.code)
		}
	}

	private interface TestSource {
		val className: String
		val code: String
	}

	private class PassingTest(override val className: String) : TestSource {
		override val code: String
			get() = """
				package ${className.substringBeforeLast('.')};

				import org.junit.jupiter.api.Test;
				import static org.junit.jupiter.api.Assertions.assertEquals;
				import static org.junit.jupiter.api.Assertions.assertTrue;

				public class ${className.substringAfterLast('.')} {
					@Test void first() { assertEquals(4, 2 + 2); }
					@Test void second() { assertTrue("x".startsWith("x")); }
				}
			""".trimIndent() + "\n"
	}

	private class DisabledTest(override val className: String) : TestSource {
		override val code: String
			get() = """
				package ${className.substringBeforeLast('.')};

				import org.junit.jupiter.api.Disabled;
				import org.junit.jupiter.api.Test;

				public class ${className.substringAfterLast('.')} {
					@Test @Disabled("not yet") void pending() { }
				}
			""".trimIndent() + "\n"
	}

	private class FailingTest(override val className: String) : TestSource {
		override val code: String
			get() = """
				package ${className.substringBeforeLast('.')};

				import org.junit.jupiter.api.Test;
				import static org.junit.jupiter.api.Assertions.assertEquals;

				public class ${className.substringAfterLast('.')} {
					@Test void failsWithAssertion() { assertEquals(4, 0); }
				}
			""".trimIndent() + "\n"
	}
}
