package sicassembler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ahmed
 */
public class FileOperator {

    public int locationCounter;
    public int startingAddress;
    public int programLength;
    public ArrayList<Instruction> instructions = new ArrayList<>();

    public HashMap<String, Integer> symTable = new HashMap<>();
    public String programName;
    public StringBuilder writer;
    private OpTable opTable = new OpTable();

    public void executeAssemblerPassOne(String filePath) {

        FileReader fileReader = null;
        String[] tokens;
        try {
            File file = new File(filePath);
            fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
            String newLine = null;

            do {
                newLine = reader.readLine();
                parseFirstLine(newLine);
            } while (newLine.startsWith("#"));

            while ((newLine = reader.readLine()) != null && !startWith(newLine, "END")) {
                if (newLine.startsWith("#")) {
                    continue;
                }
                if (isValidAssemblyLine(newLine)) {
                    Instruction newInstruction = parseLine(newLine);
                    instructions.add(newInstruction);
                    System.out.println(newInstruction);

                    //System.out.println(locationCounter+"    "+newLine);
                }
            }
            programLength = locationCounter - startingAddress;

            reader.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileOperator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
        } finally {
            try {
                fileReader.close();
            } catch (IOException ex) {
                Logger.getLogger(FileOperator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    
    public void executeAssemblerPassTwo(){
        OpTable opCodes = new OpTable();
        writer=new StringBuilder();
        writer.append("H").append(programName).append(" ")
                .append(Integer.toHexString(startingAddress)).append(Integer.toHexString(programLength));
        int t =30;
        for(Instruction instruction : instructions ){
            if(t==30){
                t=0;
                writer.append("\n");
                writer.append("T").append(Integer.toHexString(instruction.getLocation()));
                if (instructions.get(instructions.size() - 1).getLocation() > instruction.getLocation() + 30) {
                    writer.append("1E");
                } else if(instructions.get(instructions.size()-1).getLocation() > instruction.getLocation() +15){
                    writer.append(Integer.toHexString(instructions.get(instructions.size()-1).getLocation()-instruction.getLocation()));
                }
                else{
                    writer.append(0);
                    writer.append(Integer.toHexString(instructions.get(instructions.size()-1).getLocation()-instruction.getLocation()));   
                }
            }
            System.out.println(instruction.getOperand());
            if(instruction.getMnemonic()!=null) writer.append(opCodes.getOpCode(instruction.getMnemonic()));
            
            if(instruction.getOperand() == null) {
                writer.append(0000);
            }
            else if(instruction.getOperand().charAt(0)>= '0' && instruction.getOperand().charAt(0)<= '9' ){
                String hexaOperand = Integer.toHexString(Integer.parseInt(instruction.getOperand()));
                for(int i =0;i< 4- hexaOperand.length();i++){writer.append(0);}
                writer.append(hexaOperand);
            }
            else if(symTable.get(instruction.getOperand())!=null){
               writer.append(symTable.get(instruction.getOperand()));
            }
            else if(instruction.getOperand().substring(0, 2).equalsIgnoreCase("x'") 
                    && instruction.getOperand().charAt(instruction.getOperand().length()-1)=='\''){
                writer.append(instruction.getOperand().substring(2,instruction.getOperand().length()));
            }
            else if(instruction.getOperand().substring(0, 2).equalsIgnoreCase("c'") 
                    && instruction.getOperand().charAt(instruction.getOperand().length()-1)=='\''){
                for(char c : instruction.getOperand().substring(2,instruction.getOperand().length()).toCharArray()){
                    int ascii = (char) c;
                    writer.append(Integer.toHexString(ascii));
                }
            }
            
        }
        writer.append("\n");
        writer.append("E").append(Integer.toHexString(startingAddress));
    }

    public void writeFile(String filePath) {

        try {
            FileWriter filewriter = new FileWriter(filePath);
            BufferedWriter writer = new BufferedWriter(filewriter);
            writer.write("hi");
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(FileOperator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private boolean isValidAssemblyLine(String line) {
//        String pattern = "^\\s*\\w{1,5}+\\s*\\w*,?\\w*,?\\w*\\s*\\w*'?,?\\w*'?\\s*$";
//        Pattern r = Pattern.compile(pattern);
//        Matcher m = r.matcher(line);
//      if (m.find()) {
//          System.out.println("MAtched");
//          return true;
//      }
//        System.out.println(line);
//        System.out.println("not matched!!!!!!!!!!!!!!!!!!!!!");
        return true;
    }

    private boolean startWith(String line, String key) {
        if (line.trim().split("\\s+")[0].equals(key)) {
            return true;
        }
        return false;
    }

    private Instruction parseLine(String line) {

        String[] tokens = line.trim().split("\\s+");
        Instruction instruction = new Instruction();
        String symbol = null;
        String opCode = null;
        String operand = null;
        if (tokens.length == 1) {

            if (opTable.contains(tokens[0])) {
                opCode = tokens[0];
            } else {
                throw new RuntimeException("ERROR, Unrecognised Mnemonic");
            }

        } else if (tokens.length == 2) {

            if (opTable.contains(tokens[0])) {
                opCode = tokens[0];
                operand = tokens[1];
            } else {
                throw new RuntimeException("ERROR, Unrecognised Mnemonic");
            }
        } else if (tokens.length == 3) {
            if (opTable.contains(tokens[1]) || isValidDirective(tokens[1])) {
                symbol = tokens[0];
                opCode = tokens[1];
                operand = tokens[2];
            } else {
                throw new RuntimeException("ERROR, Unrecognised Mnemonic");
            }
            if (symTable.containsKey(tokens[0])) {
                throw new RuntimeException("ERROR, Duplicate Symbol");
            }
            symTable.put(symbol, locationCounter);
        }
        instruction.setSymbol(symbol);
        instruction.setMnemonic(opCode);
        instruction.setOperand(operand);
        instruction.setLocation(locationCounter);

        instruction.setIsIndexed(isIndexed(operand));
        incrementLocationCounter(opCode, operand);

        return instruction;
    }

    private boolean isValidDirective(String opCode) {
        String newOpCode = opCode.trim().toUpperCase();
        return ("BYTE".equals(newOpCode) || "WORD".equals(newOpCode) || "RESW".equals(newOpCode) || "RESB".equals(newOpCode));
    }

    private boolean isIndexed(String operand) {
        if (operand != null && operand.replaceAll("\\s+", "").toUpperCase().endsWith(",X")) {
            return true;
        }
        return false;
    }

    private void incrementLocationCounter(String opCode, String operand) {

        if (opCode.toUpperCase().equals("WORD")) {
            locationCounter += 3;
        } else if (opCode.toUpperCase().equals("BYTE")) {
            addLengthToLocationCounter(operand);
        } else if (opCode.toUpperCase().equals("RESB")) {
            locationCounter += Integer.parseInt(operand);
        } else if (opCode.toUpperCase().equals("RESW")) {
            locationCounter += Integer.parseInt(operand) * 3;
        } else {
            locationCounter += 3;
        }
    }

    private boolean parseFirstLine(String line) {
        String[] tokens = line.trim().split("\\s+");
        if (tokens.length == 3 && "START".equals(tokens[1].toUpperCase())) {
            startingAddress = Integer.parseInt(tokens[2]);
            locationCounter = Integer.parseInt(tokens[2]);
            return true;
        }
        locationCounter = 0;
        return false;

    }

    private void addLengthToLocationCounter(String operand) {
        String modifiedOperand = operand.replaceAll("\\s+", "");

        if (modifiedOperand.toUpperCase().startsWith("C")) {
            String substring = modifiedOperand.substring(2, modifiedOperand.length() - 1);
            locationCounter += substring.length();

        } else if (modifiedOperand.toUpperCase().startsWith("X")) {
            String substring = modifiedOperand.substring(2, modifiedOperand.length() - 1);
            locationCounter += substring.length() * 4;

        } else {
            throw new RuntimeException("ERROR, Wrong format");
        }
    }
}
