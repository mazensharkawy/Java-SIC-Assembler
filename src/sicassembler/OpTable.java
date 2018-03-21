/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sicassembler;

import java.util.HashMap;

/**
 *
 * @author ahmed
 */
public class OpTable {
    
    public final HashMap<String, String> opTable = new HashMap<>();
    
    
    public OpTable(){

        opTable.put("ADD", "18");
        opTable.put("ADDF", "58");
        opTable.put("ADDR", "90");
        opTable.put("AND", "40");
        opTable.put("CLEAR", "B4");
        opTable.put("COMP", "28");
        opTable.put("COMPF", "88");
        opTable.put("COMPR", "A0");
        opTable.put("DIV", "24");
        opTable.put("DIVF", "64");
        opTable.put("DIVR", "9C");
        opTable.put("FIX", "C4");
        opTable.put("FLOAT", "C0");
        opTable.put("HIO", "F4");
        opTable.put("J", "3C");
        opTable.put("JEQ", "30");
        opTable.put("JGT", "34");
        opTable.put("JLT", "38");
        opTable.put("JSUB", "48");
        opTable.put("LDA", "00");
        opTable.put("LDB", "68");
        opTable.put("LDCH", "50");
        opTable.put("LDF", "70");
        opTable.put("LDL", "08");
        opTable.put("LDS", "6C");
        opTable.put("LDT", "74");
        opTable.put("LDX", "04");
        opTable.put("LPS", "D0");
        opTable.put("MUL", "20");
        opTable.put("MULR", "60");
        opTable.put("NORM", "98");
        opTable.put("OR", "C8");
        opTable.put("RD", "D8");
        opTable.put("RMO", "A8");
        opTable.put("RSUB", "4C");
        opTable.put("SHIFTL", "A4");
        opTable.put("SHIFTR", "A8");
        opTable.put("SIO", "F0");
        opTable.put("SUBR", "94");
        opTable.put("SVC", "B0");
        opTable.put("TD", "E0");
        opTable.put("TIO", "F8");
        opTable.put("TIX", "2C");
        opTable.put("TIXR", "B8");
        opTable.put("WD", "DC");
        opTable.put("SSK", "EC");
        opTable.put("STA", "0C");
        opTable.put("STB", "78");
        opTable.put("STCH", "54");
        opTable.put("STF", "80");
        opTable.put("STI", "D4");
        opTable.put("STL", "14");
        opTable.put("STS", "7C");
        opTable.put("STSW", "E8");
        opTable.put("STT", "84");
        opTable.put("STX", "10");
        opTable.put("SUB", "1C");
        opTable.put("SUBF", "5C");
}
    
     public boolean contains(String key){
        return opTable.containsKey(key);
    }
    
    
    
    public String getOpCode(String key){
        if(opTable.containsKey(key)){
            return opTable.get(key);
        }
        return null;
    }
    
}
