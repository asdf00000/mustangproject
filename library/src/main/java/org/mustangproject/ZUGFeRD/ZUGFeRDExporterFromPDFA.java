/**
 * *********************************************************************
 * <p>
 * Copyright 2018 Jochen Staerk
 * <p>
 * Use is subject to license terms.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0.
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * **********************************************************************
 */
package org.mustangproject.ZUGFeRD;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;

import javax.activation.DataSource;
import java.io.*;

public class ZUGFeRDExporterFromPDFA implements IZUGFeRDExporter {
	protected IZUGFeRDExporter theExporter;

	public IZUGFeRDExporter load(String pdfFilename) throws IOException {
		if (getPDFAVersion(fileToByteArrayInputStream(pdfFilename)) < 2) {
			theExporter = new ZUGFeRDExporterFromA1();
		} else if (getPDFAVersion(fileToByteArrayInputStream(pdfFilename)) >= 3) {
			theExporter = new ZUGFeRDExporterFromA3();
		}
		return theExporter.load(pdfFilename);
	}

	private byte[] fileToByteArrayInputStream(String pdfFilename) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(pdfFilename);
		return fileInputStream.readAllBytes();
	}

	private int getPDFAVersion(byte[] byteArrayInputStream) throws IOException {
		// PDFBOX to be here...
		PDDocument document = PDDocument.load(byteArrayInputStream);
		PDDocumentCatalog catalog = document.getDocumentCatalog();
		PDMetadata metadata = catalog.getMetadata();

		if (metadata != null) {
			try {
				DomXmpParser xmpParser = new DomXmpParser();
				XMPMetadata xmp = xmpParser.parse(metadata.createInputStream());

				PDFAIdentificationSchema pdfaSchema = xmp.getPDFIdentificationSchema();
				if (pdfaSchema != null) {
					return pdfaSchema.getPart();
				}
			} catch (XmpParsingException e) {
				e.printStackTrace();
			} finally {
				document.close();
			}
		}
		return 0;
	}

	/**
	 * Makes A PDF/A3a-compliant document from a PDF-A1 compliant document (on the
	 * metadata level, this will not e.g. convert graphics to JPG-2000)
	 *
	 * @param pdfBinary binary of a PDF/A1 compliant document
	 * @return the generated exporter
	 * @throws IOException (should not happen at all)
	 */
	public IZUGFeRDExporter load(byte[] pdfBinary) throws IOException {
		if (getPDFAVersion(pdfBinary) >= 3) {
			theExporter = new ZUGFeRDExporterFromA3();
		} else if (getPDFAVersion(pdfBinary) < 2) {
			theExporter = new ZUGFeRDExporterFromA1();
		}
		return theExporter.load(pdfBinary);
	}


	/**
	 * Makes A PDF/A3a-compliant document from a PDF-A1 compliant document (on the
	 * metadata level, this will not e.g. convert graphics to JPG-2000)
	 *
	 * @param pdfSource source to read a PDF/A1 compliant document from
	 * @return the generated ZUGFeRDExporter
	 * @throws IOException if anything is wrong with inputstream
	 */
	public IZUGFeRDExporter load(InputStream pdfSource) throws IOException {
		if (getPDFAVersion(pdfSource.readAllBytes()) >= 3) {
			theExporter = new ZUGFeRDExporterFromA3();
		} else if (getPDFAVersion(pdfSource.readAllBytes()) < 2) {
			theExporter = new ZUGFeRDExporterFromA1();
		}
		return theExporter.load(pdfSource);
	}

	public IZUGFeRDExporter setCreator(String creator) {
		return theExporter.setCreator(creator);
	}

	public ZUGFeRDExporterFromPDFA setProfile(Profile p) {
		return (ZUGFeRDExporterFromPDFA) theExporter.setProfile(p);
	}

	public ZUGFeRDExporterFromPDFA setProfile(String profileName) {
		Profile p = Profiles.getByName(profileName);
		return (ZUGFeRDExporterFromPDFA) theExporter.setProfile(p);
	}

	public IZUGFeRDExporter setConformanceLevel(PDFAConformanceLevel newLevel) {
		return theExporter.setConformanceLevel(newLevel);
	}

	public IZUGFeRDExporter setProducer(String producer) {
		return theExporter.setProducer(producer);
	}

	public IZUGFeRDExporter setZUGFeRDVersion(int version) {

		return theExporter.setZUGFeRDVersion(version);

	}

	public boolean ensurePDFIsValid(final DataSource dataSource) throws IOException {
		return theExporter.ensurePDFIsValid(dataSource);
	}

	public IZUGFeRDExporter setXML(byte[] zugferdData) throws IOException {
		return theExporter.setXML(zugferdData);
	}

	public IZUGFeRDExporter disableFacturX() {
		return theExporter.disableFacturX();
	}

	//	public IZUGFeRDExporter setProfile(Profile zugferdConformanceLevel);
	public String getNamespaceForVersion(int ver) {
		return theExporter.getNamespaceForVersion(ver);
	}

	public String getPrefixForVersion(int ver) {
		return theExporter.getPrefixForVersion(ver);
	}

	public IZUGFeRDExporter disableAutoClose(boolean disableAutoClose) {
		return theExporter.disableAutoClose(disableAutoClose);
	}

	public IXMLProvider getProvider() {
		return theExporter.getProvider();
	}

	@Override
	public void close() throws IOException {
		theExporter.close();
	}

	@Override
	public IExporter setTransaction(IExportableTransaction trans) throws IOException {
		return theExporter.setTransaction(trans);
	}

	@Override
	public void export(String ZUGFeRDfilename) throws IOException {
		theExporter.export(ZUGFeRDfilename);
	}

	@Override
	public void export(OutputStream output) throws IOException {
		theExporter.export(output);
	}
}
