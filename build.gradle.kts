import nebula.plugin.bintray.NebulaBintrayPublishingPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "ru.capjack.degos"

plugins {
	kotlin("jvm") version "1.2.31"
	`kotlin-dsl`
	`java-gradle-plugin`
	`maven-publish`
	id("nebula.release") version "6.0.0"
}

repositories {
	maven("http://artifactory.capjack.ru/public")
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.2")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.9.2")
	
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.1.0")
	testImplementation("org.junit.jupiter:junit-jupiter-params:5.0.1")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.1.0")
}

tasks.withType<KotlinCompile> {
	kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks["postRelease"].finalizedBy("publish")

gradlePlugin {
	(plugins) {
		"DegosPublish" {
			id = "ru.capjack.degos-publish"
			implementationClass = "ru.capjack.degos.publish.DegosPublishPlugin"
		}
	}
}

publishing {
	repositories {
		val url = if (Regex("""^\d+\.\d+\.\d+$""").matches(version.toString()))
			"http://artifactory.capjack.ru/public-releases"
		else
			"http://artifactory.capjack.ru/public-snapshots"
		
		maven(url) {
			credentials {
				username = project.property("capjack.artifactory.username") as String
				password = project.property("capjack.artifactory.password") as String
			}
		}
	}
}
