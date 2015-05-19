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

import java.util.concurrent.Semaphore;

import teetime.framework.signal.InitializingSignal;
import teetime.framework.signal.StartingSignal;
import teetime.framework.signal.TerminatingSignal;

final class RunnableProducerStage extends AbstractRunnableStage {

	private final Semaphore startSemaphore = new Semaphore(0);
	private final Semaphore initSemaphore = new Semaphore(0);

	public RunnableProducerStage(final Stage stage) {
		super(stage);
	}

	@Override
	protected void beforeStageExecution() throws InterruptedException {
		waitForInitializingSignal();
		this.stage.onSignal(new InitializingSignal(), null);
		waitForStartingSignal();
		this.stage.onSignal(new StartingSignal(), null);
	}

	@Override
	protected void executeStage() {
		this.stage.executeStage();
	}

	@Override
	protected void afterStageExecution() {
		final TerminatingSignal terminatingSignal = new TerminatingSignal();
		this.stage.onSignal(terminatingSignal, null);
	}

	public void sendInitializingSignal() {
		initSemaphore.release();
	}

	public void sendStartingSignal() {
		startSemaphore.release();
	}

	public void waitForInitializingSignal() throws InterruptedException {
		initSemaphore.acquire();
	}

	public void waitForStartingSignal() throws InterruptedException {
		startSemaphore.acquire();
	}
}
