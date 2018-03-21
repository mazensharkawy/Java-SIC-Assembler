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

    public void executePassOne(String filePath) {

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

    public void executePassTwo() {
        OpTable opCodes = new OpTable();
        writer = new StringBuilder();
        writer.append("H").append(programName).append(" ")
                .append(formatHexa(Integer.toHexString(startingAddress), 6)).append(" ").append(formatHexa(Integer.toHexString(programLength), 6));
        int t = 30;
        for (Instruction instruction : instructions) {
            if (t >= 28) {
                if (instruction.getMnemonic().equalsIgnoreCase("RESW")
                    ||instruction.getMnemonic().equalsIgnoreCase("RESB")) continue;
                t = 0;
                writer.append("\n");
                writer.append("T").append(Integer.toHexString(instruction.getLocation()));
                if (instructions.get(instructions.size() - 1).getLocation() > instruction.getLocation() + 30) {
                    writer.append("1E");
                } else if (instructions.get(instructions.size() - 1).getLocation() > instruction.getLocation() + 15) {
                    writer.append(Integer.toHexString(instructions.get(instructions.size() - 1).getLocation() - instruction.getLocation()));
                } else {
                    writer.append(0);
                    writer.append(Integer.toHexString(instructions.get(instructions.size() - 1).getLocation() - instruction.getLocation()));
                }
            }
            System.out.println(((instruction.getSymbol() == null) ? "\t\t" : instruction.getSymbol() + "\t") + instruction.getMnemonic() + "\t" + instruction.getOperand());
            if (opCodes.getOpCode(instruction.getMnemonic()) != null) {
                writer.append(opCodes.getOpCode(instruction.getMnemonic()));
                t++;
            }
            else if (instruction.getMnemonic().equalsIgnoreCase("RESW")
                    ||instruction.getMnemonic().equalsIgnoreCase("RESB")){
                t = 30;
                continue;
            }

            if (instruction.getOperand() == null) {
                writer.append(0000);
            } else if (instruction.getOperand().charAt(0) >= '0' && instruction.getOperand().charAt(0) <= '9') {
                String hexaOperand = Integer.toHexString(Integer.parseInt(instruction.getOperand()));
                writer.append(formatHexa(hexaOperand, 6));
                t++;
            } else if (symTable.get(instruction.getOperand()) != null) {
                writer.append(formatHexa(Integer.toHexString(symTable.get(instruction.getOperand())), 4));
            } else if (instruction.getOperand().substring(0, 2).equalsIgnoreCase("x'")
                    && instruction.getOperand().charAt(instruction.getOperand().length() - 1) == '\'') {
                writer.append(instruction.getOperand().substring(2, instruction.getOperand().length() - 1));
            } else if (instruction.getOperand().substring(0, 2).equalsIgnoreCase("c'")
                    && instruction.getOperand().charAt(instruction.getOperand().length() - 1) == '\'') {
                for (char c : instruction.getOperand().substring(2, instruction.getOperand().length() - 1).toCharArray()) {
                    int ascii = (char) c;
                    writer.append(Integer.toHexString(ascii));
                }
            } else if (instruction.getOperand().contains(",") 
                    && (instruction.getOperand().contains("x")) || instruction.getOperand().contains("X")){
                
                String[] currentOperands = instruction.getOperand().split("\\,");
                int address =symTable.get(currentOperands[0])+32768;
                writer.append(Integer.toHexString(address));
                
            }
            else {
//                JOptionPane.showMessageDialog(new JFrame(), 
//                        "Assembling Error: Unknown Operand \"" + instruction.getOperand(), "Dialog",
//                JOptionPane.ERROR_MESSAGE);
                writer.append("0000"); // to be removed
            }
            t += 2;
        }
        writer.append("\n");
        writer.append("E").append(Integer.toHexString(startingAddress));
    }

    public void writeFile(String filePath) {
        ArrayList<String> objectCodes = new ArrayList<>();
        try {
            FileWriter filewriter = new FileWriter("objectCode.txt");
            BufferedWriter writer = new BufferedWriter(filewriter);

            writer.append("H").append(programName).append(" ")
                    .append(formatHexa(Integer.toHexString(startingAddress), 6)).append(" ")
                    .append(formatHexa(Integer.toHexString(programLength), 6));
            writer.newLine();
            System.out.println("size of T 0: " + getSizeOfT(0));
            writer.append("T ").append(Integer.toHexString(instructions.get(0).getLocation())).append(" ").append(getSizeOfT(0));
            int objectsCount = 0;
            for (int i = 0; i < instructions.size(); i++) {
                Instruction instruction = instructions.get(i);
                if (objectsCount++ < 10) {
                    writer.append(instruction.getMnemonic());
                    if (instruction.getOperand() != null || symTable.containsKey(instruction.getOperand())) {
                        writer.append(Integer.toHexString(symTable.get(instruction.getOperand())));
                    }
                } else {
                    writer.newLine();
                    writer.append("T ");
                    continue;
                }
            }

            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Assembler.class.getName()).log(Level.SEVERE, null, ex);
        }

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
        System.out.println(line);
        System.out.println("Assembly line is incorrect");
        throw new RuntimeException("ERROR, Assembly instruction is incorrect");
    }

    private boolean startWith(String line, String key) {
        if (line.trim().split("\\s+")[0].equals(key)) {
            return true;
        }
        return false;
    }

    private String getSizeOfT(int startIndex) {
        int count = 0;
        for (int i = startIndex; i < instructions.size(); i++) {

            if ("RESW".equals(instructions.get(i).getMnemonic()) || "RESB".equals(instructions.get(i).getMnemonic())) {
                if (count <= 10) {
                    return Integer.toHexString(count);
                } else {
                    return "1E";
                }
            }
            count++;
        }
        if (count <= 10) {
            return Integer.toHexString(count);
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
        return out + hexa;
    }
}
