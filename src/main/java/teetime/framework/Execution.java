/**
 * Copyright (C) 2015 Christian Wulf, Nelson Tavares de Sousa (http://christianwulf.github.io/teetime)
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

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import teetime.framework.signal.ValidatingSignal;
import teetime.framework.validation.AnalysisNotValidException;

/**
 * Represents an Execution to which stages can be added and executed later.
 * This needs a {@link Configuration},
 * in which the adding and configuring of stages takes place.
 * To start the analysis {@link #executeBlocking()} needs to be executed.
 * This class will automatically create threads and join them without any further commitment.
 *
 * @author Christian Wulf, Nelson Tavares de Sousa
 *
 * @param <T>
 *            the type of the {@link Configuration}
 *
 * @since 2.0
 */
public final class Execution<T extends Configuration> {

	// private static final Logger LOGGER = LoggerFactory.getLogger(Execution.class);

	private final T configuration;
	private final ConfigurationContext configurationContext;

	/**
	 * Creates a new {@link Execution} that skips validating the port connections and uses the default listener.
	 *
	 * @param configuration
	 *            to be used for the analysis
	 */
	public Execution(final T configuration) {
		this(configuration, false);
	}

	/**
	 * Creates a new {@link Execution} that uses a specific listener.
	 *
	 * @param configuration
	 *            to be used for the analysis
	 * @param validationEnabled
	 *            whether or not the validation should be executed
	 */
	public Execution(final T configuration, final boolean validationEnabled) {
		this.configuration = configuration;
		this.configurationContext = configuration.getContext();
		if (configuration.isInitialized()) {
			throw new IllegalStateException("Configuration was already executed");
		}
		configuration.setInitialized(true);
		if (validationEnabled) {
			validateStages();
		}
		init();
	}

	// BETTER validate concurrently
	private void validateStages() {
		final Set<Stage> threadableStages = configurationContext.getThreadableStages();
		for (Stage stage : threadableStages) {
			// // portConnectionValidator.validate(stage);
			// }

			final ValidatingSignal validatingSignal = new ValidatingSignal();
			stage.onSignal(validatingSignal, null);
			if (validatingSignal.getInvalidPortConnections().size() > 0) {
				throw new AnalysisNotValidException(validatingSignal.getInvalidPortConnections());
			}
		}
	}

	/**
	 * This initializes the analysis and needs to be run right before starting it.
	 *
	 */
	private final void init() {
		configurationContext.initializeServices();
	}

	/**
	 * Calling this method will block the current thread until the execution terminates.
	 *
	 * @throws ExecutionException
	 *             if at least one exception in one thread has occurred within the execution. The exception contains the pairs of thread and throwable
	 *
	 * @since 2.0
	 */
	public void waitForTermination() {
		int numExceptions = 0;
		configurationContext.waitForConfigurationToTerminate();
		for (Entry<Thread, List<Exception>> entry : configuration.getFactory().getThreadExceptionsMap().entrySet()) {
			numExceptions += entry.getValue().size();
		}
		if (numExceptions != 0) {
			throw new ExecutionException(configuration.getFactory().getThreadExceptionsMap());
		}
	}

	// TODO: implement
	public void abortEventually() {
		configurationContext.abortConfigurationRun();
		waitForTermination();
	}

	/**
	 * This method will start this execution and block until it is finished.
	 *
	 * @throws ExecutionException
	 *             if at least one exception in one thread has occurred within the execution. The exception contains the pairs of thread and throwable.
	 *
	 * @since 2.0
	 */
	public void executeBlocking() {
		executeNonBlocking();
		waitForTermination();
	}

	/**
	 * This method starts this execution without waiting for its termination. The method {@link #waitForTermination()} must be called to unsure a correct termination
	 * of the execution.
	 *
	 * @since 2.0
	 */
	public void executeNonBlocking() {
		if (configuration.isExecuted()) {
			throw new IllegalStateException("Any configuration instance may only be executed once.");
		}
		configuration.setExecuted(true);
		configurationContext.executeConfiguration();
	}

	/**
	 * Retrieves the Configuration which was used to add and arrange all stages needed for this execution.
	 *
	 * @return the configuration used for this execution
	 */
	public T getConfiguration() {
		return this.configuration;
	}

}
