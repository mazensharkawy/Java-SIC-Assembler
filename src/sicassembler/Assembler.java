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
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author ahmed
 */
public class Assembler {

    public int locationCounter;
    public int startingAddress;
    public int programLength;
    public ArrayList<Instruction> instructions = new ArrayList<>();

    public HashMap<String, Integer> symTable = new HashMap<>();
    public String programName;
    public StringBuilder writer;
    private OpTable opTable = new OpTable();

    public void executePassOne(String filePath) throws RuntimeException{

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
            } while (newLine.startsWith("#")||newLine.trim().isEmpty());

            while ((newLine = reader.readLine()) != null && !startWith(newLine, "END")) {
                if (newLine.startsWith("#")||newLine.trim().isEmpty()) {
                    continue;
                }
                if (isValidAssemblyLine(newLine)) {
                    Instruction newInstruction = parseLine(newLine);
                    instructions.add(newInstruction);
                    System.out.println(newInstruction);
                }
            }
            programLength = locationCounter - startingAddress;

            reader.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Assembler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
        } finally {
            try {
                fileReader.close();
            } catch (IOException ex) {
                Logger.getLogger(Assembler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void executePassTwo() throws RuntimeException{
        OpTable opCodes = new OpTable();
        writer = new StringBuilder();
        writer.append("H").append(" ").append(programName).append(" ")
                .append(formatHexa(Integer.toHexString(startingAddress), 6)).append(" ").append(formatHexa(Integer.toHexString(programLength), 6));
        int t = 30;
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instruction = instructions.get(i);
            if (t >= 28) {
                if (instruction.getMnemonic().equalsIgnoreCase("RESW")
                        || instruction.getMnemonic().equalsIgnoreCase("RESB")) {
                    continue;
                }
                t = 0;
                writer.append("\n");
                writer.append("T ").append(formatHexa(Integer.toHexString(instruction.getLocation()), 6)).append(" ");
//                if (instructions.get(instructions.size() - 1).getLocation() > instruction.getLocation() + 30) {
//                    writer.append("1E");
//                } else if (instructions.get(instructions.size() - 1).getLocation() > instruction.getLocation() + 15) {
//                    writer.append(Integer.toHexString(instructions.get(instructions.size() - 1).getLocation() - instruction.getLocation()));
//                } else {
//                    writer.append(0);
//                    writer.append(Integer.toHexString(instructions.get(instructions.size() - 1).getLocation() - instruction.getLocation()));
//                }
                writer.append(formatHexa(getSizeOfT(i),2)).append(" ");
            }
            String opCode="";
            if (opCodes.getOpCode(instruction.getMnemonic()) != null) {
                opCode=opCodes.getOpCode(instruction.getMnemonic());
                writer.append(opCode);
                t++;
            } else if (instruction.getMnemonic().equalsIgnoreCase("RESW")
                    || instruction.getMnemonic().equalsIgnoreCase("RESB")) {
                t = 30;
                continue;
            }

            if (instruction.getOperand() == null) {
                
                instruction.setObjectCode("0000");
                writer.append("0000");
                
            } else if(instruction.getOperand().charAt(instruction.getOperand().length()-1) == 'H'
                    && (instruction.getMnemonic().equalsIgnoreCase("byte")|| instruction.getMnemonic().equalsIgnoreCase("word"))){
                int length = (instruction.getMnemonic().equalsIgnoreCase("byte"))? 2:4;
                instruction.setObjectCode(instruction.getOperand().substring(0,length+1));
                writer.append(instruction.getObjectCode());
            }
            else if (instruction.getOperand().charAt(0) >= '0' && instruction.getOperand().charAt(0) <= '9') {
                
                String hexaOperand = Integer.toHexString(Integer.parseInt(instruction.getOperand()));
                instruction.setObjectCode(formatHexa(hexaOperand, 6));
                writer.append(instruction.getObjectCode());
                t++;
                
            } else if (symTable.get(instruction.getOperand()) != null) {
                
                instruction.setObjectCode(formatHexa(Integer.toHexString(symTable.get(instruction.getOperand())), 4));
                writer.append(instruction.getObjectCode());
                
            } else if (instruction.getOperand().substring(0, 2).equalsIgnoreCase("x'")
                    && instruction.getOperand().charAt(instruction.getOperand().length() - 1) == '\'') {
                instruction.setObjectCode(instruction.getOperand().substring(2, instruction.getOperand().length() - 1));
                writer.append(instruction.getObjectCode());
            } else if (instruction.getOperand().substring(0, 2).equalsIgnoreCase("c'")
                    && instruction.getOperand().charAt(instruction.getOperand().length() - 1) == '\'') {
                instruction.setObjectCode("");
                for (char c : instruction.getOperand().substring(2, instruction.getOperand().length() - 1).toCharArray()) {
                    int ascii = (char) c;
                    instruction.setObjectCode(instruction.getObjectCode()+Integer.toHexString(ascii).toUpperCase());
                }
                    writer.append(instruction.getObjectCode());
            } else if (instruction.getOperand().contains(",")
                    && (instruction.getOperand().contains("x")) || instruction.getOperand().contains("X")) {

                String[] currentOperands = instruction.getOperand().split("\\,");
                int address = symTable.get(currentOperands[0]) + 32768;
                instruction.setObjectCode(Integer.toHexString(address).toUpperCase());
                writer.append(instruction.getObjectCode());

            } else {
                throw new RuntimeException("Assembling Error: Unknown Operand \"" + instruction.getOperand());
                //writer.append("0000"); // to be removed
            }
            
            System.out.println( Integer.toHexString(instruction.getLocation()).toUpperCase() + "\t"  
                    + ((instruction.getSymbol() == null) ? "\t\t" : instruction.getSymbol() + "\t") 
                    + "\t" + instruction.getMnemonic() + "\t" 
                    + instruction.getOperand() + "\t" + opCode+ instruction.getObjectCode());
            
            writer.append(" ");
            t += 2;
        }
        writer.append("\n");
        writer.append("E").append(" ").append(formatHexa(Integer.toHexString(startingAddress),6));
    }

    
    private boolean isValidAssemblyLine(String line) {
        String pattern1 = "^\\s*\\w{1,5}+\\s*$";
        String pattern2 = "^\\s*\\w{1,5}+\\s*\\w+(,\\w+)?\\s*$";
        String pattern3 = "^\\s*\\w+\\s*\\w+\\s*\\w+((,X)|('\\w+'))?\\s*$";
        Pattern r1 = Pattern.compile(pattern1);
        Matcher m1 = r1.matcher(line);
        Pattern r2 = Pattern.compile(pattern2);
        Matcher m2 = r2.matcher(line);
        Pattern r3 = Pattern.compile(pattern3);
        Matcher m3 = r3.matcher(line);
        if (m1.find() || m2.find() || m3.find()) {
            return true;
        }
        System.out.println("Assembly line is incorrect");
        throw new RuntimeException("ERROR, Assembly instruction is incorrect");
    }

    private boolean startWith(String line, String key) {
        if (line.trim().split("\\s+")[0].toUpperCase().equals(key)) {
            return true;
        }
        return false;
    }

    private String getSizeOfT(int startIndex) {//args is the index of first instruction in the T line
        int count = 0;
        int lastOperation = 0;
        for (int i = startIndex; i < instructions.size() && count < 30; i++) {
            if ("RESW".equals(instructions.get(i).getMnemonic()) || "RESB".equals(instructions.get(i).getMnemonic())) {
                if (count == 0) {
                    continue;
                }
                return Integer.toHexString(count).toUpperCase();

            } else if ("BYTE".equals(instructions.get(i).getMnemonic())) {
                if (instructions.get(i).getOperand().startsWith("X")) {
                    lastOperation = 1;
                    count++;

                } else if (instructions.get(i).getOperand().startsWith("C")) {
                    String substring = instructions.get(i).getOperand().substring(2, instructions.get(i).getOperand().length() - 1);
                    count += substring.length();
                    lastOperation = substring.length();
                }
            } else {
                count += 3;
                lastOperation = 3;
            }
        }
        if (count <= 30) {
            return Integer.toHexString(count).toUpperCase();

        } else if (count > 30) {
            System.out.println(lastOperation);
            return Integer.toHexString(count - lastOperation).toUpperCase();
        } else {
            return "1E";
        }
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
        else{
            throw new RuntimeException("Error, Wrong assembly line: " + line);
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
            startingAddress = Integer.parseInt(tokens[2], 16);
            locationCounter = Integer.parseInt(tokens[2], 16);
            programName = tokens[0].trim().toUpperCase();
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
            locationCounter++;

        } else {
            throw new RuntimeException("ERROR, Wrong format");
        }
    }

    private String formatHexa(String hexa, int length) {
        String out = "";
        for (int i = 0; i < length - hexa.length(); i++) {
            out += "0";
        }
        return (out + hexa).toUpperCase();
    }
    
    public void writeObjectFile(File file) {
        String objectFile = file.getName().replace(".txt", "objectCode.txt");
        try {
            FileWriter filewriter = new FileWriter(objectFile);
            BufferedWriter writer = new BufferedWriter(filewriter);
            writer.write(this.writer.toString());
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Assembler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public void writeIntermediateFile(File file) {
        String objectFile = file.getName().replace(".txt", "IntermediateFile.txt");
        try {
            FileWriter filewriter = new FileWriter(objectFile);
            BufferedWriter writer = new BufferedWriter(filewriter);
            
            writer.append(String.format("%-15s",""));
            writer.append(String.format("%-15s",programName.toUpperCase()));
            writer.append(String.format("%-15s","START"));
            writer.append(String.format("%-15s",formatHexa(Integer.toHexString(startingAddress),4)));
            writer.newLine();
            for(Instruction instruction: instructions){
                writer.append(String.format("%-15s",formatHexa(Integer.toHexString(instruction.getLocation()),4)));
                
                if(instruction.getSymbol()!=null){
                   writer.append(String.format("%-15s",instruction.getSymbol()));
                
                }else{
                    writer.append(String.format("%-15s"," "));
                }
                writer.append(String.format("%-15s",instruction.getMnemonic()));
                
                if(instruction.getOperand()!=null){
                   writer.append(String.format("%-15s",instruction.getOperand()));
                
                }else{
                    writer.append(String.format("%-15s"," "));
                }
                writer.newLine();   
            }
            
            writer.append(String.format("%-30s",""));
            writer.append(String.format("%-15s","END"));
            writer.append(String.format("%-15s",programName.toUpperCase())); 
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Assembler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
