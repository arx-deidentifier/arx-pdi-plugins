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
 * Vocabulary for XML elements. * 
 * @author Helmut Spengler
 */
public class XMLDict {
    /** XML element */
    public static final String NODE_QIS             = "quasi-identifiers";
    /** XML element */
    public static final String NODE_QI              = "quasi-identifier";
    /** XML element */
    public static final String NODE_RISK_PARAMETERS = "risk_parameters";
    /** XML element */
    public static final String NODE_HIGHEST_RISK    = "highest_risk";
    /** XML element */
    public static final String NODE_AVERAGE_RISK    = "average_risk";
    /** XML element */
    public static final String NODE_RECORDS_AT_RISK = "records_at_risk";
    /** XML element */
    public static final String TRUE                 = "true";
    /** XML element */
    public static final String FALSE                = "false";

    /** XML element */
    public static final String NODE_RUNTIME_PARAMS  = "runtime_parameters";
    /** XML element */                              
    public static final String NODE_MODE            = "mode";
    /** XML element */                              
    public static final String NODE_BLOCK_SIZE      = "block_size";
    /** XML element */                              
    public final static String NODE_RECS_PER_IT     = "records_per_iteration";
    /** XML element */                              
    public final static String NODE_MAX_QIS_OPTI    = "max_qis_optimal";
    /** XML element */                              
    public final static String NODE_SECS_PER_IT     = "seconds_per_iteration";
    /** XML element */                              
    public final static String NODE_SSIZE_DS        = "max_snapshot_size_dataset";
    /** XML element */                              
    public final static String NODE_SSIZE_SS        = "max_snapshot_size_snapshot";
    /** XML element */                              
    public final static String NODE_CACHE_SIZE      = "cache_size";
    /** XML element */                              
    public final static String NODE_FIELDS          = "fields";
    /** XML element */                              
    public final static String NODE_FIELD           = "field";
}
