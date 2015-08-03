package teetime.stage.xml;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import teetime.framework.test.StageTester;

public class ApplyXSLTStageTest {

	@Test
	public void applyXSLTStageTest() {
		try {
			File xmlFile = createExampleXMLFile();

			LoadXMLStage loadStage = new LoadXMLStage();
			List<Document> documents = new ArrayList<Document>();

			StageTester.test(loadStage).and()
					.send(xmlFile.getAbsolutePath()).to(loadStage.getInputPort()).and()
					.receive(documents).from(loadStage.getOutputPort())
					.start();

			Document outputXML = documents.get(0);
			NodeList as = outputXML.getDocumentElement().getChildNodes();
			assertThat(as.getLength(), is(equalTo(5)));
			assertThat(as.item(0).getAttributes().item(0).getNodeValue(), is(equalTo("1")));
			assertThat(as.item(1).getAttributes().item(0).getNodeValue(), is(equalTo("2")));
			assertThat(as.item(2).getAttributes().item(0).getNodeValue(), is(equalTo("3")));
			assertThat(as.item(3).getAttributes().item(0).getNodeValue(), is(equalTo("4")));
			assertThat(as.item(4).getAttributes().item(0).getNodeValue(), is(equalTo("5")));
			documents.clear();

			File xsltFile = createExampleXSLTFile();

			ApplyXSLTStage xsltStage = new ApplyXSLTStage(xsltFile);
			List<String> outputs = new ArrayList<String>();

			StageTester.test(xsltStage).and()
					.send(outputXML).to(xsltStage.getInputPort()).and()
					.receive(outputs).from(xsltStage.getOutputPort())
					.start();

			assertThat(outputs.get(0), is(equalTo("54321")));

			xmlFile.delete();
			xsltFile.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File createExampleXSLTFile() throws IOException, FileNotFoundException {
		File xsltFile = File.createTempFile("applyxslttest", ".xslt");
		FileOutputStream outputStreamXSLT = new FileOutputStream(xsltFile);

		String xslt = "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\r\n" +
				"<xsl:output method=\"text\"/>\r\n" +
				"<xsl:template match=\"/\">\r\n" +
				"    <xsl:apply-templates select=\"root/a\">\r\n" +
				"        <xsl:sort select=\"position()\" data-type=\"number\" order=\"descending\"/>\r\n" +
				"    </xsl:apply-templates>\r\n" +
				"</xsl:template>\r\n" +
				"<xsl:template match=\"root\">\r\n" +
				"<xsl:value-of select=\".\"/>\r\n" +
				"</xsl:template>\r\n" +
				"</xsl:stylesheet>\r\n";
		outputStreamXSLT.write(xslt.getBytes());
		outputStreamXSLT.flush();
		outputStreamXSLT.close();
		return xsltFile;
	}

	private File createExampleXMLFile() throws IOException, FileNotFoundException {
		File xmlFile = File.createTempFile("applyxslttest", ".xml");
		FileOutputStream outputStreamXML = new FileOutputStream(xmlFile);

		String xml = "<?xml version=\"1.0\" ?>"
				+ "<root><a value=\"1\">1</a><a value=\"2\">2</a><a value=\"3\">3</a><a value=\"4\">4</a><a value=\"5\">5</a></root>";
		outputStreamXML.write(xml.getBytes());
		outputStreamXML.flush();
		outputStreamXML.close();
		return xmlFile;
	}
}
