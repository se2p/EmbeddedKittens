package scratch.structure.ast.stack;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import scratch.structure.ast.Ast;
import scratch.structure.ast.inputs.Literal;
import utils.JsonParser;

import static org.junit.Assert.fail;

public class SingleIntInputBlockTest {

    JsonNode script;

    @Before
    public void setup() {
        script = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/singlelistinput.json");
    }

//    @Test TODO update this test as soon as we have list blocks
//    public void testListInput() {
//        Ast ast = new Ast();
//        ast.parseScript(script);
//
//        SingleIntInputBlock block = (SingleIntInputBlock) ast.getRoot();
//        while (block.getNext() != null) {
//            if (!block.getInputID().equals("TOXx2wvdeM)Hd4dXaT~-")) {
//                fail("Every block should have a list input");
//            }
//            block = (SingleIntInputBlock) block.getNext();
//        }
//    }

    @Test
    public void testInputShadows() {
        Ast ast = new Ast();
        ast.parseScript(script);

        SingleIntInputBlock block = (SingleIntInputBlock) ast.getRoot();
        while (block.getNext() != null) {
            Literal shadow = (Literal) block.getSlot().getShadow();
            String value = shadow.getValue();
            if (!"0".equals(value)) {
                fail("Every block should have a shadow with the value 0.");
            }
            block = (SingleIntInputBlock) block.getNext();
        }
    }

}
