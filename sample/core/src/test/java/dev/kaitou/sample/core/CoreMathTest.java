package dev.kaitou.sample.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CoreMathTest {

	@Test
	void adds() {
		assertEquals(4, 2 + 2);
	}

	@Test
	void multiplies() {
		assertEquals(12, 3 * 4);
	}

	@Test
	void strings() {
		assertTrue("testament".startsWith("test"));
	}
}
