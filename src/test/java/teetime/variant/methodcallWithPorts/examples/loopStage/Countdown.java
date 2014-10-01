package teetime.variant.methodcallWithPorts.examples.loopStage;

import teetime.variant.methodcallWithPorts.framework.core.InputPort;
import teetime.variant.methodcallWithPorts.framework.core.OutputPort;
import teetime.variant.methodcallWithPorts.framework.core.ProducerStage;

public class Countdown extends ProducerStage<Void> {

	private final InputPort<Integer> countdownInputPort = this.createInputPort();

	private final OutputPort<Integer> newCountdownOutputPort = this.createOutputPort();

	private final Integer initialCountdown;

	public Countdown(final Integer initialCountdown) {
		this.initialCountdown = initialCountdown;
	}

	@Override
	public void onStarting() {
		this.countdownInputPort.getPipe().add(this.initialCountdown);
		super.onStarting();
	}

	@Override
	protected void execute() {
		Integer countdown = this.countdownInputPort.receive();
		if (countdown == 0) {
			this.send(this.outputPort, null);
			this.terminate();
		} else {
			this.send(this.newCountdownOutputPort, --countdown);
		}
	}

	public InputPort<Integer> getCountdownInputPort() {
		return this.countdownInputPort;
	}

	public OutputPort<Integer> getNewCountdownOutputPort() {
		return this.newCountdownOutputPort;
	}

}