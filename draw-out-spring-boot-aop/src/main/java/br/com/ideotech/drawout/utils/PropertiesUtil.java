/**
 * Copyright 2019 Adauto Martins <adauto.martin@ideotech.com.br>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	private static final String PROFILE_CONFIG_ENV_KEY = "SPRING_PROFILES_ACTIVE";

	private static PropertiesUtil instance = null;

	private CompositeConfiguration config = new CompositeConfiguration();

	private PropertiesUtil() {
		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<FileBasedConfiguration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
				PropertiesConfiguration.class).configure(params.properties().setFileName("application.properties"));
		try {
			config.addConfiguration(builder.getConfiguration());
			String activeProfile = System.getenv(PROFILE_CONFIG_ENV_KEY);
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

	public static PropertiesUtil getInstance(){
		if (instance == null){
			instance = new PropertiesUtil();
		}
		return instance;
	}

	private String getProperty(String key) {
		String keyAsEnv = key.replaceAll("\\.", "_");
		String property = System.getenv(keyAsEnv.toUpperCase());
		if (property == null || property.isEmpty()) {
			property = System.getProperty(key);
		}
		if (property == null || property.isEmpty()) {
			property = config.getString(key);
		}
		return property;
	}

	public String getValue(String key) {
		return getProperty(key);
	}

	public Long getValueAsLong(String key) {
		try {
			return Long.parseLong(getProperty(key));
		} catch (NumberFormatException nfe) {
			return null;
		}

	}

	public Long getValueAsLong(String key, Long defaultValue) {
		Long property = getValueAsLong(key);
		if (property != null) {
			return property;
		} else {
			return defaultValue;
		}
	}
}
