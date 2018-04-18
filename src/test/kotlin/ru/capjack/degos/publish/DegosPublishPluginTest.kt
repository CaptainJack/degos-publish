package ru.capjack.degos.publish

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

internal class DegosPublishPluginTest {
	@Test
	fun `Simple apply`() {
		val project = ProjectBuilder.builder().build()
		project.pluginManager.apply(DegosPublishPlugin::class.java)
	}
}