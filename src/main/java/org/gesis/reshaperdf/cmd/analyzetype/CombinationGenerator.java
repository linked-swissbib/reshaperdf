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
package org.gesis.reshaperdf.cmd.analyzetype;

/**
 *
 * @author Felix Bensmann
 */
public class CombinationGenerator {

    private String[][] inputArr = null;
    private int xDim = -1;
    private int count = -1;
    private boolean terminatedFlag = false;

    private int[] statusArr = null;

    public CombinationGenerator(String[][] inputArr) {
        this.inputArr = inputArr;
        xDim = inputArr.length;

        statusArr = new int[inputArr.length];
        for (int i = 0; i < statusArr.length; i++) {
            statusArr[i] = 0;
        }

        this.count = 1;
        for (int i = 0; i < inputArr.length; i++) {
            this.count *= inputArr[i].length;
        }

    }

    public String[] getCombination() {
        if (!terminatedFlag) {
            String[] retArr = new String[xDim];
            for (int x = 0; x < xDim; x++) {
                retArr[x] = inputArr[x][statusArr[x]];
            }
            return retArr;
        } else {
            return null;
        }
    }

    public boolean next() {
        //set iterator to last index
        int xIdx = xDim - 1;
        boolean carryFlag = true;

        while (carryFlag) {
            carryFlag = false;
            if (statusArr[xIdx] < yDimOf(xIdx) - 1) { //no carry
                statusArr[xIdx]++;
            } else {//carry
                if (xIdx > 0) {
                    statusArr[xIdx] = 0;
                    xIdx--;
                    carryFlag = true;
                } else { //abort, no further combination
                    terminatedFlag = true;
                    return false;
                }
            }
        }
        return true;
    }

    private int yDimOf(int xIdx) {
        return inputArr[xIdx].length;
    }

    public int count() {
        return this.count;
    }

}
