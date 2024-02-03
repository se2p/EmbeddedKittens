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
package de.uni_passau.fim.se2.litterbox.ml;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;

import java.io.IOException;
import java.nio.file.Path;

public interface JsonTest {
    default Program getAST(String fileName) throws IOException, ParsingException {
        Scratch3Parser parser = new Scratch3Parser();
        return parser.parseFile(Path.of(fileName).toFile());
    }

    // TODO: This is a bit redundant wrt getAST (it is added for the tests that have a static test fixture)
    static Program parseProgram(String fileName) throws IOException, ParsingException {
        Scratch3Parser parser = new Scratch3Parser();
        return parser.parseFile(Path.of(fileName).toFile());
    }
}
