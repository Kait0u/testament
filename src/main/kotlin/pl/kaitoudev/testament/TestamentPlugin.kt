package pl.kaitoudev.testament

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.flow.FlowScope
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult
import javax.inject.Inject

/**
 * Watches every [Test] task in the build, records results into a shared [TestamentService],
 * and schedules a [TestamentFlowAction] that prints the consolidated summary once the build
 * has finished.
 */
abstract class TestamentPlugin @Inject constructor(
	private val flowScope: FlowScope,
) : Plugin<Project> {

	override fun apply(project: Project) {
		val testament = project.gradle.sharedServices
			.registerIfAbsent("testament", TestamentService::class.java) {}

		project.allprojects {
			val module = this
			tasks.withType(Test::class.java).configureEach {
				val moduleKey = if (name == "test") module.name else "${module.name}:$name"
				addTestListener(TestamentListener(moduleKey, testament))
			}
		}

		flowScope.always(TestamentFlowAction::class.java) {
			parameters.testament.set(testament)
		}
	}
}

/**
 * [TestListener] is the replacement for the {@code AbstractTestTask.beforeSuite/afterSuite/
 * beforeTest/afterTest(Closure)} methods, which are deprecated for removal in Gradle 10.
 * Only serializable-friendly values (strings and a build service provider) are captured, so
 * the listener works with the configuration cache.
 */
private class TestamentListener(
	private val module: String,
	private val testament: Provider<TestamentService>,
) : TestListener {

	override fun beforeSuite(descriptor: TestDescriptor) = Unit

	override fun afterSuite(descriptor: TestDescriptor, result: TestResult) {
		if (descriptor.parent == null) {
			testament.get().recordModule(module, result)
		}
	}

	override fun beforeTest(descriptor: TestDescriptor) = Unit

	override fun afterTest(descriptor: TestDescriptor, result: TestResult) {
		if (result.resultType == TestResult.ResultType.FAILURE) {
			testament.get().recordFailure(
				module,
				descriptor.className,
				descriptor.name?.removeSuffix("()"),
				failureReason(result),
			)
		}
	}

	private fun failureReason(result: TestResult): String {
		val failure = result.failures.firstOrNull()
		val details = failure?.details
		val throwable = result.exception ?: failure?.rawFailure

		val reason = when {
			details?.isAssertionFailure == true && !details.expected.isNullOrBlank() && !details.actual.isNullOrBlank() ->
				"expected: <${details.expected}> but was: <${details.actual}>"
			!throwable?.message.isNullOrBlank() -> throwable.message.orEmpty()
			!details?.message.isNullOrBlank() -> details.message.orEmpty()
			throwable != null -> throwable.toString()
			else -> "unknown failure"
		}

		return reason.lineSequence().firstOrNull()?.trim()?.take(200).orEmpty()
	}
}
