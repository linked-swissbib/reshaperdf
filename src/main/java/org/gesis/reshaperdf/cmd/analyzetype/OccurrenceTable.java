/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gesis.reshaperdf.cmd.analyzetype;

import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author bensmafx
 */
public class OccurrenceTable {

    private Map<String, OccurenceRow> map = null;
    private String name = null;
    private String[] predicateArr = null;
    private String type = null;
   

    public OccurrenceTable(String type, String[] predicateArr, String name) {
        map = new TreeMap<String, OccurenceRow>();
        this.type = type;
        this.name = name;
        this.predicateArr = predicateArr;
    }

    public void addOccurence(String[] predicateArr, int alternateCount) {
        String key = OccurenceRow.generateKey(predicateArr);
        OccurenceRow row = map.get(key);
        if (row == null) {
            row = new OccurenceRow(predicateArr);
            map.put(row.getKey(), row);
        }
        row.increaseTotal();
        row.increaseAlternatives(alternateCount);
    }

    public String getName() {
        return name;
    }

    public void write2File(File file) throws IOException {    
        FileOutputStream fos = new FileOutputStream(file); 
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
        CSVWriter writer = new CSVWriter(osw,';', '"', '\\', "\n");
        String[] nextLine = new String[predicateArr.length + 2]; //+2 for total and alternatives
        for (String elem : map.keySet()) {
            OccurenceRow row = map.get(elem);
            for (int i = 0; i < predicateArr.length; i++) {
                nextLine[i] = row.getLiteral(i);
            }
            nextLine[nextLine.length - 2] = String.valueOf(row.getTotal());
            nextLine[nextLine.length - 1] = String.valueOf(row.getAlternatives());
            writer.writeNext(nextLine);
        }
        writer.close();
        osw.close();
        fos.close();
    }
    
    
    public void write2File(File file, List<OccurenceRow> list) throws IOException {    
        FileOutputStream fos = new FileOutputStream(file); 
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
        CSVWriter writer = new CSVWriter(osw,';', '"', '\\', "\n");
        writer.writeNext(new String[]{this.type});
        writer.writeNext(predicateArr);
        String[] nextLine = new String[predicateArr.length + 2]; //+2 for total and alternatives
        for(int i=0; i<list.size();i++){
            OccurenceRow row = list.get(i);
            for (int j = 0; j < predicateArr.length; j++) {
                nextLine[j] = row.getLiteral(j);
            }
            nextLine[nextLine.length - 2] = String.valueOf(row.getTotal());
            nextLine[nextLine.length - 1] = String.valueOf(row.getAlternatives());
            writer.writeNext(nextLine);
        }
        writer.close();
        osw.close();
        fos.close();
    }
    
    
    public List<OccurenceRow> toSortedList(boolean ascending){
        ArrayList<OccurenceRow>sortedList = new ArrayList<OccurenceRow>(map.size());
        for (String elem : map.keySet()) {
            OccurenceRow row = map.get(elem);
            sortedList.add(row);
        }
        Collections.sort(sortedList, new OccurenceRowComparator(ascending));
        return sortedList;
    }
    
    

}
