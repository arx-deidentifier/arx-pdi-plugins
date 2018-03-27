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

import org.deidentifier.arx.kettle.rrm.ParametersRuntime.Mode;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;

/**
 * This class encapsulates methods for parsing XML data related to runtime parameters.
 * @author Fabian Prasser
 * @author Helmut Spengler
 */
public class XMLReaderParametersRuntime extends XMLReader<ParametersRuntime> {

    @Override
    public ParametersRuntime read(Node node) {
        
        // Create parameters object and get root node for relevant config section
        ParametersRuntime runtimeParams = new ParametersRuntime();
        Node nodeRuntimeParams = XMLHandler.getSubNode(node, XMLDict.NODE_RUNTIME_PARAMS);
        
        // Mode
        Node mode = XMLHandler.getSubNode(nodeRuntimeParams, XMLDict.NODE_MODE);
        runtimeParams.setMode(Mode.valueOf(XMLHandler.getNodeValue(mode)));
        
        // BlockSize
        Node blockSize = XMLHandler.getSubNode(nodeRuntimeParams, XMLDict.NODE_BLOCK_SIZE);
        runtimeParams.setBlockSize(Integer.parseInt(XMLHandler.getNodeValue(blockSize)));
        
        // RecordsPerIteration
        Node recordsPerIteration = XMLHandler.getSubNode(nodeRuntimeParams, XMLDict.NODE_RECS_PER_IT);
        runtimeParams.setRecordsPerIteration(Double.parseDouble(XMLHandler.getNodeValue(recordsPerIteration)));
        
        // MaxQIsOptimal
        Node maxQIsOptimal = XMLHandler.getSubNode(nodeRuntimeParams, XMLDict.NODE_MAX_QIS_OPTI);
        runtimeParams.setMaxQIsOptimal(Integer.parseInt(XMLHandler.getNodeValue(maxQIsOptimal)));
        
        // SecondsPerIteration
        Node secondsPerIteration = XMLHandler.getSubNode(nodeRuntimeParams, XMLDict.NODE_SECS_PER_IT);
        runtimeParams.setSecondsPerIteration(Integer.parseInt(XMLHandler.getNodeValue(secondsPerIteration)));
        
        // SnapshotSizeDataset
        Node snapshotSizeDataset = XMLHandler.getSubNode(nodeRuntimeParams, XMLDict.NODE_SSIZE_DS);
        runtimeParams.setSnapshotSizeDataset(Double.parseDouble(XMLHandler.getNodeValue(snapshotSizeDataset)));
        
        // SnapshotSizeSnapshot
        Node snapshotSizeSnapshot = XMLHandler.getSubNode(nodeRuntimeParams, XMLDict.NODE_SSIZE_SS);
        runtimeParams.setSnapshotSizeSnapshot(Double.parseDouble(XMLHandler.getNodeValue(snapshotSizeSnapshot)));
        
        // CacheSize
        Node cacheSize = XMLHandler.getSubNode(nodeRuntimeParams, XMLDict.NODE_CACHE_SIZE);
        runtimeParams.setCacheSize(Integer.parseInt(XMLHandler.getNodeValue(cacheSize)));

        // Return
        return runtimeParams;
    }
}
