package dev.kaitou.sample.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class AppServiceTest {

	@Test
	void greets() {
		assertEquals("hello", AppService.greeting());
	}

	@Test
	void rejectsEmptyNames() {
		assertThrows(IllegalArgumentException.class, () -> AppService.requireName(""));
	}

	@Test
	void uppercases() {
		assertTrue(AppService.upper("abc").equals("ABC"));
	}

	@Test
	@Disabled("work in progress")
	void plannedFeature() {
		AppService.notImplementedYet();
	}

	@Test
	void occasionallyFails() {
		if (System.getProperty("testament.demo.fail") != null) {
			assertEquals(4, 0, "demonstration failure");
		}
	}
}
