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
package teetime.stage;

import teetime.framework.AbstractConsumerStage;
import teetime.framework.OutputPort;

/**
 * @author Christian Wulf
 *
 * @since 1.0
 */
public final class NoopFilter<T> extends AbstractConsumerStage<T> {

	private final OutputPort<T> outputPort = this.createOutputPort();

	@Override
	protected void execute(final T element) {
		outputPort.send(element);
	}

	public OutputPort<T> getOutputPort() {
		return this.outputPort;
	}

}
