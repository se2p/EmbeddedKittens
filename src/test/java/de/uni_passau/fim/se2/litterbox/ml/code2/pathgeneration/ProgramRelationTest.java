/*
 * Copyright (C) 2021-2024 LitterBox-ML contributors
 *
 * This file is part of LitterBox-ML.
 *
 * LitterBox-ML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox-ML is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox-ML. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.ml.code2.pathgeneration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import de.uni_passau.fim.se2.litterbox.ml.JsonTest;
import de.uni_passau.fim.se2.litterbox.ml.code2.pathgeneration.program_relation.ProgramRelation;
import de.uni_passau.fim.se2.litterbox.ml.code2.pathgeneration.program_relation.ProgramRelationFactory;

class ProgramRelationTest implements JsonTest {

    private final ProgramRelationFactory programRelationFactory = new ProgramRelationFactory();
    private final ProgramRelationFactory hashingProgramRelationFactory = ProgramRelationFactory.withHashCodeFactory();

    static final String NO_HASH_OUTPUT = "GreenFlag,(GreenFlag)^(Script)_(StmtList)_(Say)_(StringLiteral),Hello!";

    @Test
    void testSetNoHash() {
        ProgramRelation programRelation = programRelationFactory.build(
            "GreenFlag", "Hello!",
            "(GreenFlag)^(Script)_(StmtList)_(Say)_(StringLiteral)"
        );
        assertEquals(NO_HASH_OUTPUT, programRelation.toString());
    }

    @Test
    void testToString() {
        ProgramRelation programRelation = hashingProgramRelationFactory.build(
            "GreenFlag", "Hello!",
            "(GreenFlag)^(Script)_(StmtList)_(Say)_(StringLiteral)"
        );
        assertEquals("GreenFlag,-2069003229,Hello!", programRelation.toString());
    }
}
