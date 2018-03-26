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
 * This class encapsulated parameters determining the runtime behavior. 
 * @author Fabian Prasser
 * @author Helmut Spengler
 *
 */
public class ParametersRuntime {
    
    
    /** 
     * Encapsulates the two working modes of the plugin.
     * @author Helmut Spengler
     */
    public enum Mode {
        ANONYMIZE,
        ASSESS;
    }
    
    /** Default value*/
    public static final Mode    DEFAULT_MODE = Mode.ANONYMIZE;
    /** Default value*/    
    public static final int     DEFAULT_BLOCK_SIZE = 0;
    /** Default value*/
    public static final  double DEFAULT_O_MIN = 0.01d;
    /** Default value*/
    public static final  double DEFAULT_RECORDS_PER_ITERATION = 0.01d;
    /** Default value*/
    public static final  int    DEFAULT_SECONDS_PER_ITERATION = 30;
    /** Default value*/
    public static final  int    DEFAULT_MAX_QIS_OPTIMAL = 15;
    /** Default value*/
    public static final  double DEFAULT_SNAPSHOT_SIZE_DATASET = 0.2d;
    /** Default value*/
    public static final  double DEFAULT_SNAPSHOT_SIZE_SNAPSHOT = 0.8d;
    /** Default value*/
    public static final  int    DEFAULT_CACHE_SIZE = 200;
    
    /** The operation mode of the plugin. */
    private Mode mode = DEFAULT_MODE;
    
    /** The block size used for row blocking. Set to zero to deactivate row-blocking*/
    private int blockSize = DEFAULT_BLOCK_SIZE;

    /** Records per iteration. */
    private double recordsPerIteration = DEFAULT_RECORDS_PER_ITERATION;

    /** The maximum number of QIs with which an optimal anonymization is guaranteed. */
    private int maxQIsOptimal = DEFAULT_MAX_QIS_OPTIMAL;
    
    /** Seconds per iteration. */
    private int secondsPerIteration = DEFAULT_SECONDS_PER_ITERATION;

    /** Maximum size of a snapshot relative to the dataset size (ARX-default is 0.2). */
    private double snapshotSizeDataset = DEFAULT_SNAPSHOT_SIZE_DATASET;

    /** Maximum size of a snapshot relative to the previous snapshot (ARX-default is 0.8). */
    private double snapshotSizeSnapshot = DEFAULT_SNAPSHOT_SIZE_SNAPSHOT;

    /** Maximum number of snapshots allowed to store in the history (ARX-default is 200). */
    private int cacheSize = DEFAULT_CACHE_SIZE;
    
    @Override
    public ParametersRuntime clone() {
        // Clone
        ParametersRuntime result = new ParametersRuntime();
        result.setMode(this.mode);
        result.setBlockSize(this.blockSize);
        result.setRecordsPerIteration(this.recordsPerIteration);
        result.setMaxQIsOptimal(this.maxQIsOptimal);
        result.setSecondsPerIteration(this.secondsPerIteration);
        result.setSnapshotSizeDataset(this.snapshotSizeDataset);
        result.setSnapshotSizeSnapshot(this.snapshotSizeSnapshot);
        result.setCacheSize(this.cacheSize);
        return result;
    }
    
    /**
     * Return the working mode of the plugin.
     * @return
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Set the working mode of the plugin.
     * @param mode
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Return the block size used for row blocking. If it is 0,
     * row blocking is disabled.
     * @see #doRowBlocking()
     * @return
     */
    public int getBlockSize() {
        return blockSize;
    }

    /**
     * Set the block size used for row blocking. Set to 0 for
     * disabling row blocking.
     * @param blockSize
     * @see #doRowBlocking()
     */
    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    /**
     * Return, if row blocking is enabled. This is the case, if the
     * block size is greater than zero.
     * @see #getBlockSize()
     * @see #setBlockSize(int)
     * @return
     */
    public boolean doRowBlocking() {
        return blockSize > 0;
    }


    /**
     * Get the fraction of rows to be processed in each iteration.
     * @return
     */
    public double getRecordsPerIteration() {
        return recordsPerIteration;
    }

    /**
     * Set the fraction of rows to be processed in each iteration
     * @param recordsPerIteration
     */
    public void setRecordsPerIteration(double recordsPerIteration) {
        this.recordsPerIteration = recordsPerIteration;
    }
    
    /**
     * Return the maximal processing time within each iteration.
     * @return
     */
    public int getSecondsPerIteration() {
        return secondsPerIteration;
    }
    
    /**
     * Return the maximal processing time within each iteration.
     * @param secondsPerIteration
     */
    public void setSecondsPerIteration(int secondsPerIteration) {
        this.secondsPerIteration = secondsPerIteration;
    }

    /**
     * If the number of QIs of a dataset is lower than the result of this method,
     * the result is guaranteed to be optimal. Otherwise, a heuristic
     * search strategy is employed after a certain search time.
     * @see #getSecondsPerIteration()
     * @see #setSecondsPerIteration(int)
     * @return
     */
    public int getMaxQIsOptimal() {
        return maxQIsOptimal;
    }

    /**
     * If the number of QIs of a dataset is lower than its parameter,
     * the result is guaranteed to be optimal. Otherwise, a heuristic
     * search strategy is employed after a certain search time.
     * @see #getSecondsPerIteration()
     * @see #setSecondsPerIteration(int)
     * @return
     */
    public void setMaxQIsOptimal(int maxNumberOfAttributesOptimal) {
        this.maxQIsOptimal = maxNumberOfAttributesOptimal;
    }

    /**
     * Get the snapshot size for the dataset.
     * @return
     */
    public double getSnapshotSizeDataset() {
        return snapshotSizeDataset;
    }

    /**
     * Set the snapshot size for the dataset.
     * @param snapshotSizeDataset
     */
    public void setSnapshotSizeDataset(double snapshotSizeDataset) {
        this.snapshotSizeDataset = snapshotSizeDataset;
    }

    /**
     * Get the snapshot size for the snapshot.
     * @return
     */
    public double getSnapshotSizeSnapshot() {
        return snapshotSizeSnapshot;
    }

    /**
     * Set the snapshot size for the snapshot
     * @param snapshotSizeSnapshot
     */
    public void setSnapshotSizeSnapshot(double snapshotSizeSnapshot) {
        this.snapshotSizeSnapshot = snapshotSizeSnapshot;
    }

    /**
     * Get the cache size.
     * @return
     */
    public int getCacheSize() {
        return cacheSize;
    }

    /**
     * Set the cache size.
     * @param cacheSize
     */
    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }
}
