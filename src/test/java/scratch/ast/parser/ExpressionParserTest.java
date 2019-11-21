/*
 * Copyright (C) 2019 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package scratch.ast.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.expression.num.Add;
import scratch.ast.model.expression.num.Div;
import scratch.ast.model.expression.num.Minus;
import scratch.ast.model.expression.num.Mod;
import scratch.ast.model.expression.num.MouseX;
import scratch.ast.model.expression.num.Mult;
import scratch.ast.model.expression.num.NumExpr;
import scratch.ast.model.expression.num.Number;
import scratch.ast.model.expression.num.PickRandom;
import scratch.ast.model.numfunct.Pow10;
import utils.JsonParser;

public class ExpressionParserTest {

    private static JsonNode moveStepsScript;
    private static JsonNode allExprTypesScript;
    private static JsonNode literalBlock;
    private static JsonNode variableBlock;
    private static JsonNode listBlock;
    private static JsonNode blockBlock;

    private static JsonNode twoNumExprSlotsNumExprs;
    private static JsonNode addBlock;
    private static JsonNode minusBlock;
    private static JsonNode divBlock;
    private static JsonNode multBlock;

    @BeforeAll
    public static void setup() {
        moveStepsScript = JsonParser.getBlocksNodeFromJSON("./src/test/fixtures/movesteps.json");
        allExprTypesScript = JsonParser.getBlocksNodeFromJSON("./src/test/fixtures/allexprtypes.json");
        twoNumExprSlotsNumExprs = JsonParser.getBlocksNodeFromJSON("./src/test/fixtures/twoNumExprSlotsNumExprs.json");
        literalBlock = allExprTypesScript.get("QJ:02/{CIWEai#dfuC(k");
        variableBlock = allExprTypesScript.get("Q0r@4R,=K;bq+x;8?O)j");
        listBlock = allExprTypesScript.get("3k1#g23nWs5dk)w3($|+");
        blockBlock = allExprTypesScript.get("K0-dZ/kW=hWWb/GpMt8:");

        addBlock = twoNumExprSlotsNumExprs.get("$`zwlVu=MrX}[7_|OkP0");
        minusBlock = twoNumExprSlotsNumExprs.get("kNxFx|sm51cAUYf?x(cR");
        divBlock = twoNumExprSlotsNumExprs.get("b2JumU`zm:?3szh/07O(");
        multBlock = twoNumExprSlotsNumExprs.get("IBYSC9r)0ccPx;?l-2M|");
    }

    @Test
    public void testParseNumber() {
        JsonNode inputs = moveStepsScript.get("EU(l=G6)z8NGlJFcx|fS").get("inputs");
        Number result = NumExprParser.parseNumber(inputs, 0);
        assertEquals("10.0", String.valueOf(result.getValue()));
    }

    @Test
    public void testParseNumExprLiteral() throws ParsingException {
        NumExpr numExpr = NumExprParser.parseNumExpr(literalBlock, 0, allExprTypesScript);
        assertTrue(numExpr instanceof Number);
    }

    @Test
    public void testParseNumExprBlock() throws ParsingException {
        NumExpr numExpr = NumExprParser.parseNumExpr(blockBlock, 0, allExprTypesScript);
        assertTrue(numExpr instanceof MouseX);
    }

    @Test
    public void testAdd() throws ParsingException {
        NumExpr add = NumExprParser.parseNumExpr(addBlock, 0, twoNumExprSlotsNumExprs);
        assertTrue(add instanceof Add);
        assertEquals("1.0", String.valueOf(((Number) ((Add) add).getFirst()).getValue()));
        assertEquals("2.0", String.valueOf(((Number) ((Add) add).getSecond()).getValue()));
    }

    @Test
    public void testMinus() throws ParsingException {
        NumExpr minus = NumExprParser.parseNumExpr(minusBlock, 0, twoNumExprSlotsNumExprs);
        assertTrue(minus instanceof Minus);
        assertEquals("1.0", String.valueOf(((Number) ((Minus) minus).getFirst()).getValue()));
        assertEquals("2.0", String.valueOf(((Number) ((Minus) minus).getSecond()).getValue()));
    }

    @Test
    public void testMult() throws ParsingException {
        NumExpr mult = NumExprParser.parseNumExpr(multBlock, 0, twoNumExprSlotsNumExprs);
        assertTrue(mult instanceof Mult);
        assertEquals("1.0", String.valueOf(((Number) ((Mult) mult).getFirst()).getValue()));
        assertEquals("2.0", String.valueOf(((Number) ((Mult) mult).getSecond()).getValue()));
    }

    @Test
    public void testDiv() throws ParsingException {
        NumExpr div = NumExprParser.parseNumExpr(divBlock, 0, twoNumExprSlotsNumExprs);
        assertTrue(div instanceof Div);
        PickRandom pickRandom = (PickRandom) ((Div) div).getFirst();
        assertEquals("1.0", String.valueOf(((Number) (pickRandom.getFrom())).getValue()));
        assertEquals("2.0", String.valueOf(((Number) (pickRandom.getTo())).getValue()));
        Mod mod = (Mod) ((Div) div).getSecond();
        assertEquals("1.0", String.valueOf(((Number) (mod.getFirst())).getValue()));
        assertEquals("2.0", String.valueOf(((Number) (mod.getSecond())).getValue()));
    }

    @Test
    public void testNumFuncts() throws ParsingException {
        JsonNode script = JsonParser.getBlocksNodeFromJSON("./src/test/fixtures/numfuncts.json");
        JsonNode pow10Block = script.get("xbBc!xS=1Yz2Yp/DF;JT");
        assertTrue(NumExprParser.parseNumFunct(pow10Block.get("fields")) instanceof Pow10);
    }
}
