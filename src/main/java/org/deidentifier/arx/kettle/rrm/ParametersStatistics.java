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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pentaho.di.core.exception.KettleException;

/**
 * This class encapsulates the parameters related to statistics. 
 * @author Fabian Prasser
 * @author Helmut Spengler
 *
 */
public class ParametersStatistics {

    /** The number of suppressed cells. */
    private long suppressedCells = 0;
    
    /** The total number of cells. */
    private long totalCells = 0;
    
    /** Milliseconds elapsed since data processing started. */
    private long millis;
    
    /** The risk results for each processed block. */
    private Map<ParametersRisk, Long> risks = new HashMap<>();
    
    /**
     * Return the overall fraction of suppressed cells.
     * 
     * @return
     */
    public double getFractionOfSuppressedCells() {
        return (double) suppressedCells / (double) totalCells;
    }
    
    /**
     * Return aggregated risks over all blocks. For average risk and records at risk,
     * the arithmetic mean is used. For highest risk, the maximum value is used.
     * 
     * @return
     * @throws KettleException 
     */
    public ParametersRisk getRisks() throws KettleException {
        
        // Check
        if (this.risks.isEmpty()) {
            throw new KettleException("No statistics stored");
        }

        // Prepare
        double recordsAtRisk = 0d;
        double highestRisk = 0d;
        double averageRisk = 0d;
        long total = 0;
        for (Long weight : risks.values()) {
            total += weight;
        }
        
        // Calculate average
        for (Entry<ParametersRisk, Long> entry : risks.entrySet()) {
            averageRisk += entry.getKey().getAverageRisk() * entry.getValue();
            highestRisk = Math.max(highestRisk,  entry.getKey().getHighestRisk());
            recordsAtRisk += entry.getKey().getRecordsAtRisk() * entry.getValue();
        }
        recordsAtRisk /= total;
        averageRisk /= total;
        
        // Return
        ParametersRisk result = new ParametersRisk(risks.keySet().iterator().next().getQis());
        result.setRecordsAtRisk(recordsAtRisk);
        result.setHighestRisk(highestRisk);
        result.setAverageRisk(averageRisk);
        return result;
    }

    /**
     * Start timing
     */
    public void startTiming() {
        this.millis = System.currentTimeMillis();
    }
    
    /**
     * Stop timing
     * @return
     */
    public long stopTiming() {
        return System.currentTimeMillis() - millis;
    }
    
    /**
     * Tracks the risks computed for each block.
     * 
     * @param risks
     * @param numRows
     */
    public void trackRisks(ParametersRisk risks, long numRows) {
        this.risks.put(risks, numRows);
    }


    /**
     * Tracks the number of suppressed cells.
     * 
     * @param input
     * @param output
     * @return
     */
    public void trackSuppressedCells(List<String[]> input, List<String[]> output) {
        
        // Skip header
        for (int row = 1; row < output.size(); row++) {
            String[] outArray = output.get(row);
            String[] inArray = input.get(row);
            for (int column = 0; column < outArray.length; column++) {
                suppressedCells += outArray[column].equals(OperationDataTransformer.MAGIC_NULL_VALUE) && 
                                   !inArray[column].equals(OperationDataTransformer.MAGIC_NULL_VALUE)? 1 : 0;
                totalCells++;
            }
        }
    }
}
