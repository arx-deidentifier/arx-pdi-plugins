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

/**
* This class serializes configuration parameters defining the runtime behavior.
* @author Fabian Prasser
* @author Helmut Spengler
*/
public class XMLWriterParametersRuntime extends XMLWriter<ParametersRuntime> {
    
    @Override
    public String write(ParametersRuntime parameters) {
        
        // Write runtime params
        indent(XMLDict.NODE_RUNTIME_PARAMS);
        write(XMLDict.NODE_MODE,         parameters.getMode().name());
        write(XMLDict.NODE_BLOCK_SIZE,   parameters.getBlockSize());
        write(XMLDict.NODE_RECS_PER_IT,  parameters.getRecordsPerIteration());
        write(XMLDict.NODE_MAX_QIS_OPTI, parameters.getMaxQIsOptimal());
        write(XMLDict.NODE_SECS_PER_IT,  parameters.getSecondsPerIteration());
        write(XMLDict.NODE_SSIZE_DS,     parameters.getSnapshotSizeDataset());
        write(XMLDict.NODE_SSIZE_SS,     parameters.getSnapshotSizeSnapshot());
        write(XMLDict.NODE_CACHE_SIZE,   parameters.getCacheSize());
        unindent();
        
        // Return
        return toString();
    }
}
