/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
*
*******************************************************************************
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
******************************************************************************/

package org.deidentifier.arx.kettle;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deidentifier.arx.kettle.rrm.ParametersRuntime.Mode;
import org.deidentifier.arx.reliability.ParameterTranslation;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

/**
 * 
 * This class is the implementation of StepDialogInterface. Classes implementing
 * this interface need to:
 * 
 * - build and open a SWT dialog displaying the step's settings (stored in the
 * step's meta object) - write back any changes the user makes to the step's
 * meta object - report whether the user changed any settings when confirming
 * the dialog
 * 
 * @author Helmut Spengler
 * @author Fabian Prasser 
 * 
 */
public class ReidentificationRiskManagementStepDialog extends BaseStepDialog
        implements StepDialogInterface {

    /**
     * The PKG member is used when looking up internationalized strings. The
     * properties file with localized keys is expected to reside in {the package
     * of the class specified}/messages/messages_{locale}.properties
     */
    private static Class<?>                        PKG               = ReidentificationRiskManagementStepMeta.class; 

    // This is the object the stores the step's settings
    // The dialog reads the settings from it when opening
    // The dialog writes the settings to it when confirmed
    private ReidentificationRiskManagementStepMeta meta;
    
    /** Value for maximum highest risk */
    private Text                                   wMhr;
    /** The button used for configuring highest risk */
    private Button                                 wBMhr;
    /** Value for maximum highest risk */
    private Text                                   wMar;
    /** Value for records at risk */
    private Text                                   wRar;
    /** The table containing the field definitions */
    private TableView                              wFields;
    /** Information about the particular columns of the table */
    private ColumnInfo[]                           colinf;

    // Tab runtime settings
    /** Value for the mode */
    private CCombo                                 wMode;
    /** Value for records per iteration */
    private Text                                   wRecsPerIt;
    /** Max QIs optimal */
    private Text                                   wMaxQIsOptimal;
    /** Value for optimization timeout */
    private Text                                   wSecsPerIt;
    /** Value for Max. Snapshot size dataset */
    private Text                                   wMaxSsSizeDs;
    /** Value for Max. Snapshot size snapshot */
    private Text                                   wMaxSsSizeSs;
    /** Value for history size */
    private Text                                   wCacheSize;
    /** The value for the block size for row blocking */
    private Text                                   wBlockSize;
    /** The label for input element block size */
    private Label                                  wlBlockSize;
    /** State of the original "meta"*/
    private boolean state;

    /**
     * The constructor should simply invoke super() and save the incoming meta
     * object to a local variable, so it can conveniently read and write
     * settings from/to it.
     * 
     * @param parent
     *            the SWT shell to open the dialog in
     * @param in
     *            the meta object holding the step's settings
     * @param transMeta
     *            transformation description
     * @param sname
     *            the step name
     */
    public ReidentificationRiskManagementStepDialog(Shell parent,
                                                    Object in,
                                                    TransMeta transMeta,
                                                    String sname) {
        super(parent, (BaseStepMeta) in, transMeta, sname);
        meta = (ReidentificationRiskManagementStepMeta) in;
    }

    /**
     * This method is called by Spoon when the user opens the settings dialog of
     * the step. It should open the dialog and return only once the dialog has
     * been closed by the user.
     * 
     * If the user confirms the dialog, the meta object (passed in the
     * constructor) must be updated to reflect the new step settings. The
     * changed flag of the meta object must reflect whether the step
     * configuration was changed by the dialog.
     * 
     * If the user cancels the dialog, the meta object must not be updated, and
     * its changed flag must remain unaltered.
     * 
     * The open() method must return the name of the step after the user has
     * confirmed the dialog, or null if the user cancelled the dialog.
     */
    public String open() {
        // store some convenient SWT variables
        Shell parent = getParent();
        Display display = parent.getDisplay();

        // SWT code for preparing the dialog
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
        props.setLook(shell);
        setShellImage(shell, meta);

        // Save the value of the changed flag on the meta object. If the user
        // cancels
        // the dialog, it will be restored to this saved value.
        // The "changed" variable is inherited from BaseStepDialog
        state = meta.hasChanged();

        // ------------------------------------------------------- //
        // SWT code for building the actual settings dialog //
        // ------------------------------------------------------- //
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;
        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Shell.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Stepname line
        wlStepname = new Label(shell, SWT.RIGHT);
        wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
        props.setLook(wlStepname);
        fdlStepname = new FormData();
        fdlStepname.left = new FormAttachment(0, 0);
        fdlStepname.right = new FormAttachment(middle, -margin);
        fdlStepname.top = new FormAttachment(0, margin);
        wlStepname.setLayoutData(fdlStepname);

        wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wStepname.setText(stepname);
        props.setLook(wStepname);
        fdStepname = new FormData();
        fdStepname.left = new FormAttachment(middle, 0);
        fdStepname.top = new FormAttachment(0, margin);
        fdStepname.right = new FormAttachment(100, 0);
        wStepname.setLayoutData(fdStepname);

        // Create the tab folder and its tabs
        CTabFolder wTabFolder = new CTabFolder(shell, SWT.BORDER);
        props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

        createTabPrivacySettings(wTabFolder, margin);
        createTabRuntimeSettings(wTabFolder, margin);

        FormData fdTabFolder = new FormData();
        fdTabFolder.left = new FormAttachment(0, 0);
        fdTabFolder.top = new FormAttachment(wStepname, margin);
        fdTabFolder.right = new FormAttachment(100, 0);
        fdTabFolder.bottom = new FormAttachment(100, -50);
        wTabFolder.setLayoutData(fdTabFolder);
        wTabFolder.setSelection(0);

        // OK and cancel buttons
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
        setButtonPositions(new Button[] { wOK, wCancel }, margin, wTabFolder);

        // Add listeners for get, cancel and OK
        lsOK = new Listener() {
            public void handleEvent(Event e) {
                actionOk();
            }
        };
        lsGet = new Listener() {
            public void handleEvent(Event e) {
                actionGetFields(meta);
            }
        };
        lsCancel = new Listener() {
            public void handleEvent(Event e) {
                actionCancel();
            }
        };
        // Default for enter
        lsDef = new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                actionOk();
            }
        };
        
        // Set highest risk
        Listener lsSetHr = new Listener() {
            public void handleEvent(Event e) {
                actionSetHighestRisk();
            }
        };

        wOK.addListener(SWT.Selection, lsOK);
        wGet.addListener(SWT.Selection, lsGet);
        wCancel.addListener(SWT.Selection, lsCancel);
        wStepname.addSelectionListener(lsDef);
        wBMhr.addListener(SWT.Selection, lsSetHr);
        
        
        // Detect X or ALT-F4 or something that kills this window and cancel the
        // dialog properly
        shell.addShellListener(new ShellAdapter() {
            public void shellClosed(ShellEvent e) {
                actionCancel();
            }
        });

        // Set/Restore the dialog size based on last position on screen
        // The setSize() method is inherited from BaseStepDialog
        setSize();

        // Populate the dialog with the values from the meta object
        populateDialog();

        // restore the changed flag to original value, as the modify listeners
        // fire during dialog population
        meta.setChanged(changed);

        // open dialog and enter event loop
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        // at this point the dialog has closed, so either ok() or cancel() have
        // been executed
        // The "stepname" variable is inherited from BaseStepDialog
        return stepname;
    }

    /**
     * Called when the user cancels the dialog.
     */
    private void actionCancel() {
        stepname = null; // Setting to null to indicate that dialog was cancelled.
        meta.setChanged(state);  // Restoring original "changed" flag on the met aobject
        dispose();
    }

    /**
     * Get the field definitions from the preceding transformation.
     */
    private void actionGetFields(ReidentificationRiskManagementStepMeta inputMeta) {
        try {
            RowMetaInterface r = transMeta.getPrevStepFields(stepname);
            if (r != null) {
                BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1 }, new int[] {}, 0, 0,
                                                     new TableItemInsertListener() {
                                                         @Override
                                                         public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v) {
                                                             tableItem.setText(2, BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.Yes"));
                                                             return true;
                                                         }
                                                     });
            }
        } catch (KettleException ke) {
            showError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Title"),
                      BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
        }
    }
    
    /**
     * Make sure the highest risk is set to an appropriate value.
     */
    private void actionSetHighestRisk() {
        // Add validator for setting highest risk
        IInputValidator validator = new IInputValidator() {
            @Override
            public String isValid(String arg0) {
                double value = 0d;
                try {
                    value = Double.valueOf(arg0);
                } catch (Exception e) {
                    return "Not a decimal";
                }
                if (value <= 0d || value > 100d) {
                    return "Out of range";
                }
                return null;
            }
        };
        InputDialog wDMhr = new InputDialog(shell,
                    BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.DialogSetHighestRiskHeader"),
                    BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.DialogSetHighestRiskText"),
                    wMhr.getText(), validator);
        if (wDMhr.open() == Window.OK) {
            double percentage = Double.valueOf(wDMhr.getValue());
            wMhr.setText(String.valueOf(ParameterTranslation.getEffectiveRiskThreshold(percentage / 100d) * 100d));
        } 
    }

    /**
     * Called when the user confirms the dialog
     */
    private void actionOk() {

        // Extract data
        String _stepName;
        double highestRisk;
        double averageRisk;
        double recordsAtRisk;
        Mode mode;
        double recordsPerIteration;
        int maxQisOptimal;
        int secondsPerIteration;
        double snapshotSizeDataset;
        double snapshotSizeSnapshot;
        int cacheSize;
        int blockSize;
        List<String> fields;
        Set<String> qis;
        try {
            _stepName = wStepname.getText();
            highestRisk = parsePercentage(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.HighestRisk"), wMhr.getText());
            averageRisk = parsePercentage(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.AverageRisk"), wMar.getText());
            recordsAtRisk = parsePercentage(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.AverageRisk"), wRar.getText());
            mode = Mode.valueOf(wMode.getText());
            recordsPerIteration = parsePercentage(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.RecordsPerIteration"), wRecsPerIt.getText());
            maxQisOptimal = parsePositiveInteger(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.MaxQIsOptimal"), wMaxQIsOptimal.getText(), 1);
            secondsPerIteration = parsePositiveInteger(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.SecondsPerIteration"), wSecsPerIt.getText(), 0);
            snapshotSizeDataset = parsePercentage(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.SnapshotSizeDataset"), wMaxSsSizeDs.getText());
            snapshotSizeSnapshot = parsePercentage(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.SnapshotSizeSnapshot"), wMaxSsSizeSs.getText());
            cacheSize = parsePositiveInteger(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.CacheSize"), wCacheSize.getText(), 0);
            blockSize = parsePositiveInteger(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.BlockSize"), wBlockSize.getText(), 0);
            fields = new ArrayList<>();
            qis = new HashSet<>();
            for (int i = 0; i < wFields.nrNonEmpty(); i++) {
                TableItem item = wFields.getNonEmpty(i);
                String fieldName = item.getText(1);
                fields.add(fieldName);
                if (BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.Yes").equals(item.getText(2))) {
                    qis.add(fieldName);
                }
            }
        } catch (Exception e) {
            showError(BaseMessages.getString( PKG, "ReidentificationRiskManagementStep.ErrorDialog.Title"), e.getMessage());
            return;
        }
        
        if (qis.isEmpty()) {
            showError(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.ErrorDialog.Title"),
                      BaseMessages.getString( PKG, "ReidentificationRiskManagementStep.Message.QiNecessary"));
            return;
        }
        
        // Check if modified
        boolean changed = false;
        changed |= !meta.getFields().equals(fields);
        changed |= !stepname.equals(_stepName); // The "stepname" variable will be the return value for the open()
        changed |= meta.getRiskSettings().getHighestRisk() != highestRisk;
        changed |= meta.getRiskSettings().getAverageRisk() != averageRisk;
        changed |= meta.getRiskSettings().getRecordsAtRisk() != recordsAtRisk;
        changed |= !meta.getRiskSettings().getQis().equals(qis);
        changed |= meta.getRuntimeSettings().getMode() != mode;
        changed |= meta.getRuntimeSettings().getRecordsPerIteration() != recordsPerIteration;
        changed |= meta.getRuntimeSettings().getMaxQIsOptimal() != maxQisOptimal;
        changed |= meta.getRuntimeSettings().getSecondsPerIteration() != secondsPerIteration;
        changed |= meta.getRuntimeSettings().getSnapshotSizeDataset() != snapshotSizeDataset;
        changed |= meta.getRuntimeSettings().getSnapshotSizeSnapshot() != snapshotSizeSnapshot;
        changed |= meta.getRuntimeSettings().getCacheSize() != cacheSize;
        changed |= meta.getRuntimeSettings().getBlockSize() != blockSize;
        
        
        // Change
        if (changed) {
            meta.setFields(fields);
            stepname = _stepName; // The "stepname" variable will be the return value for the open()
            meta.getRiskSettings().setHighestRisk(highestRisk);
            meta.getRiskSettings().setAverageRisk(averageRisk);
            meta.getRiskSettings().setRecordsAtRisk(recordsAtRisk);
            meta.getRiskSettings().setQis(qis);
            meta.getRuntimeSettings().setMode(mode);
            meta.getRuntimeSettings().setRecordsPerIteration(recordsPerIteration);
            meta.getRuntimeSettings().setMaxQIsOptimal(maxQisOptimal);
            meta.getRuntimeSettings().setSecondsPerIteration(secondsPerIteration);
            meta.getRuntimeSettings().setSnapshotSizeDataset(snapshotSizeDataset);
            meta.getRuntimeSettings().setSnapshotSizeSnapshot(snapshotSizeSnapshot);
            meta.getRuntimeSettings().setCacheSize(cacheSize);
            meta.getRuntimeSettings().setBlockSize(blockSize);
            meta.setChanged(true);
        } else {
            meta.setChanged(state);
        }

//        wFields.removeEmptyRows();
//        wFields.setRowNums();
//        wFields.optWidth(true);
        // close the SWT dialog window
        dispose();
    }

    /**
     * Create a tabe
     * @param wPrivSettingsComp 
     * @param top for vertical alignement
     * 
     * @param margin
     * @param vertAlign
     */
    private void createFieldsTable(Composite wPrivSettingsComp, Group top, int margin, int vertAlign) {
        ////////////////////////////////////////////////
        // Key fields
        ////////////////////////////////////////////////
        Label wlKeyFields = new Label(wPrivSettingsComp, SWT.RIGHT);
        wlKeyFields.setText(BaseMessages.getString(PKG,
                                                   "ReidentificationRiskManagementStep.Heading.KeyFields"));
        props.setLook(wlKeyFields);
        FormData fdlKeyFields = new FormData();
        fdlKeyFields.top = new FormAttachment(top, 2 * margin);
        fdlKeyFields.left = new FormAttachment(0, margin);
        fdlKeyFields.width = vertAlign;
        wlKeyFields.setLayoutData(fdlKeyFields);
    
        wGet = new Button(wPrivSettingsComp, SWT.PUSH);
        wGet.setText(BaseMessages.getString(PKG, "System.Button.GetFields"));
        wGet.setEnabled(transMeta.findNrPrevSteps(stepMeta) > 0);
        setButtonPositions(new Button[] { wGet }, margin, null);
    
        ////////////////////////////////////////////////
        // Fields
        ////////////////////////////////////////////////
        colinf = new ColumnInfo[] { new ColumnInfo(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.FieldName"),
                                                   ColumnInfo.COLUMN_TYPE_TEXT,
                                                   false),
                                    new ColumnInfo(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.KeyField"),
                                                   ColumnInfo.COLUMN_TYPE_CCOMBO,
                                                   new String[] { BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.No"),
                                                                  BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.Yes") },
                                                   true)
    
        };
    
        wFields = new TableView(transMeta,
                                wPrivSettingsComp,
                                SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI,
                                colinf,
                                meta.getFields().size(),
                                new ModifyListener() {
                                    @Override
                                    public void modifyText(ModifyEvent arg0) {
                                        // Empty by design
                                    }
                                },
                                props);
    
        FormData fdFields = new FormData();
        fdFields.top = new FormAttachment(top, 3 * margin);
        fdFields.left = new FormAttachment(wlKeyFields, 2 * margin);
        fdFields.right = new FormAttachment(100, 0);
        fdFields.bottom = new FormAttachment(wGet, -margin);
        wFields.setLayoutData(fdFields);
    }
    
    /**
     * Create the tab containing the privacy settings.
     * @param wTabFolder 
     * 
     * @param margin
     */
    private void createTabPrivacySettings(CTabFolder wTabFolder, int margin) {

        // Alignment for the border between labels and input fields
        int vertAlign = 100;

        // Initialize the tab, the composite, and the layout
        CTabItem wPrivSettingsTab = new CTabItem(wTabFolder, SWT.NONE);
        wPrivSettingsTab.setText(BaseMessages.getString(PKG,
                                                        "ReidentificationRiskManagementStep.Heading.RiskThresholds"));

        Composite wPrivSettingsComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wPrivSettingsComp);

        FormLayout privSettingsLayout = new FormLayout();
        privSettingsLayout.marginWidth = 3;
        privSettingsLayout.marginHeight = 3;
        wPrivSettingsComp.setLayout(privSettingsLayout);

        ////////////////////////////////////////////////
        // Group for risk thresholds
        ////////////////////////////////////////////////
        Group wThresholdsGrp = new Group(wPrivSettingsComp, SWT.SHADOW_NONE);
        props.setLook(wThresholdsGrp);
        wThresholdsGrp.setText(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Heading.RiskThresholds"));

        FormLayout groupFileLayout = new FormLayout();
        groupFileLayout.marginWidth = 10;
        groupFileLayout.marginHeight = 10;
        wThresholdsGrp.setLayout(groupFileLayout);

        ////////////////////////////////////////////////
        // Maximum highest risk
        ////////////////////////////////////////////////
        Label wlMhr = new Label(wThresholdsGrp, SWT.RIGHT);
        wlMhr.setText(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.HighestRisk") + " (%)");
        props.setLook(wlMhr);
        FormData fdlMhr = new FormData();
        fdlMhr.top = new FormAttachment(wStepname, 2 * margin);
        fdlMhr.left = new FormAttachment(0, margin);
        fdlMhr.width = vertAlign;
        wlMhr.setLayoutData(fdlMhr);        

        wBMhr = new Button(wThresholdsGrp, SWT.PUSH);
        wBMhr.setText(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.ButtonSetHighestRisk"));
        props.setLook(wBMhr);
        FormData fdlBMhr = new FormData();
        fdlBMhr.top = new FormAttachment(wStepname, 0);
        fdlBMhr.right = new FormAttachment(100, 0);
        fdlBMhr.width = 40; // the width of the button
        wBMhr.setLayoutData(fdlBMhr);

        wMhr = new Text(wThresholdsGrp, SWT.SINGLE | SWT.LEFT | SWT.BORDER | SWT.READ_ONLY);
        props.setLook(wMhr);
        FormData fdMhr = new FormData();
        fdMhr.top = new FormAttachment(wStepname, margin);
        fdMhr.left = new FormAttachment(wlMhr, 2 * margin);
        fdMhr.right = new FormAttachment(wBMhr, -margin);
        wMhr.setLayoutData(fdMhr);

        ////////////////////////////////////////////////
        // Maximum average risk
        ////////////////////////////////////////////////
        Label wlMar = new Label(wThresholdsGrp, SWT.RIGHT);
        wlMar.setText(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.AverageRisk") +
                      " (%)");
        props.setLook(wlMar);
        FormData fdlMar = new FormData();
        fdlMar.top = new FormAttachment(wMhr, 2 * margin);
        fdlMar.left = new FormAttachment(0, margin);
        fdlMar.width = vertAlign;
        wlMar.setLayoutData(fdlMar);

        wMar = new Text(wThresholdsGrp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wMar);
        FormData fdMar = new FormData();
        fdMar.top = new FormAttachment(wMhr, margin);
        fdMar.left = new FormAttachment(wlMar, 2 * margin);
        fdMar.right = new FormAttachment(100, 0);
        wMar.setLayoutData(fdMar);

        ////////////////////////////////////////////////
        // Records at risk
        ////////////////////////////////////////////////
        Label wlRar = new Label(wThresholdsGrp, SWT.RIGHT);
        wlRar.setText(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.RecordsAtRisk") +
                      " (%)");
        props.setLook(wlRar);
        FormData fdlRar = new FormData();
        fdlRar.top = new FormAttachment(wMar, 2 * margin);
        fdlRar.left = new FormAttachment(0, margin);
        fdlRar.width = vertAlign;
        wlRar.setLayoutData(fdlRar);

        wRar = new Text(wThresholdsGrp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wRar);
        FormData fdRar = new FormData();
        fdRar.top = new FormAttachment(wMar, margin);
        fdRar.left = new FormAttachment(wlRar, 2 * margin);
        fdRar.right = new FormAttachment(100, 0);
        wRar.setLayoutData(fdRar);

        ////////////////////////////////////////////////
        // Format the group
        ////////////////////////////////////////////////
        FormData fdThresholdsGrp = new FormData();
        fdThresholdsGrp.left = new FormAttachment(0, margin);
        fdThresholdsGrp.top = new FormAttachment(wMode, 2 * margin);
        fdThresholdsGrp.right = new FormAttachment(100, -margin);
        wThresholdsGrp.setLayoutData(fdThresholdsGrp);

        ////////////////////////////////////////////////
        // The table displaying the QI properties for the fields
        ////////////////////////////////////////////////
        createFieldsTable(wPrivSettingsComp, wThresholdsGrp, margin, vertAlign);

        FormData fdPrivSettingsComp = new FormData();
        fdPrivSettingsComp.left = new FormAttachment(0, 0);
        fdPrivSettingsComp.top = new FormAttachment(0, 0);
        fdPrivSettingsComp.right = new FormAttachment(100, 0);
        fdPrivSettingsComp.bottom = new FormAttachment(100, 0);
        wPrivSettingsComp.setLayoutData(fdPrivSettingsComp);

        wPrivSettingsComp.layout();
        wPrivSettingsTab.setControl(wPrivSettingsComp);

    }

    /**
     * Create the tab containing the runtime settings.
     * @param wTabFolder 
     * 
     * @param margin
     */
    private void createTabRuntimeSettings(CTabFolder wTabFolder, int margin) {

        CTabItem wRtSettingsTab = new CTabItem(wTabFolder, SWT.NONE);
        wRtSettingsTab.setText(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Heading.RuntimeSettings"));

        Composite wRtSettingsComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wRtSettingsComp);

        FormLayout rtSettingsLayout = new FormLayout();
        rtSettingsLayout.marginWidth = 3;
        rtSettingsLayout.marginHeight = 3;
        wRtSettingsComp.setLayout(rtSettingsLayout);

        int vertAlign = 200;

        ////////////////////////////////////////////////
        // Mode
        ////////////////////////////////////////////////
        Label wlMode = new Label(wRtSettingsComp, SWT.RIGHT);
        wlMode.setText(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Heading.Mode"));
        props.setLook(wlMode);
        FormData fdlMode = new FormData();
        fdlMode.top = new FormAttachment(0, 2 * margin);
        fdlMode.left = new FormAttachment(0, margin);
        fdlMode.width = vertAlign + 17;
        wlMode.setLayoutData(fdlMode);

        wMode = new CCombo(wRtSettingsComp, SWT.BORDER | SWT.READ_ONLY);
        props.setLook(wMode);
        // wMode.addModifyListener(lsMod);
        FormData fdMode = new FormData();
        fdMode.top = new FormAttachment(0, margin);
        fdMode.left = new FormAttachment(wlMode, 2 * margin);
        fdMode.right = new FormAttachment(100, 0);
        wMode.setLayoutData(fdMode);
        wMode.setItems(toStringArray(Mode.values()));

        ////////////////////////////////////////////////
        // Group for iteration settings
        ////////////////////////////////////////////////
        Group wIterGrp = new Group(wRtSettingsComp, SWT.SHADOW_NONE);
        props.setLook(wIterGrp);
        wIterGrp.setText(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Heading.Iterations"));

        FormLayout groupFileLayout = new FormLayout();
        groupFileLayout.marginWidth = 10;
        groupFileLayout.marginHeight = 10;
        wIterGrp.setLayout(groupFileLayout);

        ////////////////////////////////////////////////
        // Records per iteration
        ////////////////////////////////////////////////
        Label wlRecsPerIt = new Label(wIterGrp, SWT.RIGHT);
        wlRecsPerIt.setText(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.RecordsPerIteration"));
        props.setLook(wlRecsPerIt);
        FormData fdlRecsPerIt = new FormData();
        fdlRecsPerIt.top = new FormAttachment(wMode, margin);
        fdlRecsPerIt.left = new FormAttachment(0, margin);
        fdlRecsPerIt.width = vertAlign;
        wlRecsPerIt.setLayoutData(fdlRecsPerIt);
        wRecsPerIt = new Text(wIterGrp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wRecsPerIt);
        FormData fdRecsPerIt = new FormData();
        fdRecsPerIt.top = new FormAttachment(wMode, margin);
        fdRecsPerIt.left = new FormAttachment(wlRecsPerIt, margin);
        fdRecsPerIt.right = new FormAttachment(100, 0);
        wRecsPerIt.setLayoutData(fdRecsPerIt);

        ////////////////////////////////////////////////
        // Maximum Number of QIs for optimal anoynmization
        ////////////////////////////////////////////////
        Label wlMaxQisOptimal = new Label(wIterGrp, SWT.RIGHT);
        wlMaxQisOptimal.setText(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.MaxQIsOptimal"));
        props.setLook(wlMaxQisOptimal);
        FormData fdlMaxQIsOptimal = new FormData();
        fdlMaxQIsOptimal.top = new FormAttachment(wRecsPerIt, margin);
        fdlMaxQIsOptimal.left = new FormAttachment(0, margin);
        fdlMaxQIsOptimal.width = vertAlign;
        wlMaxQisOptimal.setLayoutData(fdlMaxQIsOptimal);
        wMaxQIsOptimal = new Text(wIterGrp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wMaxQIsOptimal);
        FormData fdMaxQisOptimal = new FormData();
        fdMaxQisOptimal.top = new FormAttachment(wRecsPerIt, margin);
        fdMaxQisOptimal.left = new FormAttachment(wlMaxQisOptimal, margin);
        fdMaxQisOptimal.right = new FormAttachment(100, 0);
        wMaxQIsOptimal.setLayoutData(fdMaxQisOptimal);

        ////////////////////////////////////////////////
        // Seconds per iteration
        ////////////////////////////////////////////////
        Label wlSecsPerIt = new Label(wIterGrp, SWT.RIGHT); // !
        wlSecsPerIt.setText(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.SecondsPerIteration"));
        props.setLook(wlSecsPerIt);
        FormData fdlSecsPerIt = new FormData();
        fdlSecsPerIt.top = new FormAttachment(wMaxQIsOptimal, margin);
        fdlSecsPerIt.left = new FormAttachment(0, margin);
        fdlSecsPerIt.width = vertAlign;
        wlSecsPerIt.setLayoutData(fdlSecsPerIt);
        wSecsPerIt = new Text(wIterGrp, SWT.SINGLE | SWT.LEFT | SWT.BORDER); // !
        props.setLook(wSecsPerIt);
        FormData fdSecsPerIt = new FormData();
        fdSecsPerIt.top = new FormAttachment(wMaxQIsOptimal, margin);
        fdSecsPerIt.left = new FormAttachment(wlSecsPerIt, margin);
        fdSecsPerIt.right = new FormAttachment(100, 0);
        wSecsPerIt.setLayoutData(fdSecsPerIt);

        ////////////////////////////////////////////////
        // format the group
        ////////////////////////////////////////////////
        FormData fdIterGrp = new FormData();
        fdIterGrp.left = new FormAttachment(0, margin);
        fdIterGrp.top = new FormAttachment(wMode, margin);
        fdIterGrp.right = new FormAttachment(100, -margin);
        wIterGrp.setLayoutData(fdIterGrp);

        ////////////////////////////////////////////////
        // Group for caching
        ////////////////////////////////////////////////
        Group wCachingGrp = new Group(wRtSettingsComp, SWT.SHADOW_NONE);
        props.setLook(wCachingGrp);
        wCachingGrp.setText(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Heading.Caching"));

        groupFileLayout = new FormLayout();
        groupFileLayout.marginWidth = 10;
        groupFileLayout.marginHeight = 10;
        wCachingGrp.setLayout(groupFileLayout);

        ////////////////////////////////////////////////
        // Max. Snapshot size Dataset
        ////////////////////////////////////////////////
        Label wlMaxSsSizeDs = new Label(wCachingGrp, SWT.RIGHT); // !
        wlMaxSsSizeDs.setText(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.SnapshotSizeDataset"));
        props.setLook(wlMaxSsSizeDs);
        FormData fdlMaxSsSizeDs = new FormData();
        fdlMaxSsSizeDs.top = new FormAttachment(wlSecsPerIt, margin);
        fdlMaxSsSizeDs.left = new FormAttachment(0, margin);
        fdlMaxSsSizeDs.width = vertAlign;
        wlMaxSsSizeDs.setLayoutData(fdlMaxSsSizeDs);

        wMaxSsSizeDs = new Text(wCachingGrp, SWT.SINGLE | SWT.LEFT | SWT.BORDER); // !
        props.setLook(wMaxSsSizeDs);
        FormData fdMaxSsSizeDs = new FormData();
        fdMaxSsSizeDs.top = new FormAttachment(wlSecsPerIt, margin);
        fdMaxSsSizeDs.left = new FormAttachment(wlMaxSsSizeDs, margin);
        fdMaxSsSizeDs.right = new FormAttachment(100, 0);
        wMaxSsSizeDs.setLayoutData(fdMaxSsSizeDs);

        ////////////////////////////////////////////////
        // Max. Snapshot size snapshot
        ////////////////////////////////////////////////
        Label wlMaxSsSizeSs = new Label(wCachingGrp, SWT.RIGHT); // !
        wlMaxSsSizeSs.setText(BaseMessages.getString(PKG,  "ReidentificationRiskManagementStep.Label.SnapshotSizeSnapshot"));
        props.setLook(wlMaxSsSizeSs);
        FormData fdlMaxSsSizeSs = new FormData();
        fdlMaxSsSizeSs.top = new FormAttachment(wMaxSsSizeDs, margin);
        fdlMaxSsSizeSs.left = new FormAttachment(0, margin);
        fdlMaxSsSizeSs.width = vertAlign;
        wlMaxSsSizeSs.setLayoutData(fdlMaxSsSizeSs);

        wMaxSsSizeSs = new Text(wCachingGrp, SWT.SINGLE | SWT.LEFT | SWT.BORDER); // !
        props.setLook(wMaxSsSizeSs);
        FormData fdMaxSsSizeSs = new FormData();
        fdMaxSsSizeSs.top = new FormAttachment(wMaxSsSizeDs, margin);
        fdMaxSsSizeSs.left = new FormAttachment(wlMaxSsSizeSs, margin);
        fdMaxSsSizeSs.right = new FormAttachment(100, 0);
        wMaxSsSizeSs.setLayoutData(fdMaxSsSizeSs);

        ////////////////////////////////////////////////
        // History size
        ////////////////////////////////////////////////
        Label wlCacheSize = new Label(wCachingGrp, SWT.RIGHT); // !
        wlCacheSize.setText(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.CacheSize"));
        props.setLook(wlCacheSize);
        FormData fdlCacheSize = new FormData();
        fdlCacheSize.top = new FormAttachment(wMaxSsSizeSs, margin);
        fdlCacheSize.left = new FormAttachment(0, margin);
        fdlCacheSize.width = vertAlign;
        wlCacheSize.setLayoutData(fdlCacheSize);
        wCacheSize = new Text(wCachingGrp, SWT.SINGLE | SWT.LEFT | SWT.BORDER); // !
        props.setLook(wCacheSize);
        FormData fdCacheSize = new FormData();
        fdCacheSize.top = new FormAttachment(wMaxSsSizeSs, margin);
        fdCacheSize.left = new FormAttachment(wlCacheSize, margin);
        fdCacheSize.right = new FormAttachment(100, 0);
        wCacheSize.setLayoutData(fdCacheSize);

        ////////////////////////////////////////////////
        // Format the group
        ////////////////////////////////////////////////
        FormData fdCachingGrp = new FormData();
        fdCachingGrp.left = new FormAttachment(0, margin);
        fdCachingGrp.top = new FormAttachment(wIterGrp, 2 * margin);
        fdCachingGrp.right = new FormAttachment(100, -margin);
        wCachingGrp.setLayoutData(fdCachingGrp);

        ////////////////////////////////////////////////
        // Group for row blocking
        ////////////////////////////////////////////////

        Group wRowBlockingGrp = new Group(wRtSettingsComp, SWT.SHADOW_NONE);
        props.setLook(wRowBlockingGrp);
        wRowBlockingGrp.setText(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Heading.RowBlocking"));

        FormLayout rowBlockGroupLayout = new FormLayout();
        rowBlockGroupLayout.marginWidth = 10;
        rowBlockGroupLayout.marginHeight = 10;
        wRowBlockingGrp.setLayout(rowBlockGroupLayout);

        ////////////////////////////////////////////////
        // Block size
        ////////////////////////////////////////////////
        wlBlockSize = new Label(wRowBlockingGrp, SWT.RIGHT); // !
        wlBlockSize.setText(BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.BlockSize"));
        props.setLook(wlBlockSize);
        FormData fdlBlockSize = new FormData();
        fdlBlockSize.top = new FormAttachment(wCacheSize, margin);
        fdlBlockSize.left = new FormAttachment(0, margin);
        fdlBlockSize.width = vertAlign;
        wlBlockSize.setLayoutData(fdlBlockSize);

        wBlockSize = new Text(wRowBlockingGrp, SWT.SINGLE | SWT.LEFT | SWT.BORDER); // !
        props.setLook(wBlockSize);
        FormData fdBlockSize = new FormData();
        fdBlockSize.top = new FormAttachment(wCacheSize, margin);
        fdBlockSize.left = new FormAttachment(wlBlockSize, margin);
        fdBlockSize.right = new FormAttachment(100, 0);
        wBlockSize.setLayoutData(fdBlockSize);

        ////////////////////////////////////////////////
        // Format the group
        ////////////////////////////////////////////////
        FormData fdRowBlockingGrp = new FormData();
        fdRowBlockingGrp.left = new FormAttachment(0, margin);
        fdRowBlockingGrp.top = new FormAttachment(wCachingGrp, 2 * margin);
        fdRowBlockingGrp.right = new FormAttachment(100, -margin);
        wRowBlockingGrp.setLayoutData(fdRowBlockingGrp);

        wRtSettingsComp.layout();
        wRtSettingsTab.setControl(wRtSettingsComp);

    }

    /**
     * Convert a string containing a percentage specification into a double
     * containing a decimal fraction.
     * @param type
     * @param text
     * 
     * @return
     */
    private double parsePercentage(String type, String text) throws IllegalArgumentException {
        String errorMessage = BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Message.Invalid");
        try {
            double result = Double.parseDouble(text) / 100d;
            if (Double.isInfinite(result) || Double.isNaN(result) || result < 0d || result > 1d) { 
                throw new IllegalArgumentException(errorMessage + " '" + type + "'"); 
            }
            return result;
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(errorMessage + " '" + type + "'");
        }
    }

    /**
     * Parse a String representing a positive integer value.
     * @param type
     * @param text
     * @param lowerBound valid values must be equal or higher
     * @return
     */
    private int parsePositiveInteger(String type, String text, int lowerBound) throws IllegalArgumentException {
        String errorMessage = BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Message.Invalid");
        int i;
        try {
            i = Integer.parseInt(text);
        } catch (RuntimeException nfe) {
            throw new IllegalArgumentException(errorMessage + " '" + type + "'");
        }
        if (i < lowerBound) {
            throw new IllegalArgumentException(errorMessage + " '" + type + "'");
        }
        return i;
    }

    /**
     * This helper method puts the step configuration stored in the meta object
     * and puts it into the dialog controls.
     */
    private void populateDialog() {

        wStepname.setText(stepname);

        for (String field : meta.getFields()) {

            TableItem item = new TableItem(wFields.table, SWT.NONE);
            item.setText(1, Const.NVL(field, ""));
            item.setText(2,
                         Const.NVL(meta.getRiskSettings().isQi(field)
                                 ? BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.Yes")
                                 : BaseMessages.getString(PKG, "ReidentificationRiskManagementStep.Label.No"), ""));
        }

        wMode.setText(meta.getRuntimeSettings().getMode().name());

        wMhr.setText(String.valueOf(meta.getRiskSettings().getHighestRisk()   * 100d));
        wMar.setText(String.valueOf(meta.getRiskSettings().getAverageRisk()   * 100d));
        wRar.setText(String.valueOf(meta.getRiskSettings().getRecordsAtRisk() * 100d));

        wMaxQIsOptimal.setText(String.valueOf(meta.getRuntimeSettings().getMaxQIsOptimal()));
        wRecsPerIt.setText(String.valueOf(meta.getRuntimeSettings().getRecordsPerIteration() * 100d));
        wSecsPerIt.setText(String.valueOf(meta.getRuntimeSettings().getSecondsPerIteration()));

        wMaxSsSizeDs.setText(String.valueOf(meta.getRuntimeSettings().getSnapshotSizeDataset() * 100d));
        wMaxSsSizeSs.setText(String.valueOf(meta.getRuntimeSettings().getSnapshotSizeSnapshot() * 100d));
        wCacheSize.setText(String.valueOf(meta.getRuntimeSettings().getCacheSize()));

        wBlockSize.setText(String.valueOf(meta.getRuntimeSettings().getBlockSize()));

        wFields.removeEmptyRows();
        wFields.setRowNums();
        wFields.optWidth(true);

        wStepname.selectAll();
        wStepname.setFocus();
    }

    /**
     * Shows an error message
     * @param title
     * @param message
     */
    private void showError(String title, String message) {
        MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        dialog.setText(title);
        dialog.setMessage(message);
        dialog.open();
    }

    /**
     * Return the display names for the modes.
     * 
     * @param values
     * @return
     */
    private String[] toStringArray(Mode[] values) {
        List<String> names = new ArrayList<>();
        for (Mode mode : values) {
            names.add(mode.name());
        }
        return names.toArray(new String[names.size()]);
    }
}
