package ru.capjack.degos.publish

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Files
import java.util.stream.Stream

class IntegrationTest {
	
	@Test
	internal fun `Setup credentials from properties`() {
		val output = run(
			listOf(
				"plugins {id 'ru.capjack.degos.publish'}",
				"println '!' + degosPublish.username",
				"println '!' + degosPublish.password"
			),
			"-PdegosPublish.username=username123", "-PdegosPublish.password=password123"
		).output
		
		assertTrue(output.contains("!username123") && output.contains("!password123"))
	}
	
	@ParameterizedTest
	@MethodSource("configureAndCheckExistsPublishingRepositoryArgumentsProvider")
	internal fun `Configure and check exists publishing repository`(version: String, private: Boolean, repositoryName: String) {
		val build = listOf(
			"plugins {id 'ru.capjack.degos.publish'}",
			"version '$version'",
			"degosPublish.private = $private",
			"task printPublishingRepositories {",
			" doLast {",
			"  publishing.repositories.forEach { println '!' + it.name }",
			" }",
			"}"
		)
		
		val output = run(build, "printPublishingRepositories").output
		
		assertTrue(output.contains("!$repositoryName"))
	}
	
	private fun run(build: List<String>, vararg arguments: String): BuildResult {
		val projectDir = Files.createTempDirectory("test").toFile()
		val buildFile = projectDir.resolve("build.gradle")
		
		try {
			buildFile.writeText(build.joinToString("\n"))
			
			return GradleRunner.create()
				.withPluginClasspath()
				.withProjectDir(projectDir)
				.withArguments(*arguments)
				.build()
		}
		finally {
			projectDir.deleteRecursively()
		}
	}
	
	companion object {
		@JvmStatic
		fun configureAndCheckExistsPublishingRepositoryArgumentsProvider() = Stream.of(
			Arguments.of("1.0.0", false, "CapjackPublicReleases"),
			Arguments.of("1.0.0-SNAPSHOT", false, "CapjackPublicSnapshot"),
			Arguments.of("1.0.0", true, "CapjackPrivateReleases"),
			Arguments.of("1.0.0-SNAPSHOT", true, "CapjackPrivateSnapshot")
		)!!
	}
}
