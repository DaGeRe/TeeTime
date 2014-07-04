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
package teetime.variant.methodcallWithPorts.examples.experiment17;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import teetime.util.ConstructorClosure;
import teetime.variant.explicitScheduling.examples.throughput.TimestampObject;
import teetime.variant.explicitScheduling.framework.core.Analysis;
import teetime.variant.methodcallWithPorts.framework.core.Pipeline;
import teetime.variant.methodcallWithPorts.framework.core.RunnableStage;
import teetime.variant.methodcallWithPorts.framework.core.Signal;
import teetime.variant.methodcallWithPorts.framework.core.StageWithPort;
import teetime.variant.methodcallWithPorts.framework.core.pipe.IPipe;
import teetime.variant.methodcallWithPorts.framework.core.pipe.SpScPipe;
import teetime.variant.methodcallWithPorts.framework.core.pipe.UnorderedGrowablePipe;
import teetime.variant.methodcallWithPorts.stage.CollectorSink;
import teetime.variant.methodcallWithPorts.stage.Distributor;
import teetime.variant.methodcallWithPorts.stage.EndStage;
import teetime.variant.methodcallWithPorts.stage.NoopFilter;
import teetime.variant.methodcallWithPorts.stage.ObjectProducer;
import teetime.variant.methodcallWithPorts.stage.Relay;
import teetime.variant.methodcallWithPorts.stage.Sink;
import teetime.variant.methodcallWithPorts.stage.StartTimestampFilter;
import teetime.variant.methodcallWithPorts.stage.StopTimestampFilter;

/**
 * @author Christian Wulf
 * 
 * @since 1.10
 */
public class MethodCallThroughputAnalysis17 extends Analysis {

	private static final int SPSC_INITIAL_CAPACITY = 100100;
	private static final int NUM_WORKER_THREADS = Runtime.getRuntime().availableProcessors();

	private int numInputObjects;
	private ConstructorClosure<TimestampObject> inputObjectCreator;
	private int numNoopFilters;

	private final List<List<TimestampObject>> timestampObjectsList = new LinkedList<List<TimestampObject>>();

	private Thread producerThread;
	private Thread[] workerThreads;

	@Override
	public void init() {
		final Pipeline<Void, TimestampObject> producerPipeline = this.buildProducerPipeline(this.numInputObjects, this.inputObjectCreator);
		this.producerThread = new Thread(new RunnableStage(producerPipeline));

		int numWorkerThreads = Math.min(NUM_WORKER_THREADS, 1); // only for testing purpose

		this.workerThreads = new Thread[numWorkerThreads];
		for (int i = 0; i < this.workerThreads.length; i++) {
			List<TimestampObject> resultList = new ArrayList<TimestampObject>(this.numInputObjects);
			this.timestampObjectsList.add(resultList);

			Runnable workerRunnable = this.buildPipeline(null, resultList);
			this.workerThreads[i] = new Thread(workerRunnable);
		}

		// this.producerThread = new Thread(new Runnable() {
		// @Override
		// public void run() {
		// TimestampObject ts;
		// try {
		// ts = MethodCallThroughputAnalysis17.this.inputObjectCreator.call();
		// System.out.println("test" + producerPipeline + ", # filters: " + MethodCallThroughputAnalysis17.this.numNoopFilters + ", ts: "
		// + ts);
		// MethodCallThroughputAnalysis17.this.numInputObjects++;
		// System.out.println("numInputObjects: " + MethodCallThroughputAnalysis17.this.numInputObjects);
		// MethodCallThroughputAnalysis17.this.numInputObjects--;
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// System.out.println("run end");
		// }
		// });

		// this.producerThread.start();
		// this.producerThread.run();
		new RunnableStage(producerPipeline).run();

		// try {
		// this.producerThread.join();
		// } catch (InterruptedException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }

		super.init();
	}

