package teetime.variant.methodcallWithPorts.stage;

import teetime.variant.methodcallWithPorts.framework.core.ConsumerStage;
import teetime.variant.methodcallWithPorts.framework.core.OutputPort;

public class InstanceCounter<T, C extends T> extends ConsumerStage<T> {

	private final OutputPort<T> outputPort = this.createOutputPort();

	private final Class<C> type;
	private int counter;

	public InstanceCounter(final Class<C> type) {
		this.type = type;
	}

	@Override
	protected void execute(final T element) {
		if (this.type.isInstance(element)) {
			this.counter++;
		}

		this.send(this.outputPort, element);
	}

	public int getCounter() {
		return this.counter;
	}

	public OutputPort<T> getOutputPort() {
		return outputPort;
	}

}