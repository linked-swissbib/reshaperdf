/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gesis.reshaperdf.cmd.analyzetype;

/**
 *
 * @author bensmafx
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
