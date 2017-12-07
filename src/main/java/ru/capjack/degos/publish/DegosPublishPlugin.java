package ru.capjack.degos.publish;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

public class DegosPublishPlugin implements Plugin<Project> {
	private static final String PROPERTY_USERNAME = "capjackRepositoryUsername";
	private static final String PROPERTY_PASSWORD = "capjackRepositoryPassword";
	
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(MavenPublishPlugin.class);
		project.getExtensions().create(DegosPublishExtension.class, DegosPublishExtension.NAME, DefaultDegosPublishExtension.class,
			project.findProperty(PROPERTY_USERNAME),
			project.findProperty(PROPERTY_PASSWORD)
		);
		project.afterEvaluate(this::applyAfterEvaluate);
	}
	
	private void applyAfterEvaluate(Project project) {
		DegosPublishExtension extension = project.getExtensions().getByType(DegosPublishExtension.class);
		
		RepositoryConfig config = defineRepositoryConfig(
			loadRepositoryConfigs(),
			extension.isAsPrivate(),
			defineIsReleaseByVersion(project.getVersion().toString())
		);
		
		addRepository(
			project.getExtensions().getByType(PublishingExtension.class).getRepositories(),
			config,
			extension.getUsername(),
			extension.getPassword()
		);
	}
	
	private RepositoryConfig defineRepositoryConfig(RepositoryConfig[] list, boolean secret, boolean release) {
		for (RepositoryConfig config : list) {
			if (config.secret == secret && config.releases == release) {
				return config;
			}
		}
		throw new IllegalStateException(String.format("Failed to define repository for secret is %b and release is %b", secret, release));
	}
	
	private RepositoryConfig[] loadRepositoryConfigs() {
		try {
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			return mapper.readValue(getClass().getClassLoader().getResource("repositories.yml"), RepositoryConfig[].class);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to load repository configurations", e);
		}
	}
	
	private boolean defineIsReleaseByVersion(String version) {
		return !version.contains("SNAPSHOT");
	}
	
	private void addRepository(RepositoryHandler repositories, RepositoryConfig config, String username, String password) {
		repositories.maven(repository -> {
			repository.setName(config.name);
			repository.setUrl(config.url);
			
			repository.credentials(credentials -> {
				credentials.setUsername(username);
				credentials.setPassword(password);
			});
		});
	}
	
	static private class RepositoryConfig {
		public String  name;
		public String  url;
		public boolean secret;
		public boolean releases;
	}
}
