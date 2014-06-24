/***************************************************************************
 * Copyright 2014 Kieker Project (http://kieker-monitoring.net)
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
 ***************************************************************************/
package teetime.variant.methodcall.examples.experiment03;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import teetime.util.ConstructorClosure;
import teetime.util.StatisticsUtil;
import teetime.util.StopWatch;
import teetime.variant.explicitScheduling.examples.throughput.TimestampObject;
import test.PerformanceTest;

/**
 * @author Christian Wulf
 * 
 * @since 1.10
 */
public class MethodCallThoughputTimestampAnalysis3Test extends PerformanceTest {

	// 500 times faster than our new framework
	// TODO check why

	@Test
	public void testWithManyObjects() {
		System.out.println("Testing teetime (mc) with NUM_OBJECTS_TO_CREATE=" + NUM_OBJECTS_TO_CREATE + ", NUM_NOOP_FILTERS="
				+ NUM_NOOP_FILTERS + "...");
		final StopWatch stopWatch = new StopWatch();
		final List<TimestampObject> timestampObjects = new ArrayList<TimestampObject>(NUM_OBJECTS_TO_CREATE);

		final MethodCallThroughputAnalysis3 analysis = new MethodCallThroughputAnalysis3();
		analysis.setNumNoopFilters(NUM_NOOP_FILTERS);
		analysis.setTimestampObjects(timestampObjects);
		analysis.setInput(NUM_OBJECTS_TO_CREATE, new ConstructorClosure<TimestampObject>() {
			@Override
			public TimestampObject create() {
				return new TimestampObject();
			}
		});
		analysis.init();

		stopWatch.start();
		try {
			analysis.start();
		} finally {
			stopWatch.end();
		}

		StatisticsUtil.printStatistics(stopWatch.getDurationInNs(), timestampObjects);
	}

}
