package dev.kaitou.sample.app;

final class AppService {

	private AppService() {
	}

	static String greeting() {
		return "hello";
	}

	static void requireName(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("name must not be blank");
		}
	}

	static String upper(String value) {
		return value.toUpperCase();
	}

	static void notImplementedYet() {
		throw new UnsupportedOperationException("coming soon");
	}
}
