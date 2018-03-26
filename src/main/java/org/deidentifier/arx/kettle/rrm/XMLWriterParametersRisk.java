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
 * This class serializes configuration parameters related to risk mananagement.
 * @author Fabian Prasser
 * @author Helmut Spengler
 */
public class XMLWriterParametersRisk extends XMLWriter<ParametersRisk> {    
    
    @Override
    public String write(ParametersRisk parameters) {
        
        // Write QIs
        indent(XMLDict.NODE_QIS);
        for (String qi: parameters.getQis()) {
            write(XMLDict.NODE_QI, qi);
        };
        unindent();
        
        // Write thresholds
        indent(XMLDict.NODE_RISK_PARAMETERS);
        write(XMLDict.NODE_HIGHEST_RISK,    parameters.getHighestRisk());
        write(XMLDict.NODE_AVERAGE_RISK,    parameters.getAverageRisk());
        write(XMLDict.NODE_RECORDS_AT_RISK, parameters.getRecordsAtRisk());
        unindent();
        
        // Return
        return toString();
    }
}
