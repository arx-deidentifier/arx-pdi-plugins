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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.kettle.ReidentificationRiskManagementStep;
import org.deidentifier.arx.kettle.ReidentificationRiskManagementStepData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.BaseStep;

/**
 * This class encapsulates methods for data transformation. 
 * @author Fabian Prasser
 * @author Helmut Spengler
 *
 */
public class OperationDataTransformer {

    /** The String representation for NULL values */
    public final static String MAGIC_NULL_VALUE = "_KETTLE_SPOON_NULL_";

    /**
     * Creates an empty dataset containing null values in all cells.
     * 
     * @param cols number of columns
     * @param rows number of rows
     * @param header header
     * @return
     */
    public List<String[]> getEmptyDataset(String[] header, int cols, int rows) {
        List<String[]> ret = new ArrayList<>();

        ret.add(header);

        for (int i = 0; i < rows; i++) {
            String[] row = new String[cols];
            for (int j = 0; j < cols; j++) {
                row[j] = MAGIC_NULL_VALUE;
            }
            ret.add(row);
        }
        return ret;
    }

    /**
     * Convert data coming from the previous step of the transformation to internal format.
     * 
     * @param stepData
     * @param row
     * @return
     * @throws KettleValueException
     */
    public String[] read(ReidentificationRiskManagementStepData stepData, Object[] row) throws KettleValueException {
        String[] result = new String[stepData.getFieldIndexes().size()];
        for (int i = 0; i < result.length; i++) {
            String string = stepData.getOutputRowMeta().getString(row, stepData.getFieldIndexes().get(i));
            result[i] = string != null ? string : MAGIC_NULL_VALUE;
        }
        return result;
    }

    /**
     * Convert/prepare internal data and pass it to the next step of the transformation
     * if regularOutput==true. Else, pass all data to the error channel.
     * 
     * @param step
     * @param payload
     * @param regularOutput whether the payload is routed to the next step or to an error destination
     * @param stepData
     * @throws KettleException
     */
    public void write(BaseStep step, List<String[]> payload, boolean regularOutput, ReidentificationRiskManagementStepData stepData) throws KettleException {

        // Skip header
        for (int i=1; i<payload.size(); i++) {

            String[] rowFromBuffer = payload.get(i);
            Object[] outputRow = RowDataUtil.allocateRowData(stepData.getOutputRowMeta().size());

            for (int j = 0; j < rowFromBuffer.length; j++) {
                String value = rowFromBuffer[j];
                if (value == null || value.equals(OperationDataTransformer.MAGIC_NULL_VALUE) || value.equals(DataType.ANY_VALUE) || value.equals(DataType.NULL_VALUE)) {
                    outputRow[stepData.getFieldIndexes().get(j)] = null;
                } else {
                    outputRow[stepData.getFieldIndexes().get(j)] = value.getBytes();
                }
            }
            if (regularOutput) {
                step.putRow(stepData.getOutputRowMeta(), outputRow);
            } else {                
                step.putError(stepData.getOutputRowMeta(), outputRow, 1, BaseMessages.getString(ReidentificationRiskManagementStep.class, "ReidentificationRiskManagementStep.Message.InvalidThreshold"), null, "RRM001");
            }
        }
    }

    /**
     * Converts a handle to a list of strings
     * @param header
     * @param output
     * @return
     */
    public List<String[]> convert(DataHandle input, DataHandle output) {
        if (output != null) {
            List<String[]> result = new ArrayList<>();
            for (Iterator<String[]> iterator = output.iterator(); iterator.hasNext(); ) {
                result.add(iterator.next());
            }
            return result;
        } else {
            return getEmptyDataset(input.iterator().next(), input.getNumColumns(), input.getNumRows());
        }
    }
}
