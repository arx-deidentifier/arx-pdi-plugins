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
package org.deidentifier.arx.kettle;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.kettle.rrm.ParametersStatistics;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * This class encapsulates state data.
 * 
 * @author Helmut Spengler
 * @author Fabian Prasser
 *
 */
public class ReidentificationRiskManagementStepData extends BaseStepData implements StepDataInterface {
    
     /** The metadata for the outgoing rows. We don't need a member for the incoming rows, since they are identical */
    private RowMetaInterface outputRowMeta;    

    /** The row buffer (intentionally package-private) */
    private List<String[]> buffer;

    /** The statistics collected (intentionally package-private) */
    private ParametersStatistics statistics;
    
    /** The indices for the particular fields in the incoming object array. Needed by OperationDataTransformer  */
    private List<Integer> fieldIndexes;

    /**
     * Constructor.
     */
    public ReidentificationRiskManagementStepData() {
        super();
    }

    /**
     * This method is called from ReidentificationRiskManagementStep.init(), each time a transformation has started.
     */
    public void init() {
        buffer = new ArrayList<String[]>();
        statistics = new ParametersStatistics();
        fieldIndexes = new ArrayList<>(); // fieldIndexes can only be filled with content upon receiving the first row
    }

    /**
     * This method is called from ReidentificationRiskManagementStep.dispose(), each time a transformation has finished.
     */
    public void dispose() {
        buffer = null;
        statistics = null;
        fieldIndexes = null;
    }

    /**
     * Return the buffer.
     * @return
     */
    public List<String[]> getBuffer() {
        return buffer;
    }

    /**
     * Return the statistics.
     * @return
     */
    public ParametersStatistics getStatistics() {
        return statistics;
    }

    /**
     * Return the RowMeta object for the output.
     * @return
     */
    public RowMetaInterface getOutputRowMeta() {
        return outputRowMeta;
    }
    
    /**
     * Set the RowMeta object for the output.
     * @return
     */
    public void setOutputRowMeta(RowMetaInterface rmi) {
        this.outputRowMeta = rmi;
    }

    /**
     * Return the field indexes.
     * @return
     */
    public List<Integer> getFieldIndexes() {
        return fieldIndexes;
    }
}
