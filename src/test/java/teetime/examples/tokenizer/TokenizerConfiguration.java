package teetime.examples.tokenizer;

import java.io.File;

import teetime.framework.AnalysisConfiguration;
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

		PIPE_FACTORY_REGISTRY.getPipeFactory(ThreadCommunication.INTRA, PipeOrdering.ARBITRARY, false).create(
				init.getOutputPort(), f2b.getInputPort());
		PIPE_FACTORY_REGISTRY.getPipeFactory(ThreadCommunication.INTRA, PipeOrdering.ARBITRARY, false).create(
				f2b.getOutputPort(), decomp.getInputPort());
		PIPE_FACTORY_REGISTRY.getPipeFactory(ThreadCommunication.INTRA, PipeOrdering.ARBITRARY, false).create(
				decomp.getOutputPort(), decrypt.getInputPort());
		PIPE_FACTORY_REGISTRY.getPipeFactory(ThreadCommunication.INTRA, PipeOrdering.ARBITRARY, false).create(
				decrypt.getOutputPort(), b2s.getInputPort());
		PIPE_FACTORY_REGISTRY.getPipeFactory(ThreadCommunication.INTRA, PipeOrdering.ARBITRARY, false).create(
				b2s.getOutputPort(), tokenizer.getInputPort());
		PIPE_FACTORY_REGISTRY.getPipeFactory(ThreadCommunication.INTRA, PipeOrdering.ARBITRARY, false).create(
				tokenizer.getOutputPort(), this.counter.getInputPort());

		// this.getFiniteProducerStages().add(init);
		this.addThreadableStage(init);
	}

	public int getTokenCount() {
		return this.counter.getNumElementsPassed();
	}

}
