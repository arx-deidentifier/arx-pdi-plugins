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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deidentifier.arx.kettle.rrm.OperationCellSuppression;
import org.deidentifier.arx.kettle.rrm.OperationDataTransformer;
import org.deidentifier.arx.kettle.rrm.OperationRiskAssessment;
import org.deidentifier.arx.kettle.rrm.ParametersRisk;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * This class is the implementation of StepInterface.
 * Classes implementing this interface need to:
 * 
 * - initialize the step
 * - execute the row processing logic
 * - dispose of the step 
 * 
 * Please do not create any local fields in a StepInterface class. Store any
 * information related to the processing logic in the supplied step data interface
 * instead.  
 * 
 * @author Helmut Spengler
 * @author Fabian Prasser
 */
public class ReidentificationRiskManagementStep extends BaseStep implements StepInterface {

    /** The access variable to PDI's i18n system */
    private Class<?> PKG = ReidentificationRiskManagementStep.class;

    /**
     * The constructor should simply pass on its arguments to the parent class.
     * 
     * @param s                 step description
     * @param stepDataInterface step data class
     * @param c                 step copy
     * @param t                 transformation description
     * @param dis               transformation executing
     */
    public ReidentificationRiskManagementStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
        super(s, stepDataInterface, c, t, dis);
    }
 
    /**
     * This method is called by PDI during transformation startup. 
     * 
     * It should initialize required for step execution. 
     * 
     * The meta and data implementations passed in can safely be cast
     * to the step's respective implementations. 
     * 
     * It is mandatory that super.init() is called to ensure correct behavior.
     * 
     * Typical tasks executed here are establishing the connection to a database,
     * as wall as obtaining resources, like file handles.
     * 
     * @param smi   step meta interface implementation, containing the step settings
     * @param sdi  step data interface implementation, used to store runtime information
     * 
     * @return true if initialization completed successfully, false if there was an error preventing the step from working. 
     *  
     */
    @Override
    public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
        
        // Casting to step-specific implementation classes is safe
        ReidentificationRiskManagementStepMeta meta = (ReidentificationRiskManagementStepMeta) smi;
        ReidentificationRiskManagementStepData data = (ReidentificationRiskManagementStepData) sdi;
        if ( !super.init( meta, data ) ) {
          return false;
        }

        // Initialize the StepData object.
        data.init();

        return true;
    }

    /**
     * Once the transformation starts executing, the processRow() method is called repeatedly
     * by PDI for as long as it returns true. To indicate that a step has finished processing rows
     * this method must call setOutputDone() and return false;
     * 
     * Steps which process incoming rows typically call getRow() to read a single row from the
     * input stream, change or add row content, call putRow() to pass the changed row on 
     * and return true. If getRow() returns null, no more rows are expected to come in, 
     * and the processRow() implementation calls setOutputDone() and returns false to
     * indicate that it is done too.
     * 
     * Steps which generate rows typically construct a new row Object[] using a call to
     * RowDataUtil.allocateRowData(numberOfFields), add row content, and call putRow() to
     * pass the new row on. Above process may happen in a loop to generate multiple rows,
     * at the end of which processRow() would call setOutputDone() and return false;
     * 
     * @param smi the step meta interface containing the step settings
     * @param sdi the step data interface that should be used to store
     * 
     * @return true to indicate that the function should be called again, false if the step is done
     */
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

        // Safely cast the step settings (meta) and runtime info (data) to specific implementations 
        ReidentificationRiskManagementStepMeta stepMeta = (ReidentificationRiskManagementStepMeta) smi;
        ReidentificationRiskManagementStepData stepData = (ReidentificationRiskManagementStepData) sdi;
        
        // Get incoming row, getRow() potentially blocks waiting for more rows, returns null if no more rows expected
        Object[] r = getRow();
        
        // Handle empty input
        if (r == null && stepData.getBuffer().isEmpty()) {
            return false;
        }

        // If no more rows are expected, indicate step is finished and processRow() should not be called again
        if (r == null || (stepMeta.getRuntimeSettings().doRowBlocking() && stepMeta.getRuntimeSettings().getBlockSize() == stepData.getBuffer().size())) {
            switch (stepMeta.getRuntimeSettings().getMode()) {
            case ANONYMIZE:
                OperationCellSuppression op = new OperationCellSuppression(stepMeta.getRiskSettings(), stepMeta.getRuntimeSettings());
                List<String[]> output = op.perform(stepData.getBuffer());
                stepData.getStatistics().trackSuppressedCells(stepData.getBuffer(), output);
                new OperationDataTransformer().write(this, output, true, stepData);

                // Done
                if (r == null) {
                    logBasic("Fraction of suppressed cells: " + stepData.getStatistics().getFractionOfSuppressedCells());
                    setOutputDone();
                    return false;
                }
                break;
            case ASSESS:
                OperationRiskAssessment assessment = new OperationRiskAssessment(stepMeta.getRiskSettings());
                ParametersRisk risk = assessment.calculate(stepData.getBuffer());
                stepData.getStatistics().trackRisks(risk, stepData.getBuffer().size()-1);

                if (!risk.satisfies(stepMeta.getRiskSettings())) { // Check if risks are fulfilled
                    String errorMessage = BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Message.InvalidThreshold");
                    logError(errorMessage);

                    if (getStepMeta().isDoingErrorHandling()) {
                        new OperationDataTransformer().write(this, stepData.getBuffer(), false, stepData); // HS
                        //                            for (Object[] rowForNextStep : rowBuffer) {
                        putError(getInputRowMeta(), r, 1, errorMessage, null, "RiskThresholds001");
                        //                            }
                    } else {
                        setErrors(1);
                        stopAll();
                        setOutputDone(); // signal end to receiver(s)
                        return false;
                    }
                } else {
                    new OperationDataTransformer().write(this, stepData.getBuffer(), true, stepData); // HS
                }

                // Done
                if (r == null) {
                    log.logBasic(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.HighestRisk") + " = "
                            + risk.getHighestRisk());
                    log.logBasic(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.AverageRisk") + " = "
                            + risk.getAverageRisk());
                    log.logBasic(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.RecordsAtRisk") + " = "
                            + risk.getRecordsAtRisk());
                    setOutputDone();
                    return false;
                }
            }
        }

        // The "first" flag is inherited from the base step implementation
        // it is used to guard some processing tasks, like figuring out field indexes
        // in the row structure that only need to be done once
        if (first) {
            first = false;
           
            // Clone the input row structure and place it in our data object. As the row structure
            // is not changed by our plugin, a subsequent call getFields() isn't necessary
            stepData.setOutputRowMeta(getInputRowMeta().clone());
            
            // Check if at least one QI is specified
            if (stepMeta.getRiskSettings().getQis().size() < 1) {                
                throw new KettleException((BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Message.QiNecessary")));
            }
            
            // For each defined QI field, if it really exists in input
            Set<String> fieldsFromInputStream = new HashSet<String>(Arrays.asList(stepData.getOutputRowMeta().getFieldNames()));
            for (String qi : stepMeta.getRiskSettings().getQis()) {
                if (!fieldsFromInputStream.contains(qi)) {
                    throw new KettleException((BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Message.QiNotInFields")) +  ": '" + qi + "'");
                }
            }
            
            // Initialize the field indexes
            for (String field : stepData.getOutputRowMeta().getFieldNames()) {
                int actualIndex = stepData.getOutputRowMeta().indexOfValue(field);
                stepData.getFieldIndexes().add(actualIndex);
                if (actualIndex < 0) {
                    throw new KettleException(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Message.FieldNotFound: " + field));
                }
            }
            stepData.getBuffer().add(stepData.getOutputRowMeta().getFieldNames());      // Ad the header to the payload; needed by ARX
            stepData.getBuffer().add(new OperationDataTransformer().read(stepData, r)); // Also add the first row!
        } else { // Convert and buffer data
            if (r != null) {
                stepData.getBuffer().add(new OperationDataTransformer().read(stepData, r));
            }
        }
      // Log progress if it is time to to so
      if (checkFeedback(getLinesRead()) ) {
        logBasic(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Message.NumLinesProcessed", getLinesRead())); // Some basic logging
      }

      // indicate that processRow() should be called again
      return true;
    }

    /**
     * This method is called by PDI once the step is done processing. 
     * 
     * The dispose() method is the counterpart to init() and should release any resources
     * acquired for step execution like file handles or database connections.
     * 
     * The meta and data implementations passed in can safely be cast
     * to the step's respective implementations. 
     * 
     * It is mandatory that super.dispose() is called to ensure correct behavior.
     * 
     * @param smi   step meta interface implementation, containing the step settings
     * @param sdi  step data interface implementation, used to store runtime information
     */
    @Override
    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {

        // Casting to step-specific implementation classes is safe
        ReidentificationRiskManagementStepMeta meta = (ReidentificationRiskManagementStepMeta) smi;
        ReidentificationRiskManagementStepData data = (ReidentificationRiskManagementStepData) sdi;

        // Add any step-specific clean up that may be needed here
        data.dispose();
        
        // Call superclass dispose()
        super.dispose( meta, data );
    }
}
