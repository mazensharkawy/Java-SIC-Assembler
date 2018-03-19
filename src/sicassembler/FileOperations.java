package sicassembler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ahmed
 */
public class FileOperations {

    public static void executeAssemblerPathOne(String filePath) {

        FileReader fileReader = null;
        String []tokens;
        try {
            File file = new File(filePath);
            fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
            String newLine = null;
            newLine = reader.readLine();
            tokens = tokenizeLine(newLine);
            
            while ((newLine = reader.readLine()) != null) {
                
                isValidAssemblyLine(newLine);
                tokens = tokenizeLine(newLine);
                
                System.out.println(newLine);
            }
            reader.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileOperations.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileOperations.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fileReader.close();
            } catch (IOException ex) {
                Logger.getLogger(FileOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public static void writeFile(String filePath) {

        try {
            FileWriter filewriter = new FileWriter(filePath);
            BufferedWriter writer =new BufferedWriter(filewriter);
            writer.write("hi");
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(FileOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    private static boolean isValidAssemblyLine(String line){
        //TODO regex validation
        return true;
    }
    
    private static String[] tokenizeLine(String line){
        return line.split(line);
    }

}
