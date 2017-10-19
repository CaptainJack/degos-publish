package ru.capjack.degos.publish;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

class DegosPublishPluginTest {
	@Test
	void simpleApply() {
		Project project = ProjectBuilder.builder().build();
		project.getPluginManager().apply(DegosPublishPlugin.class);
	}
}