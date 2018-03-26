/*
 * Kettle re-identification risk management step
 * Copyright (C) 2018 TUM/MRI
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.deidentifier.arx.kettle.rrm;

import java.util.HashSet;
import java.util.Set;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;

/**
 * This class encapsulates methods for parsing XML data related parameters relevant for risk management.
 * @author Fabian Prasser
 * @author Helmut Spengler
 */
public class XMLReaderParametersRisk extends XMLReader<ParametersRisk> {

	public ParametersRisk read(Node node) {

		// Read the quasi-identifiers
		Set<String> qis = new HashSet<>();
		Node qisInXml = XMLHandler.getSubNode(node, XMLDict.NODE_QIS);
		int nrQis = XMLHandler.countNodes(qisInXml, XMLDict.NODE_QI);
		for ( int i = 0; i < nrQis; i++ ) {
			Node qiNode = XMLHandler.getSubNodeByNr( qisInXml, XMLDict.NODE_QI, i );
			qis.add(Const.NVL(XMLHandler.getNodeValue(qiNode), ""));
		}

		// Create parameters object
		ParametersRisk riskParams = new ParametersRisk(qis);
		
		// Get thresholds
		Node nodeThresholds = XMLHandler.getSubNode(node, XMLDict.NODE_RISK_PARAMETERS);
		
		// Highest risk
		Node highestRiskInXml = XMLHandler.getSubNode(nodeThresholds, XMLDict.NODE_HIGHEST_RISK);
		riskParams.setHighestRisk(Double.parseDouble(XMLHandler.getNodeValue(highestRiskInXml)));
		
		// Average risk
		Node averageRiskInXml = XMLHandler.getSubNode(nodeThresholds, XMLDict.NODE_AVERAGE_RISK);
		riskParams.setAverageRisk(Double.parseDouble(XMLHandler.getNodeValue(averageRiskInXml)));
		
		// Records at risk
		Node recsAtRiskInXml = XMLHandler.getSubNode(nodeThresholds, XMLDict.NODE_RECORDS_AT_RISK);
		riskParams.setRecordsAtRisk(Double.parseDouble(XMLHandler.getNodeValue(recsAtRiskInXml)));
		
		// Return
		return riskParams;		
	}
}
