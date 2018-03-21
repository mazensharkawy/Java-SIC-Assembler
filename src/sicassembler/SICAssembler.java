package sicassembler;

/**
 *
 * @author ahmed
 */
public class SICAssembler {

    public static void main(String[] args) {
        FileOperator operator = new FileOperator();
        operator.executeAssemblerPassOne("assembly.txt");
        operator.executeAssemblerPassTwo();
        System.out.println(operator.writer);
    }
    
}
