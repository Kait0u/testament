package pl.kaitoudev.testament

import org.gradle.api.flow.FlowAction
import org.gradle.api.flow.FlowParameters
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference

/**
 * Runs once the build has completed (successfully or not) and prints the accumulated
 * Testament summary as the last thing in the build output.
 */
abstract class TestamentFlowAction : FlowAction<TestamentFlowAction.Params> {

	interface Params : FlowParameters {
		@get:ServiceReference
		val testament: Property<TestamentService>
	}

	override fun execute(parameters: Params) {
		parameters.testament.get().printSummary()
	}
}
