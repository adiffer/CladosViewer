/**
 * <h2>Copyright</h2> © 2021 Alfred Differ<br>
 * ------------------------------------------------------------------------ <br>
 * ---org.interworldtransport.cladosviewer.FileOpenEvents<br>
 * -------------------------------------------------------------------- <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.<p>
 * 
 * Use of this code or executable objects derived from it by the Licensee 
 * states their willingness to accept the terms of the license. <p> 
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.<p> 
 * 
 * ------------------------------------------------------------------------ <br>
 * ---org.interworldtransport.cladosviewer.FileOpenEvents<br>
 * ------------------------------------------------------------------------ <br>
 */

package org.interworldtransport.cladosviewerEvents;

import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import javax.swing.*;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.interworldtransport.cladosF.Cardinal;
import org.interworldtransport.cladosF.CladosFCache;
import org.interworldtransport.cladosF.CladosField;
import org.interworldtransport.cladosG.Algebra;
import org.interworldtransport.cladosG.CladosGBuilder;
import org.interworldtransport.cladosG.CladosGCache;
import org.interworldtransport.cladosG.Foot;
import org.interworldtransport.cladosG.Monad;
import org.interworldtransport.cladosG.Nyad;
import org.interworldtransport.cladosGExceptions.BadSignatureException;
import org.interworldtransport.cladosGExceptions.GeneratorRangeException;
import org.interworldtransport.cladosviewer.ErrorDialog;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class FileOpenEvents implements ActionListener {

	private final static String path2Algs = "//Algebra";
	private final static String path2AlgUUIDs = "//Algebra/@UUID";
	private final static String path2AllCardinals = "//Foot/Cardinals/Cardinal/@unit";
	private final static String path2AllSignatures = "//GProduct/Signature/text()";
	private final static String path2DivFields = "//Algebra/*[@cardinal]";
	private final static String path2FootCardinals = "//Foot/Name[!]	/Cardinals/Cardinal/@unit"; // re-do this one
	private final static String path2FootNames = "//Algebra/Foot/Name";
	private final static String path2MonadNames = "//Monad/Name/text()";
	private final static String path2Monads = "//Monad";
	private final static String path2NyadNames = "//Nyad/Name/text()";
	private final static String path2Nyads = "//Nyad";
	private final static String path4NyadCount = "count(//Nyad)";
	private ArrayList<String> _algUUIDs;
	private ArrayList<Algebra> _algs;
	private ArrayList<Foot> _foot;
	private ArrayList<String> _monadNames;
	private ArrayList<Monad> _monads;
	private int _nyadCount;
	private ArrayList<String> _nyadNames;
	private ArrayList<Nyad> _nyads;
	private CladosField _repMode;

	protected JMenuItem _control;
	protected FileEvents _parent;

	public FileOpenEvents(JMenuItem _control, FileEvents _parent) {
		super();
		this._control = _control;
		_control.addActionListener(this);
		this._parent = _parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// If SaveFile is unknown at this point, present a file chooser.
		// If there are issues with the file pointed to by SaveFile, present a chooser
		File fIni;
		if (_parent._GUI.IniProps.getProperty("Desktop.File.Snapshot") != null)
		// save to file described in conf setting
		{
			fIni = new File(_parent._GUI.IniProps.getProperty("Desktop.File.Snapshot"));
			if (!(fIni.exists() & fIni.isFile() & fIni.canWrite())) {
				int returnVal = _parent.fc.showSaveDialog(_control);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					fIni = _parent.fc.getSelectedFile();
				} else
					return;
			}
		} else {
			int returnVal = _parent.fc.showSaveDialog(_control);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				fIni = _parent.fc.getSelectedFile();
			} else
				return;
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		XPathFactory xPathFactory = XPathFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(fIni);
			if (doc == null | doc.getFirstChild().getNodeName() != "NyadList")
				return;

			if (findNyadCount(doc, xPathFactory.newXPath()) <= 0)
				return; // STOP if no nyads are to be created
			// ----------------
			// We know how many nyads are involved. Validation done too.
			// ----------------
			_repMode = findDivField(doc, xPathFactory.newXPath());
			if (_parent._GUI.appGeometryView.getNyadListSize() > 0) {
				if (_parent._GUI.appGeometryView.getRepMode() != _repMode)
					return;
			}
			// ----------------
			// We know which DivField child is involved. Validation done too.
			// ----------------
			buildGProducts(doc, xPathFactory.newXPath());
			// ----------------
			// Bases/GProducts built/stored in CladosGBuilder.INSTANCE. Retrieve later.
			// ----------------
			buildCardinals(doc, xPathFactory.newXPath());
			// ----------------
			// Cardinals built/stored in CladosFBuilder.INSTANCE. Retrieve later.
			// ----------------
			buildFoot(doc, xPathFactory.newXPath());
			System.out.println("Made it past Foot building.");
			if (_foot == null | _foot.size() == 0)
				return; // STOP if no Foot objects created.
			appendFootCardinals(doc, xPathFactory.newXPath());
			System.out.println("Made it past Foot Card populating.");
			// ----------------
			// Foot objects built/stored locally in _foot.
			// [[NOT appending Frames right now as those will change soon.]]
			// ----------------
			buildTheAlgebras(doc, xPathFactory.newXPath());
			// ----------------
			// All DISTINCT algebras present in the file are created
			// One issue, though. If two algebras have the same name and different details
			// only the first one encountered will be created.
			// DO NOT name them the same unless you intend them to be the same.
			// ----------------

			_nyadNames = new ArrayList<String>(_nyadCount);
			findAllNyadNames(doc, xPathFactory.newXPath());
			// ----------------
			// _nyadNames will have _nyadCount entries now with some possibly being empty.
			// ----------------
			// TODO parse the XML into the 'defaults' for initiating the calculator.
			// 'Count', 'Order', 'DivField', etc.

		} catch (ParserConfigurationException e1) {
			ErrorDialog.show("Couldn't acquire DocumentBuilderFactory instance.\n" + e1.getMessage(),
					"Parser Configuration Exception");
			return;
		} catch (SAXException e1) {
			ErrorDialog.show("Couldn't parse the XML to create a Document.\n" + e1.getMessage(), "SAX Exception");
			return;
		} catch (IOException e1) {
			ErrorDialog.show("Stopped by a general IO issue.\n" + e1.getMessage(), "IO Exception");
			return;
		} catch (NumberFormatException e1) {
			ErrorDialog.show("A number was expected somewhere and couldn't be parsed.\n" + e1.getMessage(),
					"Number Format Exception");
			return;
		} catch (XPathExpressionException e1) {
			ErrorDialog.show("XPath string malformed\nOR\nXML file doesn't contain expected node.\n" + e1.getMessage(),
					"XPath Expression Exception");
			return;
		} catch (DOMException e1) {
			ErrorDialog.show("Couldn't parse the XML to create a Document.\n" + e1.getMessage(), "DOM Exception");
			return;
		} catch (GeneratorRangeException e1) {
			ErrorDialog.show("An unsupported signature length was found in file.", "Generator Range Exception");
			return;
		} catch (BadSignatureException e1) {
			ErrorDialog.show("A malformed (bad characters) signature was found in file.\n" + e1.getSourceMessage(),
					"Bad Signature Exception");
			return;
		}

		// TODO Call builder method in parent Event handler. It will hand back an
		// ArrayList<NyadAbstract>?

		// TODO Validate! If ArrayList length isn't 'Count', inform the user.

		// TODO Deliver nyads one at a time to the Viewer Panel in the calculator to be
		// appended as if created there.

		_parent._GUI.appStatusBar
				.setStatusMsg("File OPEN is not ready yet. Working on XML parser of CladosG objects.\n");
	}

	private void appendFootCardinals(Document pDoc, XPath pX) throws XPathExpressionException {
		for (Foot pt : _foot) {
			StringBuffer test = new StringBuffer(path2FootCardinals);
			int spot = test.indexOf("!");
			test.replace(spot, spot, String.valueOf(_foot.indexOf(pt)));

			XPathExpression expr = pX.compile(test.toString());
			NodeList cardNodes = (NodeList) expr.evaluate(pDoc, XPathConstants.NODESET);
			if (cardNodes == null)
				continue;
			if (cardNodes.getLength() == 0)
				continue;
			for (int m = 0; m < cardNodes.getLength(); m++) {
				Optional<Cardinal> temp = CladosFCache.INSTANCE.findCardinal(cardNodes.item(m).getNodeName());
				if (temp.isPresent()) pt.appendCardinal(temp.get());
			}
		}
	}

	private void buildCardinals(Document pDoc, XPath pX) throws XPathExpressionException {
		XPathExpression expr = pX.compile(path2AllCardinals);
		NodeList cardNodes = (NodeList) expr.evaluate(pDoc, XPathConstants.NODESET);
		for (int k = 0; k < cardNodes.getLength(); k++) {
			Cardinal temp = Cardinal.generate(cardNodes.item(k).getNodeName());
			CladosFCache.INSTANCE.appendCardinal(temp);
		}
	}

	private void buildFoot(Document pDoc, XPath pX) throws XPathExpressionException {
		XPathExpression expr = pX.compile(path2FootNames);
		NodeList footNodes = (NodeList) expr.evaluate(pDoc, XPathConstants.NODESET);
		if (footNodes == null)
			return;
		if (footNodes.getLength() == 0)
			return;

		ArrayList<String> finds = new ArrayList<String>(footNodes.getLength());

		for (int k = 0; k < footNodes.getLength(); k++)
			if (!finds.contains(footNodes.item(k).getNodeValue()))
				finds.add(footNodes.item(k).getNodeValue());
		finds.trimToSize();

		_foot = new ArrayList<Foot>(finds.size());
		for (String pt : finds)
			_foot.add(CladosGBuilder.createFoot(pt, pt));
	}

	private void buildGProducts(Document pDoc, XPath pX)
			throws XPathExpressionException, DOMException, GeneratorRangeException, BadSignatureException {
		XPathExpression expr = pX.compile(path2AllSignatures);
		NodeList sigNodes = (NodeList) expr.evaluate(pDoc, XPathConstants.NODESET);
		for (int k = 0; k < sigNodes.getLength(); k++) {
			CladosGBuilder.INSTANCE.createGProduct(sigNodes.item(k).getNodeValue());
		}
	}

	private void buildTheAlgebras(Document pDoc, XPath pX) throws XPathExpressionException, BadSignatureException {
		XPathExpression expr = pX.compile(path2Algs);
		NodeList algNodes = (NodeList) expr.evaluate(pDoc, XPathConstants.NODESET);
		_algs = new ArrayList<Algebra>(algNodes.getLength());
		for (int j = 0; j < algNodes.getLength(); j++) {
			// For this to work, the Alg child elements must be in order.
			// Name, A Div Field, Foot, GProduct
			// And GProducts, Bases, and Cardinals already exist as objects
			String uuid = algNodes.item(j).getAttributes().getNamedItem("UUID").getTextContent();
			Node name = algNodes.item(j).getFirstChild();
			String name2Use = name.getTextContent();
			boolean testIfPresent = false;
			for (Algebra point : _algs)
				if (point.getAlgebraName() == name2Use) {
					testIfPresent = true;
					break;
				}
			if (testIfPresent)
				break; // Do NOT make another algebra with the same name
			// -------------------
			// New algebra to create because name not found in _algs
			// -------------------
			Node divF = name.getNextSibling();
			String card2Use = divF.getAttributes().getNamedItem("cardinal").getTextContent();
			// We will look up the Cardinal in the Algebra constructor
			Node foot = divF.getNextSibling();
			String foot2Use = foot.getFirstChild().getTextContent();
			// We will find the pre-created foot in a moment
			Node gp = divF.getNextSibling();
			String sig2Use = gp.getFirstChild().getTextContent();
			// We will look up the the GProduct in the Algebra constructor
			int ftIndx = -1;
			for (Foot point : _foot) {
				if (point.getFootName() == foot2Use)
					ftIndx = _foot.indexOf(point);
			}
			if (ftIndx < 0)
				return;
			// ftIndx points at the foot to use

			// FINALLY we build the new algebra and add it to _algs.
			_algs.add(CladosGBuilder.createAlgebraWithFootPlus(_foot.get(ftIndx),
					CladosFCache.INSTANCE.findCardinal(card2Use).get(),
					CladosGCache.INSTANCE.findGProductMap(sig2Use).get(), name2Use));
		}
	}

	private Monad buildAMonad(Node pNode) {

		// For this to work, the Monad child elements must be in order.
		// Name, Algebra, Frame, Coefficients
		// And GProducts, Bases, and Cardinals already exist as objects
		Node name = pNode.getFirstChild();
		String name2Use = name.getTextContent();

		Node alg = name.getNextSibling();

		return null;
	}

	private void buildAllNyads(Document pDoc, XPath pX) throws XPathExpressionException {
		XPathExpression expr = pX.compile(path2Nyads);
		NodeList nyadNodes = (NodeList) expr.evaluate(pDoc, XPathConstants.NODESET);
		_nyads = new ArrayList<Nyad>(nyadNodes.getLength());
		// For this to work, the Nyad child elements must be in order.
		// Name, Foot, AlgebraList, MonadList
		for (int k = 0; k < _nyadCount; k++) {
			Node name = nyadNodes.item(k).getFirstChild();
			String name2Use = name.getTextContent();

			Node foot = name.getNextSibling();
			String foot2Use = foot.getFirstChild().getTextContent();

			Node monadList = foot.getNextSibling().getNextSibling();
			NodeList monads = monadList.getChildNodes();
			int nyadOrder = monads.getLength();
			_monads = new ArrayList<Monad>(nyadOrder);
			for (int j = 0; j < nyadOrder; j++) {
				_monads.add(buildAMonad(monads.item(j)));
			}
		}

	}

	private void findAllAlgebraUUIDs(Document pDoc, XPath pX) throws XPathExpressionException {
		XPathExpression expr = pX.compile(path2AlgUUIDs);
		NodeList UUIDNodes = (NodeList) expr.evaluate(pDoc, XPathConstants.NODESET);
		_algUUIDs = new ArrayList<String>(UUIDNodes.getLength());
		for (int k = 0; k < UUIDNodes.getLength(); k++)
			_algUUIDs.add(UUIDNodes.item(k).getTextContent());
	}

	private void findAllMonadNames(Document pDoc, XPath pX) throws XPathExpressionException {
		XPathExpression expr = pX.compile(path2MonadNames);
		NodeList nameNodes = (NodeList) expr.evaluate(pDoc, XPathConstants.NODESET);
		_monadNames = new ArrayList<String>(nameNodes.getLength());
		for (int k = 0; k < nameNodes.getLength(); k++)
			_monadNames.add(nameNodes.item(k).getTextContent());
	}

	private void findAllNyadNames(Document pDoc, XPath pX) throws XPathExpressionException {
		XPathExpression expr = pX.compile(path2NyadNames);
		NodeList nameNodes = (NodeList) expr.evaluate(pDoc, XPathConstants.NODESET);
		_nyadNames.clear();
		if (nameNodes.getLength() > 0) {
			for (int k = 0; k < nameNodes.getLength(); k++)
				_nyadNames.add(nameNodes.item(k).getTextContent());
			if (nameNodes.getLength() < _nyadCount)
				for (int k = nameNodes.getLength(); k < _nyadCount; k++)
					_nyadNames.add("");
		} else
			for (int k = 0; k < _nyadCount; k++)
				_nyadNames.add("");
	}

	private CladosField findDivField(Document pDoc, XPath pX) throws XPathExpressionException {
		XPathExpression expr = pX.compile(path2DivFields);
		NodeList divFieldNodes = (NodeList) expr.evaluate(pDoc, XPathConstants.NODESET);
		String first = divFieldNodes.item(0).getNodeName();
		if (divFieldNodes.getLength() == 1)
			switch (first) {
			case "RealF":
				return CladosField.REALF;
			case "RealD":
				return CladosField.REALD;
			case "ComplexF":
				return CladosField.COMPLEXF;
			case "ComplexD":
				return CladosField.COMPLEXD;
			default:
				return null;
			}

		for (int k = 1; k < divFieldNodes.getLength(); k++)
			if (first != divFieldNodes.item(k).getNodeName())
				return null;

		switch (first) {
		case "RealF":
			return CladosField.REALF;
		case "RealD":
			return CladosField.REALD;
		case "ComplexF":
			return CladosField.COMPLEXF;
		case "ComplexD":
			return CladosField.COMPLEXD;
		default:
			return null;
		}
	}

	private int findNyadCount(Document pDoc, XPath pX) throws XPathExpressionException {
		XPathExpression expr = pX.compile(path2Nyads);
		NodeList nyadNodes = (NodeList) expr.evaluate(pDoc, XPathConstants.NODESET);
		_nyadCount = nyadNodes.getLength();
		return _nyadCount;
	}
}