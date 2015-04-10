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
package teetime.stage.io;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

import teetime.framework.AbstractConsumerStage;
import teetime.framework.OutputPort;

/**
 * @author Christian Wulf
 * 
 * @since 1.10
 */
public final class Directory2FilesFilter extends AbstractConsumerStage<File> {

	private final OutputPort<File> outputPort = this.createOutputPort();

	private FileFilter filter;
	private Comparator<File> fileComparator;

	/**
	 * @since 1.10
	 */
	public Directory2FilesFilter(final FileFilter fileFilter) {
		this.setFilter(fileFilter);
	}

	/**
	 * @since 1.10
	 */
	public Directory2FilesFilter(final Comparator<File> fileComparator) {
		this.setFileComparator(fileComparator);
	}

	/**
	 * @since 1.10
	 */
	public Directory2FilesFilter(final FileFilter fileFilter, final Comparator<File> fileComparator) {
		this.setFilter(fileFilter);
		this.setFileComparator(fileComparator);
	}

	/**
	 * @since 1.10
	 */
	public Directory2FilesFilter() {
		super();
	}

	@Override
	protected void execute(final File inputDir) {
		final File[] inputFiles = inputDir.listFiles(this.filter);

		if (inputFiles == null) {
			this.logger.error("Directory '" + inputDir + "' does not exist or an I/O error occured.");
			return;
		}

		if (this.fileComparator != null) {
			Arrays.sort(inputFiles, this.fileComparator);
		}

		for (final File file : inputFiles) {
			outputPort.send(file);
		}
	}

	public FileFilter getFilter() {
		return this.filter;
	}

	public void setFilter(final FileFilter filter) {
		this.filter = filter;
	}

	public Comparator<File> getFileComparator() {
		return this.fileComparator;
	}

	public void setFileComparator(final Comparator<File> fileComparator) {
		this.fileComparator = fileComparator;
	}

	public OutputPort<File> getOutputPort() {
		return outputPort;
	}

}
