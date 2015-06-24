package teetime.stage.basic.distributor.dynamic;

import teetime.util.framework.port.PortActionHelper;

class ControlledDynamicDistributor<T> extends DynamicDistributor<T> {

	@Override
	protected void checkForPendingPortActionRequest() {
		try {
			PortActionHelper.checkBlockingForPendingPortActionRequest(this, portActions);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	// @Override
	// protected OutputPort<?>[] getOutputPorts() { // repeated declaration for testing purposes
	// return super.getOutputPorts();
	// }

}
