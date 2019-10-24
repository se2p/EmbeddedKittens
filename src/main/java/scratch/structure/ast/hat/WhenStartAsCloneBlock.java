package scratch.structure.ast.hat;

import scratch.structure.ast.Stackable;
import scratch.structure.ast.visitor.BlockVisitor;

public class WhenStartAsCloneBlock extends HatBlock {

    public WhenStartAsCloneBlock(String opcode, Stackable next, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, next, shadow, topLevel, x, y);
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }

}
