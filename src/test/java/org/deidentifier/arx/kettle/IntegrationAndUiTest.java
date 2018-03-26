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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.deidentifier.arx.kettle.rrm.ParametersRuntime;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;

public class IntegrationAndUiTest {

	static final String STEP_NAME = "Test Anonymization Step";
	


    /**
     * Create PDI input data.
     * 
     * @param rawData
     * @param fields
     * @return
     */
    public List<RowMetaAndData> generateInputData(String[][] rawData, List<String> fields) {

        List<RowMetaAndData> result = new ArrayList<RowMetaAndData>();
        RowMetaInterface rowMeta = new RowMeta();
        for (String field : fields) {
            rowMeta.addValueMeta(new ValueMetaString(field));
        }

        for ( int i = 0; i < rawData.length; i++ ) {
            Object[] data = new Object[rawData[0].length];
            for (int j = 0; j < rawData[0].length; j++) {
                data = new Object[] { rawData[i][j], rawData[i][1] };
            }
            for (int j = 0; j < rawData[0].length; j++) {
                data[j] = rawData[i][j];
            }
            result.add(new RowMetaAndData(rowMeta, data) );
        }
        return result;
    }

 
    /**
     * Create a kettle transformation suited for testing.
     * 
     * @param mode
     * @param useWcMatch
     * @param highestRisk
     * @param averageRisk
     * @param recordsAtRisk
     * @param qis
     * @param blockSize
     * @return
     */
    private TransMeta createTestTransformation(ParametersRuntime.Mode mode, boolean useWcMatch, double highestRisk, double averageRisk, double recordsAtRisk, Set<String> qis, int blockSize) {
        ReidentificationRiskManagementStepMeta meta = new ReidentificationRiskManagementStepMeta();
        meta.setDefault();
        meta.setFields(TestData.fields1);
        meta.getRiskSettings().setHighestRisk(highestRisk);
        meta.getRiskSettings().setAverageRisk(averageRisk);
        meta.getRiskSettings().setRecordsAtRisk(recordsAtRisk);
        meta.getRiskSettings().setQis(qis);
        meta.getRuntimeSettings().setBlockSize(blockSize);
        meta.getRuntimeSettings().setMode(mode);
        TransMeta tm = TransTestFactory.generateTestTransformation( new Variables(), meta, STEP_NAME );
        return tm;
    }

	@BeforeClass
	public static void setUpBeforeClass() throws KettleException {
		KettleEnvironment.init( false );
	}

	/**
	 * Test the correct behavior for violated thresholds.
	 * 
	 * @throws KettleException
	 */
	public void testNegativeAssessmentResult() throws KettleException {
	    
		TransMeta tm = createTestTransformation(ParametersRuntime.Mode.ASSESS, true, 0.2d, 0.11d, 1d, TestData.qis1, 0);

		List<RowMetaAndData> result = TransTestFactory.executeTestTransformation( tm, TransTestFactory.INJECTOR_STEPNAME,
				STEP_NAME, TransTestFactory.DUMMY_STEPNAME, generateInputData(TestData.ds1, TestData.fields1));
		
		assertEquals(0, result.size());
	}

	/**
	 * Perform an anonymization and check if input and output row numbers match.
	 * 
	 * @throws KettleException
	 */
	@Test
	public void testAnonymization1() throws KettleException {
	    
        TransMeta tm = createTestTransformation(ParametersRuntime.Mode.ANONYMIZE, false, 0.2d, 0.99d, 1d, TestData.qis1, 0);

		List<RowMetaAndData> inputData = generateInputData(TestData.ds1, TestData.fields1);
		inputData.remove(7);
		inputData.remove(6);
		inputData.remove(5);
		inputData.remove(4);
		List<RowMetaAndData> result = TransTestFactory.executeTestTransformation( tm, TransTestFactory.INJECTOR_STEPNAME,
				STEP_NAME, TransTestFactory.DUMMY_STEPNAME, inputData);
		
		assertEquals(inputData.size(), result.size());
	}

	/**
	 * Perform an anonymization and check if input and output row numbers match.
	 * 
	 * @throws KettleException
	 */
	@Test
	public void testAnonymization2() throws KettleException {
	    
		TransMeta tm = createTestTransformation(ParametersRuntime.Mode.ANONYMIZE, false, 0.2d, 0.1d, 1d, TestData.qis1, 0);

		List<RowMetaAndData> inputData = generateInputData(TestData.ds1, TestData.fields1);
		List<RowMetaAndData> result = TransTestFactory.executeTestTransformation( tm, TransTestFactory.INJECTOR_STEPNAME,
				STEP_NAME, TransTestFactory.DUMMY_STEPNAME, inputData);
		
		assertEquals(inputData.size(), result.size());
	}   
}
