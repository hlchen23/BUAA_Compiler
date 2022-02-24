package Mips.Instruction;

import Mips.Register;

public class LOAD extends INSTR {

    private Register dstReg;
    private Register locateReg;
    private int offset;

    public LOAD(Register dstReg, Register locateReg, int offset) {
        this.dstReg = dstReg;
        this.locateReg = locateReg;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return String.format("lw %s, %s(%s)\r\n",dstReg,offset,locateReg);
    }
}
