package ru.capjack.degos.publish

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.the

open class DegosPublishPlugin : Plugin<Project> {
	companion object {
		private const val PROPERTY_USERNAME = "capjack.artifactory.username"
		private const val PROPERTY_PASSWORD = "capjack.artifactory.password"
	}
	
	override fun apply(project: Project) {
		project.plugins.apply(MavenPublishPlugin::class.java)
		
		project.extensions.create("degosPublish", DegosPublishExtension::class.java).apply {
			username = project.findProperty(PROPERTY_USERNAME) as String?
			password = project.findProperty(PROPERTY_PASSWORD) as String?
			publication = project.name.split(' ', '_', '-', '.').joinToString("") { it.capitalize() }
		}
		
		if (project.plugins.hasPlugin("nebula.release")) {
			project.tasks["postRelease"].finalizedBy("publish")
		}
		
		project.afterEvaluate(::applyAfterEvaluate)
	}
	
	private fun applyAfterEvaluate(project: Project) {
		val degosPublishExtension = project.extensions.getByType(DegosPublishExtension::class.java)
		
		val config = defineRepositoryConfig(
			loadRepositoryConfigs(),
			degosPublishExtension.private,
			defineIsReleaseByVersion(project.version.toString())
		)
		
		project.configure<PublishingExtension> {
			
			repositories.maven(config.url) {
				name = config.name
				credentials {
					username = degosPublishExtension.username
					password = degosPublishExtension.password
				}
			}
			
			degosPublishExtension.publication?.also { name ->
				val hasJava = project.plugins.hasPlugin(JavaPlugin::class.java)
				
				val component = if (hasJava) project.components["java"] else null
				
				val sources = if (hasJava && isPublicationSource(degosPublishExtension.publicationSources, config.releases))
					project.tasks.create("sourceJar", Jar::class.java) {
						dependsOn(project.tasks["classes"])
						from(project.the<JavaPluginConvention>().sourceSets["main"].allSource)
						classifier = "sources"
						extension = "jar"
						group = "build"
					}
				else null
				
				(publications) {
					name(MavenPublication::class) {
						if (component != null) from(component)
						if (sources != null) artifact(sources)
					}
				}
			}
		}
	}
	
	private fun isPublicationSource(publicationSource: DegosPublishExtension.PublicationSource, release: Boolean): Boolean {
		return when (publicationSource) {
			DegosPublishExtension.PublicationSource.NEVER   -> false
			DegosPublishExtension.PublicationSource.ALWAYS  -> true
			DegosPublishExtension.PublicationSource.RELEASE -> release
		}
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
	
	private class RepositoryConfig {
		var name: String = "undefined"
		var url: String = "undefined"
		var private: Boolean = false
		var releases: Boolean = false
	}
}
