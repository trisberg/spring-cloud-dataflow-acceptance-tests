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
package org.springframework.cloud.dataflow.acceptance.test.util;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mark Pollack
 * @author Christian Tzolov
 */
public class LogTestNameRule extends TestWatcher {

	private final static Logger log = LoggerFactory.getLogger("junit.logTestName");

	@Override
	protected void starting(Description description) {
		log.info("Starting Test {}", description.getDisplayName());
	}

	@Override
	protected void failed(Throwable e, Description description) {
		log.error("!!! Test {} Failed !!!", description.getDisplayName(), e);
	}

	@Override
	protected void succeeded(Description description) {
		log.info("Test {} Succeeded", description.getDisplayName());
	}

	@Override
	protected void finished(Description description) {
		log.info("Finished Test {}", description.getDisplayName());
	}
}
