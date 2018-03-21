package sicassembler;

/**
 *
 * @author ahmed
 */
public class SICAssembler {

    public static void main(String[] args) {
        Assembler assembler = new Assembler();
        assembler.executePassOne("assembly.txt");
        assembler.executePassTwo();
        System.out.println(assembler.writer.toString().toUpperCase());
    }
    
}