	private Pipeline<Void, TimestampObject> buildProducerPipeline(final int numInputObjects, final ConstructorClosure<TimestampObject> inputObjectCreator) {
		final ObjectProducer<TimestampObject> objectProducer = new ObjectProducer<TimestampObject>(numInputObjects, inputObjectCreator);
		Distributor<TimestampObject> distributor = new Distributor<TimestampObject>();
		Sink<TimestampObject> sink = new Sink<TimestampObject>();
		EndStage<Void> endStage = new EndStage<Void>();
		endStage.closure = inputObjectCreator;

		final Pipeline<Void, TimestampObject> pipeline = new Pipeline<Void, TimestampObject>();
		pipeline.setFirstStage(objectProducer);
		// pipeline.setFirstStage(sink);
		// pipeline.setFirstStage(endStage);

		pipeline.setLastStage(distributor);
		// pipeline.setLastStage(sink);
		// pipeline.setLastStage(new EndStage<TimestampObject>());

		// UnorderedGrowablePipe.connect(objectProducer.getOutputPort(), sink.getInputPort());
		// objectProducer.getOutputPort().pipe = new UnorderedGrowablePipe<TimestampObject>();

		UnorderedGrowablePipe.connect(objectProducer.getOutputPort(), distributor.getInputPort());
		distributor.getOutputPort().setPipe(new UnorderedGrowablePipe<TimestampObject>());

		return pipeline;
	}

	/**
	 * @param numNoopFilters
	 * @since 1.10
	 */
	private Runnable buildPipeline(final StageWithPort<Void, TimestampObject> previousStage, final List<TimestampObject> timestampObjects) {
		Relay<TimestampObject> relay = new Relay<TimestampObject>();
		// create stages
		final StartTimestampFilter startTimestampFilter = new StartTimestampFilter();
		@SuppressWarnings("unchecked")
		final NoopFilter<TimestampObject>[] noopFilters = new NoopFilter[this.numNoopFilters];
		for (int i = 0; i < noopFilters.length; i++) {
			noopFilters[i] = new NoopFilter<TimestampObject>();
		}
		final StopTimestampFilter stopTimestampFilter = new StopTimestampFilter();
		final CollectorSink<TimestampObject> collectorSink = new CollectorSink<TimestampObject>(timestampObjects);

		final Pipeline<TimestampObject, Void> pipeline = new Pipeline<TimestampObject, Void>();
		pipeline.setFirstStage(relay);
		pipeline.addIntermediateStage(startTimestampFilter);
		pipeline.addIntermediateStages(noopFilters);
		pipeline.addIntermediateStage(stopTimestampFilter);
		pipeline.setLastStage(collectorSink);

		SpScPipe.connect(null, relay.getInputPort(), SPSC_INITIAL_CAPACITY);
		IPipe<TimestampObject> startPipe = relay.getInputPort().getPipe();
		for (int i = 0; i < this.numInputObjects; i++) {
			startPipe.add(this.inputObjectCreator.create());
		}
		// startPipe.close();
		startPipe.setSignal(Signal.FINISHED);

		UnorderedGrowablePipe.connect(relay.getOutputPort(), startTimestampFilter.getInputPort());

		UnorderedGrowablePipe.connect(startTimestampFilter.getOutputPort(), noopFilters[0].getInputPort());
		for (int i = 0; i < noopFilters.length - 1; i++) {
			UnorderedGrowablePipe.connect(noopFilters[i].getOutputPort(), noopFilters[i + 1].getInputPort());
		}
		UnorderedGrowablePipe.connect(noopFilters[noopFilters.length - 1].getOutputPort(), stopTimestampFilter.getInputPort());
		UnorderedGrowablePipe.connect(stopTimestampFilter.getOutputPort(), collectorSink.getInputPort());

		return new RunnableStage(pipeline);
	}

	@Override
	public void start() {
		super.start();

		for (Thread workerThread : this.workerThreads) {
			workerThread.start();
		}

		try {
			for (Thread workerThread : this.workerThreads) {
				workerThread.join();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setInput(final int numInputObjects, final ConstructorClosure<TimestampObject> inputObjectCreator) {
		this.numInputObjects = numInputObjects;
		this.inputObjectCreator = inputObjectCreator;
	}

	public int getNumNoopFilters() {
		return this.numNoopFilters;
	}

	public void setNumNoopFilters(final int numNoopFilters) {
		this.numNoopFilters = numNoopFilters;
	}

	public List<List<TimestampObject>> getTimestampObjectsList() {
		return this.timestampObjectsList;
	}

}