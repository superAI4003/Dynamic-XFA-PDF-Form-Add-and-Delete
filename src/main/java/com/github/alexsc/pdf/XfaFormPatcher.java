package com.github.alexsc.pdf;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.xfa.XfaForm;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class XfaFormPatcher {

	public static void main(String[] args) throws Exception {
		File sourceFile = new File("FDA-2253_Dyn_Sec_Ext_R8 09-28-2021.pdf");
		File destinationFile = new File("FDA-2253_Dyn_Sec_Ext_R8 09-28-2021-patched.pdf");

		// Load PDF
		PdfWriter writer = new PdfWriter(destinationFile);
		PdfReader pdfReader = new PdfReader(sourceFile);
		pdfReader.setUnethicalReading(true);
		PdfDocument pdf = new PdfDocument(pdfReader, writer, new StampingProperties().useAppendMode());

		// Process XFA form
		PdfAcroForm form = PdfAcroForm.getAcroForm(pdf, true);
		XfaForm xfa = form.getXfaForm();
		org.w3c.dom.Document xfaDocument = xfa.getDomDocument();
		dumpDocument(xfaDocument, "original.xml");
		traverseNodes(xfaDocument.getDocumentElement());
		dumpDocument(xfaDocument, "patched.xml");

		// Update XFA form and save PDF
		xfa.setDomDocument(xfaDocument);
		xfa.write(pdf);
		pdf.close();
	}

	private static void dumpDocument(org.w3c.dom.Document document, String filename) throws IOException, TransformerException {
		DOMSource domSource = new DOMSource(document);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(domSource, result);

		try (PrintWriter out = new PrintWriter(filename)) {
			out.println(writer.toString());
		}
	}

	private static void adjustNewNode(Node node) throws Exception {
		NamedNodeMap attributes = node.getAttributes();
		attributes.getNamedItem("name").setNodeValue("btnAddRowAbove");

		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodes = (NodeList) xPath.evaluate("//*", node, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node subNode = nodes.item(i);
			if ("text".equals(subNode.getNodeName())) {
				subNode.setTextContent("Add Row Above");
			}
			if ("script".equals(subNode.getNodeName())) {
				subNode.setTextContent("item7.materialTable._materialRow.insertInstance(materialRow.instanceIndex, true);");
			}
		}
		// TODO: adjust anything else
	}

	private static void adjustOldNode(Node node) throws Exception {
		//NamedNodeMap attributes = node.getAttributes();
		//attributes.getNamedItem("w").setNodeValue("5.032mm");
	}

	private static boolean isExistingDeleteButton(Node node) {
		if ("field".equals(node.getNodeName()) && node.getAttributes().getNamedItem("name").getTextContent().contains("btnDelete")) {
			return true;
		}

		return false;
	}

	private static boolean isMaterialTable(Node node) {
		if ("subform".equals(node.getNodeName()) && node.getAttributes().getNamedItem("name").getTextContent().contains("materialTable")) {
			return true;
		}

		return false;
	}

	private static boolean traverseNodes(Node node) throws Exception {
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				traverseNodes(currentNode);
				// Clone the existing 'Delete' button
				if (isExistingDeleteButton(currentNode)) {
					Node newNode = currentNode.cloneNode(true);
					adjustOldNode(currentNode);
					adjustNewNode(newNode);
					node.insertBefore(newNode, currentNode.getNextSibling());
					return false;
				}
				if (isMaterialTable(currentNode)) {
					NamedNodeMap attributes = currentNode.getAttributes();
					// Original value = '36.768mm 22mm 36.768mm 89.29mm 0.375083in'
					// Split the last column into two columns
					if (attributes.getNamedItem("columnWidths") != null) {
						attributes.getNamedItem("columnWidths").setNodeValue("36.768mm 22mm 36.768mm 79.29mm 0.375083in 0.375083in");
					}
				}
			}
		}

		return false;
	}
}
