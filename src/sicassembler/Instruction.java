/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sicassembler;

/**
 *
 * @author ahmed
 */
public class Instruction {

    public int location;
    public String symbol;
    public String mnemonic;
    public String operand;
    public boolean isIndexed = false;

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public String getOperand() {
        return operand;
    }

    public void setOperand(String operand) {
        this.operand = operand;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return "Instruction{" + "location=" + location + ", symbol=" + symbol + ", mnemonic=" + mnemonic + ", operand=" + operand + '}';
    }

}
