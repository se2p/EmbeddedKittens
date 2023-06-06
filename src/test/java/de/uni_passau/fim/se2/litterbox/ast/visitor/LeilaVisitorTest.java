/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.LeilaAnalyzer;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;

public class LeilaVisitorTest implements JsonTest {

    @Test
    public void testSetRotationStyle() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/setRotationStyle.json";
        String output = getLeilaForProject(path);
        assertThat(output).contains("define rotationStyle as \"don't rotate\"");
        assertThat(output).contains("define rotationStyle as \"left-right\"");
        assertThat(output).contains("define rotationStyle as \"all around\"");
    }

    @Test
    public void testTouching() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/touching.json";
        String output = getLeilaForProject(path);

        assertThat(output).contains("touchingMousePointer()");
        assertThat(output).contains("touchingEdge()");
        assertThat(output).contains("touchingColor(rgb(88, 192, 228))");
        assertThat(output).contains("touchingObject(locate actor \"Apple\")");
    }

    @Test
    public void testMouseDown() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/mouseDown.json";
        String output = getLeilaForProject(path);

        assertThat(output).contains("mouseDown()");
    }

    @Test
    public void testJoin() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/join.json";
        String output = getLeilaForProject(path);

        assertThat(output).contains("joinStrings(\"apple \", \"banana\")");
    }

    @Test
    public void testTurnRight() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/turnRight.json";
        String output = getLeilaForProject(path);

        assertThat(output).contains("turnRight(15)");
    }

    @Test
    public void testChangeVariableBy() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/changeVariableBy.json";
        String output = getLeilaForProject(path);

        assertThat(output).contains("define myvar as myvar + 1");
    }

    @Test
    public void testFromNumber() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/fromNumber.json";
        String output = getLeilaForProject(path);

        assertThat(output).contains("touchingColor((0 + 0))");
    }

    @Test
    public void testProcedureCombinedTextAndParams() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/ambiguousProcedureAndCombinedTextSignature.json";
        String output = getLeilaForProject(path);

        assertThat(output).contains("define myMethodWithParamsText (aParam: string, bParam: boolean) begin");
    }

    @Test
    public void testAmbiguousProcedureName() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/ambiguousProcedureAndCombinedTextSignature.json";
        String output = getLeilaForProject(path);

        assertThat(output).contains("define myMethod_LHk0K2pDITVfS0wjel1CeVlKXkg () begin");
        assertThat(output).contains("define myMethod_THhXfmdpLEldOSk2Oy0xRG5EZCk () begin");
        assertThat(output).contains("myMethod_LHk0K2pDITVfS0wjel1CeVlKXkg()");
        assertThat(output).doesNotContain("myMethod_THhXfmdpLEldOSk2Oy0xRG5EZCk()");
    }

    @Test
    public void testGoToSprite() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/goToSprite.json";
        String output = getLeilaForProject(path);

        assertThat(output).contains("""
                            declare o as actor\s
                            define o as locate actor "Affe"
                            goToSprite(o)\
                """);
    }

    @Test
    public void testGlideSecsTo() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/glideSecsTo.json";
        String output = getLeilaForProject(path);

        assertThat(output).contains("""
                        declare o as actor\s
                        define o as locate actor "Apple"
                        glideSecsToSprite(1, o)\
                """);
    }

    @Test
    public void testGlobalVarInStage() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/globalInStage.json";
        String output = getLeilaForProject(path);

        assertThat(output).contains("""
                    script on startup do begin\s
                        define Stage.global as 10
                    end \
                """);
        assertThat(output).contains("""
                    script on startup do begin\s
                        define Stage.global as 0
                        define local as 0
                    end \
                """);
        assertThat(output).contains("    declare local as float\n");
        assertThat(output).doesNotContain("    declare Stage.local as float\n");
    }

    @Test
    public void testAttributeAboveValue() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/attributeAboveValue.json";
        String output = getLeilaForProject(path);

        assertThat(output).contains("""

                    script on message "loudness_ABOVE_10" do begin\s
                        moveSteps(10)
                    end\s

                    script on startup do begin\s
                        repeat forever
                            if (loudness > 10) begin\s
                                broadcast "loudness_ABOVE_10"
                            end\s
                        end\s
                    end\s

                    script on message "timer_ABOVE_10" do begin\s
                        moveSteps(10)
                    end\s

                    script on startup do begin\s
                        repeat forever
                            if (timer > 10) begin\s
                                broadcast "timer_ABOVE_10"
                            end\s
                        end\s
                    end \
                """);
    }

    @Test
    public void testBackdropSwitchEvent() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/backdropSwitchEvent.json";
        String output = getLeilaForProject(path);

        assertThat(output).contains("""
                script on message "BACKDROP_SWITCHED_TO_backdrop1" () do begin\s
                        moveSteps(10)
                    end\s

                    script on startup do begin\s
                        declare oldBackdrop as string
                        define oldBackdrop as backdropName()
                        declare currentBackdrop as string
                        define currentBackdrop as backdropName()
                        repeat forever
                            if ((not (oldBackdrop = "backdrop1")) and (currentBackdrop = "backdrop1")) then begin\s
                                broadcast "BACKDROP_SWITCHED_TO_backdrop1" ()
                            end\s
                            define oldBackdrop as currentBackdrop
                            define currentBackdrop as backdropName()
                        end\s
                    end\s

                end\s""");
    }

    private String getLeilaForProject(String path) throws IOException, ParsingException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(out, true, StandardCharsets.UTF_8);
        LeilaVisitor visitor = new LeilaVisitor(stream, false, true);
        Program program = getAST(path);
        visitor.visit(program);
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    public void testCheckFailsForFolder(@TempDir File tempFile) throws IOException {
        Path path = Path.of("./src/test/fixtures/emptyProject.json");
        Path outPath = tempFile.toPath().toAbsolutePath();
        LeilaAnalyzer analyzer = new LeilaAnalyzer(path, outPath.resolve("foobar"), false, true,false);
        analyzer.analyzeFile();
        File output = outPath.resolve("foobar").resolve("emptyProject.sc").toFile();
        assertThat(output.exists()).isFalse();
    }
}
