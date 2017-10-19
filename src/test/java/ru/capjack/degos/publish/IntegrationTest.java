package ru.capjack.degos.publish;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntegrationTest {
	
	static Stream<Arguments> configureAndCheckExistsPublishingRepositoryArgumentsProvider() {
		return Stream.of(
			Arguments.of("1.0.0", false, "CapjackPublicReleases"),
			Arguments.of("1.0.0-SNAPSHOT", false, "CapjackPublicSnapshot"),
			Arguments.of("1.0.0", true, "CapjackPrivateReleases"),
			Arguments.of("1.0.0-SNAPSHOT", true, "CapjackPrivateSnapshot")
		);
	}
	
	
	@Test
	void setupCreditionalsFromPropertiest() throws IOException {
		String output =
			run(
				Arrays.asList(
					"plugins {id 'capjack.degos-publish'}",
					"println degosPublish.username",
					"println degosPublish.password"
				),
				"-PcapjackRepositoryUsername=username123", "-PcapjackRepositoryPassword=password123"
			).getOutput();
		
		assertTrue(output.contains("username123"));
		assertTrue(output.contains("password123"));
	}
	
	@ParameterizedTest
	@MethodSource("configureAndCheckExistsPublishingRepositoryArgumentsProvider")
	void configureAndCheckExistsPublishingRepository(String version, boolean asPrivate, String repositoryName) throws IOException {
		List<String> build = Arrays.asList(
			"plugins {id 'capjack.degos-publish'}",
			"version '" + version + "'",
			"degosPublish.asPrivate = " + asPrivate,
			"task printPublishingRepositories {",
			" doLast {",
			"  publishing.repositories.forEach { println it.name }",
			" }",
			"}"
		);
		
		String output = run(build, "printPublishingRepositories").getOutput();
		
		assertTrue(output.contains(repositoryName));
	}
	
	private BuildResult run(List<String> build, String... arguments) throws IOException {
		Path projectDir = Files.createTempDirectory("test");
		Path buildFile = projectDir.resolve("build.gradle");
		
		try {
			Files.write(buildFile, build);
			
			return GradleRunner.create()
				.withPluginClasspath()
				.withProjectDir(projectDir.toFile())
				.withArguments(arguments)
				.build();
		}
		finally {
			//noinspection ResultOfMethodCallIgnored
			Files.walk(projectDir)
				.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
		}
	}
}
