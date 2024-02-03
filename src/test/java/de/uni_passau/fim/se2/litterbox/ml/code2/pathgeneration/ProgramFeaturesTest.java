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

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.uni_passau.fim.se2.litterbox.ml.code2.pathgeneration.program_relation.ProgramRelation;
import de.uni_passau.fim.se2.litterbox.ml.code2.pathgeneration.program_relation.ProgramRelationFactory;

class ProgramFeaturesTest {

    private final ProgramRelationFactory programRelationFactory = ProgramRelationFactory.withHashCodeFactory();

    ProgramFeatures cat;

    @BeforeEach
    void setUp() {
        cat = new ProgramFeatures("cat", programRelationFactory);
        cat.addFeature(
            "39.0",
            "(NumberLiteral)^(Key)^(KeyPressed)^(Script)_(StmtList)_(Say)_(StringLiteral)",
            "Hi!"
        );
        cat.addFeature(
            "39.0",
            "(NumberLiteral)^(Key)^(KeyPressed)^(Script)_(StmtList)_(Show)",
            "Show"
        );
        cat.addFeature(
            "Hi!",
            "(StringLiteral)^(Say)^(StmtList)_(Show)",
            "Show"
        );
    }

    @Test
    void testToString() {
        assertEquals(
            "cat 39.0,625791294,Hi! 39.0," +
                "1493538624,Show Hi!,-547448667,Show",
            cat.toString()
        );
    }

    @Test
    void testAddFeature() {
        assertEquals(3, cat.getFeatures().size());
    }

    @Test
    void testIsEmpty() {
        ProgramFeatures programFeatures = new ProgramFeatures("abby", programRelationFactory);
        assertTrue(programFeatures.isEmpty());
    }

    @Test
    void testGetName() {
        assertEquals("cat", cat.getName());
    }

    @Test
    void testGetFeatures() {
        List<ProgramRelation> features = cat.getFeatures();
        assertEquals(3, features.size());
        assertEquals("39.0,625791294,Hi!", features.get(0).toString());
        assertEquals("39.0,1493538624,Show", features.get(1).toString());
        assertEquals("Hi!,-547448667,Show", features.get(2).toString());
    }
}
