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
package de.uni_passau.fim.se2.litterbox.ml.code2;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.uni_passau.fim.se2.litterbox.ml.CliTest;

class Code2vecIntegrationTest extends CliTest {

    @Test
    void processProgramWithMultipleSpritesToFile(@TempDir Path tempDir) throws IOException {
        commandLine.execute(
            "code2vec", "-p", "src/test/fixtures/multipleSprites.json", "--include-stage", "-o", tempDir.toString()
        );

        final List<String> outputLines = Files.readAllLines(tempDir.resolve("multipleSprites.txt"));
        assertThat(outputLines).hasSize(3);
        assertThat(outputLines).containsExactly(
            "cat 39,625791294,hi_! 39,1493538624,Show hi_!,-547448667,Show",
            "abby GreenFlag,-2069003229,hello_!",
            "stage GreenFlag,1809747443,10"
        );
    }
}
