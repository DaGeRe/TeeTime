package teetime.variant.methodcallWithPorts.examples.traceReconstruction;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import teetime.util.concurrent.hashmap.ConcurrentHashMapWithDefault;
import teetime.util.concurrent.hashmap.TraceBuffer;
import teetime.variant.explicitScheduling.framework.core.Analysis;
import teetime.variant.methodcallWithPorts.framework.core.Pipeline;
import teetime.variant.methodcallWithPorts.framework.core.RunnableStage;
import teetime.variant.methodcallWithPorts.framework.core.StageWithPort;
import teetime.variant.methodcallWithPorts.framework.core.pipe.SingleElementPipe;
import teetime.variant.methodcallWithPorts.framework.core.pipe.SpScPipe;
import teetime.variant.methodcallWithPorts.stage.Cache;
import teetime.variant.methodcallWithPorts.stage.Clock;
import teetime.variant.methodcallWithPorts.stage.CollectorSink;
import teetime.variant.methodcallWithPorts.stage.CountingFilter;
import teetime.variant.methodcallWithPorts.stage.ElementThroughputMeasuringStage;
import teetime.variant.methodcallWithPorts.stage.InstanceOfFilter;
import teetime.variant.methodcallWithPorts.stage.kieker.Dir2RecordsFilter;
import teetime.variant.methodcallWithPorts.stage.kieker.className.ClassNameRegistryRepository;
import teetime.variant.methodcallWithPorts.stage.kieker.traceReconstruction.TraceReconstructionFilter;
import teetime.variant.methodcallWithPorts.stage.stringBuffer.StringBufferFilter;
import teetime.variant.methodcallWithPorts.stage.stringBuffer.handler.IMonitoringRecordHandler;
import teetime.variant.methodcallWithPorts.stage.stringBuffer.handler.StringHandler;

import kieker.analysis.plugin.filter.flow.TraceEventRecords;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.flow.IFlowRecord;

public class TraceReconstructionAnalysis extends Analysis {

	private final List<TraceEventRecords> elementCollection = new LinkedList<TraceEventRecords>();

	private Thread clockThread;
	private Thread workerThread;

	private ClassNameRegistryRepository classNameRegistryRepository;
	private final Map<Long, TraceBuffer> traceId2trace = new ConcurrentHashMapWithDefault<Long, TraceBuffer>(new TraceBuffer());

	private CountingFilter<IMonitoringRecord> recordCounter;
	private CountingFilter<TraceEventRecords> traceCounter;
	private ElementThroughputMeasuringStage<IFlowRecord> throughputFilter;

	private File inputDir;

	@Override
	public void init() {
		super.init();
		StageWithPort<Void, Long> clockStage = this.buildClockPipeline();
		this.clockThread = new Thread(new RunnableStage(clockStage));

		Pipeline<?, ?> pipeline = this.buildPipeline(clockStage);
		this.workerThread = new Thread(new RunnableStage(pipeline));
	}

	private StageWithPort<Void, Long> buildClockPipeline() {
		Clock clock = new Clock();
		clock.setIntervalDelayInMs(100);

		return clock;
	}

	private Pipeline<File, Void> buildPipeline(final StageWithPort<Void, Long> clockStage) {
		this.classNameRegistryRepository = new ClassNameRegistryRepository();

		// create stages
		final Dir2RecordsFilter dir2RecordsFilter = new Dir2RecordsFilter(this.classNameRegistryRepository);
		this.recordCounter = new CountingFilter<IMonitoringRecord>();
		final Cache<IMonitoringRecord> cache = new Cache<IMonitoringRecord>();

		final StringBufferFilter<IMonitoringRecord> stringBufferFilter = new StringBufferFilter<IMonitoringRecord>();
		final InstanceOfFilter<IMonitoringRecord, IFlowRecord> instanceOfFilter = new InstanceOfFilter<IMonitoringRecord, IFlowRecord>(
				IFlowRecord.class);
		this.throughputFilter = new ElementThroughputMeasuringStage<IFlowRecord>();
		final TraceReconstructionFilter traceReconstructionFilter = new TraceReconstructionFilter(this.traceId2trace);
		this.traceCounter = new CountingFilter<TraceEventRecords>();
		final CollectorSink<TraceEventRecords> collector = new CollectorSink<TraceEventRecords>(this.elementCollection);

		// configure stages
		stringBufferFilter.getDataTypeHandlers().add(new IMonitoringRecordHandler());
		stringBufferFilter.getDataTypeHandlers().add(new StringHandler());

		// connect stages
		SpScPipe.connect(null, dir2RecordsFilter.getInputPort(), 1);
		SingleElementPipe.connect(dir2RecordsFilter.getOutputPort(), this.recordCounter.getInputPort());
		SingleElementPipe.connect(this.recordCounter.getOutputPort(), cache.getInputPort());
		SingleElementPipe.connect(cache.getOutputPort(), stringBufferFilter.getInputPort());
		SingleElementPipe.connect(stringBufferFilter.getOutputPort(), instanceOfFilter.getInputPort());
		SingleElementPipe.connect(instanceOfFilter.getOutputPort(), this.throughputFilter.getInputPort());
		SingleElementPipe.connect(this.throughputFilter.getOutputPort(), traceReconstructionFilter.getInputPort());
		// SingleElementPipe.connect(instanceOfFilter.getOutputPort(), traceReconstructionFilter.getInputPort());
		SingleElementPipe.connect(traceReconstructionFilter.getOutputPort(), this.traceCounter.getInputPort());
		SingleElementPipe.connect(this.traceCounter.getOutputPort(), collector.getInputPort());

		SpScPipe.connect(clockStage.getOutputPort(), this.throughputFilter.getTriggerInputPort(), 1);

		// fill input ports
		dir2RecordsFilter.getInputPort().getPipe().add(this.inputDir);

		// create and configure pipeline
		Pipeline<File, Void> pipeline = new Pipeline<File, Void>();
		pipeline.setFirstStage(dir2RecordsFilter);
		pipeline.addIntermediateStage(this.recordCounter);
		pipeline.addIntermediateStage(cache);
		pipeline.addIntermediateStage(stringBufferFilter);
		pipeline.addIntermediateStage(instanceOfFilter);
		pipeline.addIntermediateStage(this.throughputFilter);
		pipeline.addIntermediateStage(traceReconstructionFilter);
		pipeline.addIntermediateStage(this.traceCounter);
		pipeline.setLastStage(collector);
		return pipeline;
	}

	@Override
	public void start() {
		super.start();

		this.clockThread.start();
		this.workerThread.start();

		try {
			this.workerThread.join();
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
		this.clockThread.interrupt();
	}

	public List<TraceEventRecords> getElementCollection() {
		return this.elementCollection;
	}

	public int getNumRecords() {
		return this.recordCounter.getNumElementsPassed();
	}

	public int getNumTraces() {
		return this.traceCounter.getNumElementsPassed();
	}

	public List<Long> getThroughputs() {
		return this.throughputFilter.getThroughputs();
	}

	public File getInputDir() {
		return this.inputDir;
	}

	public void setInputDir(final File inputDir) {
		this.inputDir = inputDir;
	}
}