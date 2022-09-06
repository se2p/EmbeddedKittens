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
package de.uni_passau.fim.se2.litterbox.analytics.mblock.perfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MatrixOffScriptTest implements JsonTest {

    @Test
    public void testEmpty() throws ParsingException, IOException {
        assertThatFinderReports(0, new MatrixOffScript(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void motorScreenCodey() throws ParsingException, IOException {
        assertThatFinderReports(1, new MatrixOffScript(), "./src/test/fixtures/mblock/test/turnOffResourcesMotorScreen.json");
    }

    @Test
    public void motorScreenWorkaround() throws ParsingException, IOException {
        assertThatFinderReports(1, new MatrixOffScript(), "./src/test/fixtures/mblock/test/turnOffResourceWorkaround.json");
    }

    @Test
    public void motorScreenWithShutdownBetween() throws ParsingException, IOException {
        assertThatFinderReports(1, new MatrixOffScript(), "./src/test/fixtures/mblock/test/turnOffResourcesInclBlack.json");
    }

    @Test
    public void motorOffScreenNot() throws ParsingException, IOException {
        assertThatFinderReports(0, new MatrixOffScript(), "./src/test/fixtures/mblock/test/turnOffNotComplete.json");
    }
}
