package Mips.Instruction;

import Mips.Register;

public class STORE extends INSTR {

    private Register srcReg;
    private Register locateReg;
    private int offset;

    public STORE(Register srcReg, Register locateReg, int offset) {
        this.srcReg = srcReg;
        this.locateReg = locateReg;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return String.format("sw %s, %s(%s)\r\n",srcReg,offset,locateReg);
    }
}
