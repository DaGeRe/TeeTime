/**
 * Copyright (C) 2015 Christian Wulf, Nelson Tavares de Sousa (http://teetime.sourceforge.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package teetime.framework;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import teetime.framework.pipe.IPipe;
import teetime.stage.CountingMapMerger;
import teetime.stage.InitialElementProducer;
import teetime.stage.basic.distributor.Distributor;
import teetime.stage.basic.distributor.RoundRobinStrategy2;
import teetime.stage.basic.merger.Merger;
import teetime.stage.io.File2SeqOfWords;
import teetime.stage.string.WordCounter;
import teetime.stage.util.CountingMap;

public class TraversorTest {

	private final Traversor traversor = new Traversor(new IntraStageCollector());

	@Test
	public void traverse() {
		TestConfiguration tc = new TestConfiguration();
		traversor.traverse(tc.init);
		Set<Stage> comparingSet = new HashSet<Stage>();
		comparingSet.add(tc.init);
		comparingSet.add(tc.f2b);
		comparingSet.add(tc.distributor);
		assertTrue(comparingSet.equals(traversor.getVisitedStage()));
	}

	// WordCounterConfiguration
	private class TestConfiguration extends AnalysisConfiguration {

		public final CountingMapMerger<String> result = new CountingMapMerger<String>();
		public final InitialElementProducer<File> init;
		public final File2SeqOfWords f2b;
		public Distributor<String> distributor;

		public TestConfiguration() {
			int threads = 2;
			init = new InitialElementProducer<File>(new File(""));
			f2b = new File2SeqOfWords("UTF-8", 512);
			distributor = new Distributor<String>(new RoundRobinStrategy2());

			// last part
			final Merger<CountingMap<String>> merger = new Merger<CountingMap<String>>();
			// CountingMapMerger (already as field)

			// Connecting the stages of the first part of the config
			connectIntraThreads(init.getOutputPort(), f2b.getInputPort());
			connectIntraThreads(f2b.getOutputPort(), distributor.getInputPort());

			// Middle part... multiple instances of WordCounter are created and connected to the merger and distrubuter stages
			for (int i = 0; i < threads; i++) {
				// final InputPortSizePrinter<String> inputPortSizePrinter = new InputPortSizePrinter<String>();
				final WordCounter wc = new WordCounter();
				// intraFact.create(inputPortSizePrinter.getOutputPort(), wc.getInputPort());

				final IPipe distributorPipe = connectBoundedInterThreads(distributor.getNewOutputPort(), wc.getInputPort(), 10000);
				final IPipe mergerPipe = connectBoundedInterThreads(wc.getOutputPort(), merger.getNewInputPort());
				// Add WordCounter as a threadable stage, so it runs in its own thread
				addThreadableStage(wc);

			}

			// Connect the stages of the last part
			connectIntraThreads(merger.getOutputPort(), result.getInputPort());

			// Add the first and last part to the threadable stages
			addThreadableStage(init);
			addThreadableStage(merger);
		}

	}

}