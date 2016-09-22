/*
 * Copyright (C) 2016 GESIS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, see 
 * http://www.gnu.org/licenses/ .
 */
package org.gesis.reshaperdf.utils.sort;

import java.io.File;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.gesis.reshaperdf.utils.StatementsComparatorSPO;

/**
 *
 * @author bensmafx
 */
public class MultithreadMerger {

    private static final int THREADS = 10;
    private final ExecutorService executor;
    private final Map<Integer, File> map;
    private final File outFile;
    private int lastLevel = -1;
    private final File workspace;
    private final AtomicInteger counter;
    private int fileCountdown = -1;
    private int powersOfTwo = -1;

    public MultithreadMerger(int nrOfFiles, File outFile, File workspace) {
        this.outFile = outFile;
        this.workspace = workspace;
        this.fileCountdown = nrOfFiles;
        this.powersOfTwo = nrOfFiles / 2;
        executor = Executors.newFixedThreadPool(THREADS);//creating a pool of THREADS threads    
        map = new ConcurrentHashMap<Integer, File>();
        lastLevel = (int) Math.round(log(nrOfFiles) / log(2));
        counter = new AtomicInteger();
    }

    public synchronized void registerFile(File file, int level) {
        if (level == lastLevel) {  //exit condition
            file.renameTo(outFile);
            executor.shutdown();
            return;
        }
//        fileCountdown--;
//        if (fileCountdown == powersOfTwo) {
//            System.gc();
//            powersOfTwo /= 2;
//        }

        File fileA = map.remove(level); //contains() may not work in multithreading environment
        if (fileA != null) {
            File resultingFile = new File(workspace, "lv" + (level + 1) + "_" + counter.incrementAndGet());
            MergeTask task = new MergeTask(resultingFile, fileA, file, new StatementsComparatorSPO(), this, level);
            executor.execute(task);
        } else {
            map.put(level, file);
        }
    }

    public void waitForIt() throws InterruptedException {
        while (!executor.isTerminated()) {
            Thread.sleep(1000 * 5);
        }

    }

}
