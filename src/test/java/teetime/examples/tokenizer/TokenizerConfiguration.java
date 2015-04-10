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
package teetime.examples.tokenizer;

import java.io.File;

import teetime.framework.AnalysisConfiguration;
import teetime.framework.pipe.IPipeFactory;
import teetime.framework.pipe.PipeFactoryRegistry.PipeOrdering;
import teetime.framework.pipe.PipeFactoryRegistry.ThreadCommunication;
import teetime.stage.ByteArray2String;
import teetime.stage.CipherByteArray;
import teetime.stage.CipherByteArray.CipherMode;
import teetime.stage.Counter;
import teetime.stage.InitialElementProducer;
import teetime.stage.ZipByteArray;
import teetime.stage.ZipByteArray.ZipMode;
import teetime.stage.io.File2ByteArray;
import teetime.stage.string.Tokenizer;

public class TokenizerConfiguration extends AnalysisConfiguration {

	private static final IPipeFactory INTRA_PIPE_FACTORY = PIPE_FACTORY_REGISTRY.getPipeFactory(ThreadCommunication.INTRA, PipeOrdering.ARBITRARY, false);
	private final Counter<String> counter;

	public TokenizerConfiguration(final String inputFile, final String password) {
		final File input = new File(inputFile);

		final InitialElementProducer<File> init = new InitialElementProducer<File>(input);
		final File2ByteArray f2b = new File2ByteArray();
		final ZipByteArray decomp = new ZipByteArray(ZipMode.DECOMP);
		final CipherByteArray decrypt = new CipherByteArray(password, CipherMode.DECRYPT);
		final ByteArray2String b2s = new ByteArray2String();
		final Tokenizer tokenizer = new Tokenizer(" ");
		this.counter = new Counter<String>();

		INTRA_PIPE_FACTORY.create(
				init.getOutputPort(), f2b.getInputPort());
		INTRA_PIPE_FACTORY.create(
				f2b.getOutputPort(), decomp.getInputPort());
		INTRA_PIPE_FACTORY.create(
				decomp.getOutputPort(), decrypt.getInputPort());
		INTRA_PIPE_FACTORY.create(
				decrypt.getOutputPort(), b2s.getInputPort());
		INTRA_PIPE_FACTORY.create(
				b2s.getOutputPort(), tokenizer.getInputPort());
		INTRA_PIPE_FACTORY.create(
				tokenizer.getOutputPort(), this.counter.getInputPort());

		// this.getFiniteProducerStages().add(init);
		this.addThreadableStage(init);
	}

	public int getTokenCount() {
		return this.counter.getNumElementsPassed();
	}

}
