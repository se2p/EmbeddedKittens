package scratch.structure.ast.stack;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import scratch.structure.ast.Ast;
import scratch.structure.ast.ScratchBlock;
import utils.JsonParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SetVolumeToBlockTest {

    JsonNode script;

    @Before
    public void setup() {
        script = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/setvolumeto.json");
    }

    @Test
    public void testStructure() {
        Ast ast = new Ast();
        ast.parseScript(script);

        ScratchBlock root = ast.getRoot();
        if (!(root instanceof SetVolumeToBlock)) {
            fail("Result of this fixture should be a setvolumeto block");
        }

        ScratchBlock node = root;
        int count = 0;
        while (node.getNext() != null) {
            count++;
            node = (ScratchBlock) node.getNext();
        }
        assertEquals("Two nodes expected", 2, count);
    }

    @Test
    public void testIntInput() {
        Ast ast = new Ast();
        ast.parseScript(script);

        ScratchBlock root = ast.getRoot();
        if (!(root instanceof SetVolumeToBlock)) {
            fail("Result of this fixture should be a setvolumeto block");
        }
        SetVolumeToBlock block = (SetVolumeToBlock) root;
        assertEquals(10, block.getInputValue());
    }

    @Test
    public void testVariableInput() {
        Ast ast = new Ast();
        ast.parseScript(script);

        ScratchBlock root = ast.getRoot();
        if (!(root instanceof SetVolumeToBlock)) {
            fail("Result of this fixture should be a setvolumeto block");
        }
        SetVolumeToBlock block = (SetVolumeToBlock) root.getNext();

        assertEquals("`jEk@4|i[#Fk?(8x)AV.-my variable", block.getInputID());
    }
}

