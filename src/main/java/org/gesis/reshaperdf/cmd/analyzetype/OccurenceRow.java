package org.gesis.reshaperdf.cmd.analyzetype;

/**
 *
 * @author bensmafx
 */
public class OccurenceRow{

    private String[] arr = null;
    private int total = 0;
    private int alternatives = 0;

  
    public OccurenceRow(String[] literalArr) {
        arr = new String[literalArr.length];
        
        for (int i = 0; i < literalArr.length; i++) {
            arr[i] = literalArr[i];
        }
    }

    public int getPredicateCount() {
        return arr.length;
    }

    public void setLiteral(int index, String value) {
        arr[index] = value;
    }

    public String getLiteral(int index) {
        return arr[index];
    }

    public int getTotal() {
        return total;
    }

    public int getAlternatives() {
        return alternatives;
    }

    public String getKey() {
        String key = "";
        for (int i = 0; i < arr.length; i++) {
            key += arr[i];
        }
        return key;
    }

    public static String generateKey(String[] predicateArr) {
        String key = "";
        for (int i = 0; i < predicateArr.length; i++) {
            key += predicateArr[i];
        }
        return key;
    }

    public void increaseTotal() {
        total++;
    }

    public void increaseAlternatives(int alternateCount) {
        alternatives += alternateCount;
    }

    

   

}
