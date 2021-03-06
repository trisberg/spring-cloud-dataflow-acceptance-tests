/*
 * Copyright 2017-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.dataflow.acceptance.test;

import org.hamcrest.Matchers;
import org.junit.Test;

import org.springframework.cloud.dataflow.acceptance.test.util.StreamDefinition;
import org.springframework.cloud.dataflow.rest.resource.StreamDefinitionResource;
import org.springframework.cloud.dataflow.rest.resource.about.AboutResource;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;

/**
 * Executes acceptance tests for the ticktock demo.
 * @author Glenn Renfro
 * @author Thomas Risberg
 * @author Vinicius Carvalho
 * @author Ilayaperumal Gopinathan
 * @author Christian Tzolov
 */

public class TickTockTests extends AbstractStreamTests {

	@Test
	public void tickTockTests() {
		StreamDefinition stream = StreamDefinition.builder("TICKTOCK")
				.definition("time | log")
				.addProperty("app.log.log.expression", "'TICKTOCK - TIMESTAMP: '.concat(payload)")
				.build();

		deployStream(stream);
		assertTrue("Source not started", waitForLogEntry(stream.getApplication("time"), "Started TimeSource"));
		assertTrue("Sink not started", waitForLogEntry(stream.getApplication("log"), "Started LogSink"));
		assertTrue("No output found", waitForLogEntry(stream.getApplication("log"), "TICKTOCK - TIMESTAMP:"));
	}

	@Test
	public void tickTockUpdateRollbackTests() {
		AboutResource aboutResource = this.dataFlowOperations.aboutOperation().get();
		String implementation = aboutResource.getVersionInfo().getImplementation().getName();
		if (!implementation.equals("spring-cloud-dataflow-server")) {
			assumeThat("Skipping test", "true",
					Matchers.equalToIgnoringCase(String.valueOf(aboutResource.getFeatureInfo().isStreamsEnabled())));
		}
		this.dataFlowOperations.streamOperations().destroyAll();
		StreamDefinition stream = StreamDefinition.builder("TICKTOCK")
				.definition("time | log")
				.addProperty("app.log.log.expression", "'TICKTOCK - TIMESTAMP: '.concat(payload)")
				.build();

		deployStream(stream);
		assertTrue("Source not started", waitForLogEntry(stream.getApplication("time"), "Started TimeSource"));
		assertTrue("Sink not started", waitForLogEntry(stream.getApplication("log"), "Started LogSink"));
		assertTrue("No output found", waitForLogEntry(stream.getApplication("log"), "TICKTOCK - TIMESTAMP:"));
		StreamDefinitionResource updatedStream = updateStream(stream,
				"app.log.log.expression='TICKTOCK Updated - TIMESTAMP: '.concat(payload)", null);
		assertTrue(updatedStream.getDslText().contains("--log.expression=\"'TICKTOCK Updated - TIMESTAMP: '.concat(payload)\""));
		assertTrue("Sink not started", waitForLogEntry(stream.getApplication("log"), "Started LogSink"));
		assertTrue("No output found", waitForLogEntry(stream.getApplication("log"), "TICKTOCK Updated - TIMESTAMP:"));
		rollbackStream(stream);
		assertTrue("No output found", waitForLogEntry(stream.getApplication("log"), "TICKTOCK - TIMESTAMP:"));
	}

	@Test
	public void tickTockTestsFromConfigServer() {
		String platformType = System.getProperty("PLATFORM_TYPE", "");
		String skipCloudConfig = System.getProperty("SKIP_CLOUD_CONFIG", "false");
		assumeThat("Skipping test", "cloudfoundry", Matchers.equalToIgnoringCase(platformType));
		assumeThat("Skipping test", "false", Matchers.equalToIgnoringCase(skipCloudConfig));
		StreamDefinition stream = StreamDefinition.builder("TICKTOCK-config-server")
				.definition("time | log")
				.addProperty("app.log.spring.profiles.active", "test")
				.addProperty("deployer.log.cloudfoundry.services", "cloud-config-server")
				.addProperty("app.log.spring.cloud.config.name", "MY_CONFIG_TICKTOCK_LOG_NAME")
				.build();
		deployStream(stream);
		assertTrue("Source not started", waitForLogEntry(stream.getApplication("time"), "Started TimeSource"));
		assertTrue("Sink not started", waitForLogEntry(stream.getApplication("log"), "Started LogSink"));
		assertTrue("No output found", waitForLogEntry(stream.getApplication("log"), "TICKTOCK CLOUD CONFIG - TIMESTAMP:"));
	}
}
