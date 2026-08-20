plugins {
	`kotlin-dsl`
	`java-gradle-plugin`
	`maven-publish`
	id("com.gradle.plugin-publish") version "1.3.1"
}

group = "pl.kaitoudev"
version = "0.1.0"

repositories {
	mavenCentral()
}

dependencies {
	testImplementation(platform("org.junit:junit-bom:5.13.4"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testImplementation(kotlin("test"))
	testImplementation(gradleTestKit())
	testRuntimeOnly(files(tasks.pluginUnderTestMetadata))
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
	compilerOptions {
		jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
	}
}

gradlePlugin {
	website = "https://github.com/Kait0u/testament"
	vcsUrl = "https://github.com/Kait0u/testament.git"
	plugins {
		create("testament") {
			id = "pl.kaitoudev.testament"
			implementationClass = "pl.kaitoudev.testament.TestamentPlugin"
			displayName = "Testament"
			description = "Prints a consolidated test execution summary with failure details at the end of every build."
			tags = listOf("test", "testing", "summary", "report", "ci")
		}
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.register("printVersion") {
	doLast {
		println(project.version.toString())
	}
}
