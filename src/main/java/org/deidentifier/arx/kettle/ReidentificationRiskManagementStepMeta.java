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
import java.util.Map;

import org.deidentifier.arx.kettle.rrm.ParametersRisk;
import org.deidentifier.arx.kettle.rrm.ParametersRuntime;
import org.deidentifier.arx.kettle.rrm.XMLDict;
import org.deidentifier.arx.kettle.rrm.XMLReaderParametersRisk;
import org.deidentifier.arx.kettle.rrm.XMLReaderParametersRuntime;
import org.deidentifier.arx.kettle.rrm.XMLWriterParametersRisk;
import org.deidentifier.arx.kettle.rrm.XMLWriterParametersRuntime;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**  
 * This class is an implementation of StepMetaInterface.
 * Classes implementing this interface need to:
 * 
 * - keep track of the step settings
 * - serialize step settings both to xml and a repository
 * - provide new instances of objects implementing StepDialogInterface, StepInterface and StepDataInterface
 * - report on how the step modifies the meta-data of the row-stream (row structure and field types)
 * - perform a sanity-check on the settings provided by the user 
 * 
 * @author Helmut Spengler
 * @author Fabian Prasser
 */
@Step(
      id = "ReidentificationRiskManagementStep",
      name = "ReidentificationRiskManagementStep.Step.Name",
      description = "ReidentificationRiskManagementStep.Job.Description", 
      image = "org/deidentifier/arx/kettle/resources/arx-logo.svg", 
      categoryDescription = "ReidentificationRiskManagementStep.Category",
      i18nPackageName = "org.deidentifier.arx.kettle",
      documentationUrl = "ReidentificationRiskManagementStep.Url.Documentation",
      casesUrl = "ReidentificationRiskManagementStep.Url.Cases",
      forumUrl = "ReidentificationRiskManagementStep.Url.Forum"
        )
public class ReidentificationRiskManagementStepMeta extends BaseStepMeta implements StepMetaInterface {

    /** The PKG member is used when looking up internationalized strings.
     *  The properties file with localized keys is expected to reside in 
     *  {the package of the class specified}/messages/messages_{locale}.properties  */
    private static final Class<?> PKG = ReidentificationRiskManagementStepMeta.class; // for i18n purposes

    /** The names of the fields handled by this instance of the step.*/
    private List <String> fields;
    /** The parameters related to risk management */
    private ParametersRisk riskSettings;
    /** The parameters determining the runtime behavior */
    private ParametersRuntime runtimeSettings;

    /**
     * Called by Spoon to get a new instance of the SWT dialog for the step.
     * A standard implementation passing the arguments to the constructor of the step dialog is recommended.
     * 
     * @param shell    an SWT Shell
     * @param meta     description of the step 
     * @param transMeta  description of the the transformation 
     * @param name    the name of the step
     * @return       new instance of a dialog for this step 
     */
    public StepDialogInterface getDialog( Shell shell, StepMetaInterface meta, TransMeta transMeta, String name ) {
        return new ReidentificationRiskManagementStepDialog( shell, meta, transMeta, name );
    }

