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

/**
 *
 * @author ahmed
 */
public class FileOperator {

    public int locationCounter;
    public int startingAddress;
    public ArrayList<Instruction> instructions = new ArrayList<>();
    public HashMap<String, String> symTable = new HashMap<>();
    public String programName;
    int programLength ;
    public StringBuilder writer;

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

            while ((newLine = reader.readLine())!=null&&!startWith(newLine, "END")) {
                if (newLine.startsWith("#")) {
                    continue;
                }
                if (isValidAssemblyLine(newLine)) {
                    instructions.add(parseLine(newLine));

                    System.out.println(newLine);
                }
            }
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
//        OpTable opCodes = new OpTable();
        writer=new StringBuilder();
        writer.append("H").append(programName).append(" ")
                .append(Integer.toHexString(startingAddress)).append(Integer.toHexString(programLength));
        int t =10;
        for(Instruction instruction : instructions ){
            if(t==10){
                t=0;
                writer.append("%n");
                writer.append("T").append(Integer.toHexString(instruction.location));
                if(instructions.get(instructions.size()-1).location > instruction.location +30){
                    writer.append("1E");
                } else{
                    writer.append(Integer.toHexString(instructions.get(instructions.size()-1).location-instruction.location));
                }
            }
            
            writer.append(instruction.operand);
        }
        writer.append("%n");
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
        //TODO regex validation
        return true;
    }

    private boolean startWith(String line, String key) {
        String st = line.trim().split("\\s+")[0];
        if (line.trim().split("\\s+")[0].equals(key)) {
            return true;
        }
        return false;
    }

    private Instruction parseLine(String line) {

        String[] tokens = line.trim().split("\\s+");
        Instruction instruction = new Instruction();
        if (tokens.length == 1) {
            instruction.setMnemonic(tokens[0]);
        } else if (tokens.length == 2) {
            instruction.setMnemonic(tokens[0]);
            instruction.setOperand(tokens[1]);
        } else if (tokens.length == 3) {
            instruction.setSymbol(tokens[0]);
            instruction.setMnemonic(tokens[1]);
            instruction.setOperand(tokens[2]);
        }
        instruction.setLocation(locationCounter);
        locationCounter += 3;
        return instruction;
    }

    private boolean parseFirstLine(String line) {

        String[] tokens = line.split(" ");

        if (tokens.length == 3 && "START".equals(tokens[1].toUpperCase())) {
            startingAddress = Integer.parseInt(tokens[2]);
            locationCounter = Integer.parseInt(tokens[2]);
            return true;
        }
        locationCounter = 0;
        return false;

    }
}
