package ru.capjack.degos.publish

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.maven

open class DegosPublishPlugin : Plugin<Project> {
	companion object {
		private const val PROPERTY_USERNAME = "capjack.artifactory.username"
		private const val PROPERTY_PASSWORD = "capjack.artifactory.password"
	}
	
	override fun apply(project: Project) {
		project.plugins.apply(MavenPublishPlugin::class.java)
		
		project.extensions.create(
			DegosPublishExtension::class.java, DegosPublishExtension.NAME, DegosPublishExtensionImpl::class.java,
			project.findProperty(PROPERTY_USERNAME) as String? ?: "",
			project.findProperty(PROPERTY_PASSWORD) as String? ?: ""
		)
		
		if (project.plugins.hasPlugin("nebula.release")) {
			project.tasks["postRelease"].dependsOn("publish")
		}
		
		project.afterEvaluate(::applyAfterEvaluate)
	}
	
	private fun applyAfterEvaluate(project: Project) {
		val extension = project.extensions.getByType(DegosPublishExtension::class.java)
		
		val config = defineRepositoryConfig(
			loadRepositoryConfigs(),
			extension.private,
			defineIsReleaseByVersion(project.version.toString())
		)
		
		addRepository(
			project.extensions.getByType(PublishingExtension::class.java).repositories,
			config,
			extension.username,
			extension.password
		)
	}
	
	private fun defineRepositoryConfig(list: Array<RepositoryConfig>, private: Boolean, release: Boolean): RepositoryConfig {
		for (config in list) {
			if (config.private == private && config.releases == release) {
				return config
			}
		}
		throw IllegalStateException(String.format("Failed to define repository for private is %b and release is %b", private, release))
	}
	
	private fun loadRepositoryConfigs(): Array<RepositoryConfig> {
		try {
			val mapper = ObjectMapper(YAMLFactory())
			return mapper.readValue(javaClass.classLoader.getResource("repositories.yml"), Array<RepositoryConfig>::class.java)
		}
		catch (e: Exception) {
			throw RuntimeException("Failed to load repository configurations", e)
		}
	}
	
	private fun defineIsReleaseByVersion(version: String): Boolean {
		return Regex("""^\d+\.\d+\.\d+$""").matches(version)
	}
	
	private fun addRepository(repositories: RepositoryHandler, config: RepositoryConfig, username: String, password: String) {
		repositories.maven(config.url) {
			name = config.name
			credentials {
				this.username = username
				this.password = password
			}
		}
	}
	
	private class RepositoryConfig {
		var name: String = "undefined"
		var url: String = "undefined"
		var private: Boolean = false
		var releases: Boolean = false
	}
}
