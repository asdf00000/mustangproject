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

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.mustangproject.EStandard;
import org.mustangproject.FileAttachment;
import org.mustangproject.XMLTools;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mustangproject.ZUGFeRD.ZUGFeRDDateFormat.DATE;
import static org.mustangproject.ZUGFeRD.model.DocumentCodeTypeConstants.CORRECTEDINVOICE;
import static org.mustangproject.ZUGFeRD.model.TaxCategoryCodeTypeConstants.CATEGORY_CODES_WITH_EXEMPTION_REASON;

public class UBLDAPullProvider implements IXMLProvider {

	protected IExportableTransaction trans;
	protected TransactionCalculator calc;
	byte[] ublData;
	protected Profile profile = Profiles.getByName(EStandard.ubldespatchadvice, "basic", 1);


	@Override
	public void generateXML(IExportableTransaction trans) {
		this.trans = trans;
		this.calc = new TransactionCalculator(trans);

		final SimpleDateFormat ublDateFormat = new SimpleDateFormat("yyyy-MM-dd");


		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<DespatchAdvice xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:DespatchAdvice-2\" xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\" xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\" xmlns:cec=\"urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2\" xmlns:csc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonSignatureComponents-2\">\n" +
				"  <cbc:ID>" + XMLTools.encodeXML(trans.getNumber()) + "</cbc:ID>\n" +
				"  <cbc:IssueDate>"+ublDateFormat.format(trans.getIssueDate())+"</cbc:IssueDate>\n" +
				"  <cac:DespatchSupplierParty/>\n" +
				"  <cac:DeliveryCustomerParty/>\n";

		int i = 1;
		for (IZUGFeRDExportableItem item : trans.getZFItems()) {
			xml +=
					"  <cac:DespatchLine>\n" +
							"    <cbc:ID>" + XMLTools.encodeXML(Integer.toString(i++)) + "</cbc:ID>\n" +
							"    <cac:OrderLineReference>\n" +
							"      <cbc:LineID>" + XMLTools.encodeXML(item.getBuyerOrderReferencedDocumentLineID()) + "</cbc:LineID>\n" +
							"    </cac:OrderLineReference>\n" +
							"    <cac:Item/>\n" +
							"  </cac:DespatchLine>\n";

		}
		xml += "</DespatchAdvice>\n";
		final byte[] ublRaw;
		try {
			ublRaw = xml.getBytes("UTF-8");

			ublData = XMLTools.removeBOM(ublRaw);
		} catch (final UnsupportedEncodingException e) {
			Logger.getLogger(UBLDAPullProvider.class.getName()).log(Level.SEVERE, null, e);
		}
	}


	@Override
	public byte[] getXML() {

		byte[] res = ublData;

		final StringWriter sw = new StringWriter();
		Document document = null;
		try {
			document = DocumentHelper.parseText(new String(ublData));
		} catch (final DocumentException e1) {
			Logger.getLogger(ZUGFeRD2PullProvider.class.getName()).log(Level.SEVERE, null, e1);
		}
		try {
			final OutputFormat format = OutputFormat.createPrettyPrint();
			format.setTrimText(false);
			final XMLWriter writer = new XMLWriter(sw, format);
			writer.write(document);
			res = sw.toString().getBytes(StandardCharsets.UTF_8);

		} catch (final IOException e) {
			Logger.getLogger(ZUGFeRD2PullProvider.class.getName()).log(Level.SEVERE, null, e);
		}

		return res;

	}

	@Override
	public void setTest() {

	}

	@Override
	public void setProfile(Profile p) {
		profile = p;
	}

	@Override
	public Profile getProfile() {
		return profile;
	}

}
