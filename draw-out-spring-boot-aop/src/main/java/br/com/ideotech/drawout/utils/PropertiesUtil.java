package br.com.ideotech.drawout.utils;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class PropertiesUtil {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(PropertiesUtil.class);
	private static final String PROFILE_CONFIG_KEY = "spring.profiles.active";

	private CompositeConfiguration config = new CompositeConfiguration();

	public PropertiesUtil() {
		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<FileBasedConfiguration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
				PropertiesConfiguration.class).configure(params.properties().setFileName("application.properties"));
		try {
			config.addConfiguration(builder.getConfiguration());
			String activeProfile = System.getenv(PROFILE_CONFIG_KEY);
			if (activeProfile == null || activeProfile.isEmpty()) {
				activeProfile = System.getProperty(PROFILE_CONFIG_KEY);
			}
			if (activeProfile == null || activeProfile.isEmpty()) {
				activeProfile = config.getString(PROFILE_CONFIG_KEY);
			}
			// If it has spring profile defined
			if (activeProfile != null && !activeProfile.isEmpty()) {
				config.addConfiguration(
						(new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
								.configure(params.properties()
										.setFileName(String.format("application-%s.properties", activeProfile))))
												.getConfiguration());
			}
		} catch (ConfigurationException cex) {
			LOGGER.warn("Error during loading of application.properties.", cex);
		}
	}

	public String getValue(String key) {
		return config.getString(key);
	}
}
