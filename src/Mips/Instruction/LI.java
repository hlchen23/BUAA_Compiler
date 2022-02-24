package Mips.Instruction;

import Mips.Register;

public class LI extends INSTR {
    private Register reg;
    private int im;

    public LI(Register reg, int im) {
        this.reg = reg;
        this.im = im;
    }

    @Override
    public String toString() {
        return String.format("li %s, %s\r\n",reg,im);
    }
}
