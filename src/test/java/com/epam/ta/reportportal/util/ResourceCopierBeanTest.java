/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.util;

import com.google.common.base.Charsets;
import com.google.common.base.StandardSystemProperty;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Resource copier bean test
 *
 * @author Andrei Varabyeu
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ResourceCopierBeanTest.TestConfig.class })
public class ResourceCopierBeanTest {
	private static final String RANDOM_NAME = RandomStringUtils.randomAlphabetic(5);

	private static final String RESOURCE_TO_BE_COPIED = "classpath:logback-test.xml";

	@Autowired
	private ResourceLoader resourceLoader;

	@Test
	void testResourceCopierBean() throws IOException {
		File createdFile = new File(StandardSystemProperty.JAVA_IO_TMPDIR.value(), RANDOM_NAME);
		Resource resource = resourceLoader.getResource(RESOURCE_TO_BE_COPIED);
		String copied = Files.asCharSource(createdFile, Charsets.UTF_8).read();
		String fromResource = CharStreams.toString(new InputStreamReader(resource.getInputStream(), Charsets.UTF_8));
		assertEquals(fromResource, copied, "Copied file is not equal to resource source");
	}

	@AfterEach
	void deleteCreatedFile() throws IOException {
		FileUtils.deleteQuietly(new File(StandardSystemProperty.JAVA_IO_TMPDIR.value(), RANDOM_NAME));
	}

	@Configuration
	public static class TestConfig {

		@Bean
		ResourceCopierBean resourceCopier() {
			ResourceCopierBean rcb = new ResourceCopierBean();
			rcb.setSource(RESOURCE_TO_BE_COPIED);
			rcb.setTempDirDestination(RANDOM_NAME);
			return rcb;
		}
	}
}