    /**
     * Called by PDI to get a new instance of the step implementation. 
     * A standard implementation passing the arguments to the constructor of the step class is recommended.
     * 
     * @param stepMeta        description of the step
     * @param stepDataInterface    instance of a step data class
     * @param cnr          copy number
     * @param transMeta        description of the transformation
     * @param disp          runtime implementation of the transformation
     * @return            the new instance of a step implementation 
     */
    @Override
    public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp ) {
        return new ReidentificationRiskManagementStep( stepMeta, stepDataInterface, cnr, transMeta, disp );
    }

    @Override
    public boolean supportsErrorHandling() {
        return true;
    }

    /**
     * Called by PDI to get a new instance of the step data class.
     */
    @Override
    public StepDataInterface getStepData() {
        return new ReidentificationRiskManagementStepData();
    }

    /**
     * This method is called every time a new step is created and should allocate/set the step configuration
     * to sensible defaults. The values set here will be used by Spoon when a new step is created.    
     */
    @Override
    public void setDefault() {
        fields = new ArrayList<>();
        riskSettings = new ParametersRisk();
        runtimeSettings = new ParametersRuntime();
    }

    /**
     * This method is used when a step is duplicated in Spoon. It needs to return a deep copy of this
     * step meta object. Be sure to create proper deep copies if the step configuration is stored in
     * modifiable objects.
     * 
     * See org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta.clone() for an example on creating
     * a deep copy.
     * 
     * @return a deep copy of this
     */
    @Override
    public Object clone() {
        ReidentificationRiskManagementStepMeta result = (ReidentificationRiskManagementStepMeta) super.clone();
        result.fields      = new ArrayList<>(this.fields);
        result.riskSettings    = this.riskSettings.clone();
        result.runtimeSettings = this.runtimeSettings.clone();
        return result;
    }

    /**
     * This method is called by Spoon when a step needs to serialize its configuration to XML. The expected
     * return value is an XML fragment consisting of one or more XML tags.  
     * 
     * Please use org.pentaho.di.core.xml.XMLHandler to conveniently generate the XML.
     * 
     * @return a string containing the XML serialization of this step
     */
    @Override
    public String getXML() throws KettleException {
        StringBuilder result = new StringBuilder();

        // Fields
        result.append("<" + XMLDict.NODE_FIELDS + ">");
        for (String field : fields) {
            result.append("<" + XMLDict.NODE_FIELD + ">" + field + "</" + XMLDict.NODE_FIELD + ">");
        }
        result.append("</" + XMLDict.NODE_FIELDS + ">");

        // Risk- and runtime-settings
        result.append(new XMLWriterParametersRisk().write(riskSettings));
        result.append(new XMLWriterParametersRuntime().write(runtimeSettings));

        return result.toString();
    }
    /**
     * This method is called by PDI when a step needs to load its configuration from XML.
     * 
     * Please use org.pentaho.di.core.xml.XMLHandler to conveniently read from the
     * XML node passed in.
     * 
     * @param stepnode  the XML node containing the configuration
     * @param databases  the databases available in the transformation
     * @param metaStore the metaStore to optionally read from
     */
    @Override
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {

        try {
            // Load fields
            fields = new ArrayList<String>();
            Node fieldsInXml = XMLHandler.getSubNode(stepnode, XMLDict.NODE_FIELDS);
            int nrFields = XMLHandler.countNodes(fieldsInXml, XMLDict.NODE_FIELD);
            for ( int i = 0; i < nrFields; i++ ) {
                Node fieldNode = XMLHandler.getSubNodeByNr(fieldsInXml, XMLDict.NODE_FIELD, i, false);
                fields.add(Const.NVL(XMLHandler.getNodeValue(fieldNode), ""));
            }

            // Load risk related parameters
            XMLReaderParametersRuntime readerRuntime = new XMLReaderParametersRuntime();
            runtimeSettings = readerRuntime.read(stepnode);

            // Load parameters determining the runtime behavior
            XMLReaderParametersRisk readerRisk = new XMLReaderParametersRisk();
            riskSettings = readerRisk.read(stepnode);
        } catch ( Exception e ) {
            throw new KettleXMLException( "Unable to read step info from XML node", e ); // TODO i18nize?
        }
    }

    /**
     * This method is called by Spoon when a step needs to serialize its configuration to a repository.
     * The repository implementation provides the necessary methods to save the step attributes.
     *
     * @param rep                 the repository to save to
     * @param metaStore           the metaStore to optionally write to
     * @param id_transformation   the id to use for the transformation when saving
     * @param id_step             the id to use for the step  when saving
     */
    public void saveRep(Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step)
            throws KettleException {
        try {
            //        rep.saveStepAttribute( id_transformation, id_step, "outputfield", outputField ); //$NON-NLS-1$ // TODO implement saveRep() ?
        } catch ( Exception e ) {
            throw new KettleException( "Unable to save step into repository: " + id_step, e );
        }
    }

    /**
     * This method is called by PDI when a step needs to read its configuration from a repository.
     * The repository implementation provides the necessary methods to read the step attributes.
     * 
     * @param rep        the repository to read from
     * @param metaStore  the metaStore to optionally read from
     * @param id_step    the id of the step being read
     * @param databases  the databases available in the transformation
     */
    public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
            throws KettleException {
        try {
            //        outputField  = rep.getStepAttributeString( id_step, "outputfield" ); //$NON-NLS-1$ // TODO implement readRep()?
        } catch ( Exception e ) {
            throw new KettleException("Unable to load step from repository", e);
        }
    }

    /**
     * This method is called to determine the changes the step is making to the row-stream.
     * To that end a RowMetaInterface object is passed in, containing the row-stream structure as it is when entering
     * the step. This method must apply any changes the step makes to the row stream. Usually a step adds fields to the
     * row-stream.
     * 
     * @param inputRowMeta    the row structure coming in to the step
     * @param name         the name of the step making the changes
     * @param info        row structures of any info steps coming in
     * @param nextStep      the description of a step this step is passing rows to
     * @param space        the variable space for resolving variables
     * @param repository    the repository instance optionally read from
     * @param metaStore      the metaStore to optionally read from
     */
    public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
                           VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

        /*
         * This implementation does not change the field definitions, therefore nothing happens here.
         */
    }

    /**
     * This method is called when the user selects the "Verify Transformation" option in Spoon. 
     * A list of remarks is passed in that this method should add to. Each remark is a comment, warning, error, or ok.
     * The method should perform as many checks as necessary to catch design-time errors.
     * 
     * Typical checks include:
     * - verify that all mandatory configuration is given
     * - verify that the step receives any input, unless it's a row generating step
     * - verify that the step does not receive any input if it does not take them into account
     * - verify that the step finds fields it relies on in the row-stream
     * 
     *   @param remarks    the list of remarks to append to
     *   @param transMeta  the description of the transformation
     *   @param stepMeta  the description of the step
     *   @param prev      the structure of the incoming row-stream
     *   @param input      names of steps sending input to the step
     *   @param output    names of steps this step is sending output to
     *   @param info      fields coming in from info steps 
     *   @param metaStore  metaStore to optionally read from
     */
    public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
                       String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
                       IMetaStore metaStore ) {
        CheckResult cr;

        // See if there are input streams leading to this step!
        if ( input != null && input.length > 0 ) {
            cr = new CheckResult( CheckResult.TYPE_RESULT_OK,
                                  BaseMessages.getString( PKG, "ReidentificationRiskManagementStep.Message.ReceivingRows.OK" ), stepMeta );
            remarks.add( cr );
        } else {
            cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR,
                                  BaseMessages.getString( PKG, "ReidentificationRiskManagementStep.Message.ReceivingRows.ERROR" ), stepMeta );
            remarks.add( cr );
        }
    }

    /**
     * Return the field names handled by this step.
     * 
     * @return
     */
    public List<String> getFields() {
        return fields;
    }

    /**
     * Set the field names handled by this step.
     * 
     * @param fieldNames
     */
    public void setFields(List<String> fieldNames) {
        this.fields = fieldNames;
    }

    /**
     * Return the risk settings.
     * @return
     */
    public ParametersRisk getRiskSettings() {
        return riskSettings;
    }

    /**
     * Return the runtime settings.
     * @return
     */
    public ParametersRuntime getRuntimeSettings() {
        return runtimeSettings;
    }
}
