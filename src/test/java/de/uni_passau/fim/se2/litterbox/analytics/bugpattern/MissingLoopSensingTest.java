/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.pqgram.PQGramProfile;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MissingLoopSensingTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testMissingLoopSensing() throws IOException, ParsingException {
        Program codeHero = JsonTest.parseProgram("./src/test/fixtures/bugpattern/codeHero.json");
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(codeHero);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testAnina() throws IOException, ParsingException {
        Program anina = JsonTest.parseProgram("./src/test/fixtures/bugpattern/anina.json");
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(anina);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testMissingLoopSensingNested() throws IOException, ParsingException {
        Program nestedMissingLoopSensing = JsonTest.parseProgram("./src/test/fixtures/bugpattern/nestedMissingLoopSensing.json");
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(nestedMissingLoopSensing);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testMissingLoopSensingMultiple() throws IOException, ParsingException {
        Program missingLoopSensingMultiple = JsonTest.parseProgram("./src/test/fixtures/bugpattern/missingLoopSensingMultiple.json");
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(missingLoopSensingMultiple);
        Assertions.assertEquals(5, reports.size());
    }

    @Test
    public void testMissingLoopSensingVariable() throws IOException, ParsingException {
        Program missingLoopSensingVariable = JsonTest.parseProgram("./src/test/fixtures/bugpattern/geisterwald.json");
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(missingLoopSensingVariable);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testGetName() {
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Assertions.assertEquals("missing_loop_sensing", parameterName.getName());
    }

    @Test
    public void testMissingLoopSensingAfterWaitUntil() throws IOException, ParsingException {
        Program empty = JsonTest.parseProgram("./src/test/fixtures/bugpattern/missingLoopSensingAfterWaitUntil.json");
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testDistances() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/bugpattern/differentDistancesMLS.json");
        MissingLoopSensing mls = new MissingLoopSensing();
        List<Issue> reports = new ArrayList<>(mls.check(prog));
        Assertions.assertEquals(8, reports.size());

        //scripts are equal, location is equal
        Assertions.assertTrue(reports.get(0).isDuplicateOf(reports.get(1)));
        Assertions.assertEquals(1, reports.get(0).getDistanceTo(reports.get(1)));

        // scripts are different, location is equals
        PQGramProfile profile0 = new PQGramProfile(reports.get(0).getScriptOrProcedureDefinition());
        PQGramProfile profile2 = new PQGramProfile(reports.get(2).getScriptOrProcedureDefinition());
        double distanceSameLocationOtherScript = 1 + profile0.calculateDistanceTo(profile2);
        Assertions.assertEquals(distanceSameLocationOtherScript, reports.get(0).getDistanceTo(reports.get(2)));

        //Location equals other location in same script
        Assertions.assertSame(reports.get(4).getScriptOrProcedureDefinition(), reports.get(5).getScriptOrProcedureDefinition());
        Assertions.assertEquals(reports.get(4).getCodeLocation(), reports.get(5).getCodeLocation());
        Assertions.assertEquals(0, reports.get(4).getDistanceTo(reports.get(5)));

        // scripts are different, location is equals
        PQGramProfile profile4 = new PQGramProfile(reports.get(4).getScriptOrProcedureDefinition());
        distanceSameLocationOtherScript = 1 + profile0.calculateDistanceTo(profile4);
        Assertions.assertEquals(distanceSameLocationOtherScript, reports.get(0).getDistanceTo(reports.get(4)));

        // scripts are different, location is different
        PQGramProfile profile3 = new PQGramProfile(reports.get(3).getScriptOrProcedureDefinition());
        distanceSameLocationOtherScript = 1 + 1 + profile0.calculateDistanceTo(profile3);
        Assertions.assertEquals(distanceSameLocationOtherScript, reports.get(0).getDistanceTo(reports.get(3)));

        //Location not equals other location in same script
        Assertions.assertSame(reports.get(6).getScriptOrProcedureDefinition(), reports.get(7).getScriptOrProcedureDefinition());
        Assertions.assertNotEquals(reports.get(6).getCodeLocation(), reports.get(7).getCodeLocation());
        Assertions.assertEquals(1, reports.get(6).getDistanceTo(reports.get(7)));
    }
}
