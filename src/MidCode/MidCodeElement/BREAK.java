package MidCode.MidCodeElement;

import Mips.MipsFactory;
import Optimizer.ControlFlowAnalysis.Flow;

public class BREAK extends MidCode implements ABSTRACT_JUMP {

    private String label;

    public BREAK(String label) {
        super();
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("break goto %s\r\n",label);
    }

    @Override
    public String createMips() {
        return String.format("j %s\r\n",label);
    }

    @Override
    public void createMipsOpt() {
        Flow.saveBlockTail(this);
        MipsFactory.mipsCode += String.format("j %s\r\n",label);
    }

    @Override
    public int getDstNum() {
        return 1;
    }

    @Override
    public String getDst_1() {
        return label;
    }

    @Override
    public String getDst_2() {
        return null;
    }

    @Override
    public void setDst_1(String dst_1) {

    }

    @Override
    public void setDst_2(String dst_2) {

    }
}
