/**
 * Copyright © 2015 Christian Wulf, Nelson Tavares de Sousa (http://teetime-framework.github.io)
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
 */
package teetime.framework.test;

import java.util.Collection;

import teetime.framework.AbstractStage;
import teetime.framework.InputPort;

public class InputHolder<I> {

	private final StageTester stageTester;
	private final AbstractStage stage;
	private final Collection<I> inputElements;

	private InputPort<? super I> port;

	InputHolder(final StageTester stageTester, final AbstractStage stage, final Collection<I> inputElements) {
		this.stageTester = stageTester;
		this.stage = stage;
		this.inputElements = inputElements;
	}

	public StageTestSetup to(final InputPort<? super I> port) { // NOPMD deliberately chosen name
		if (port.getOwningStage() != stage) {
			throw new InvalidTestCaseSetupException("The given input port does not belong to the stage which should be tested.");
		}
		this.port = port;

		return new StageTestSetup(stageTester);
	}

	/* default */ Collection<I> getInputElements() {
		return inputElements;
	}

	/* default */ InputPort<? super I> getPort() {
		return port;
	}

}
