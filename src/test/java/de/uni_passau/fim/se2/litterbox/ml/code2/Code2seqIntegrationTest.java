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
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.uni_passau.fim.se2.litterbox.ml.CliTest;

class Code2seqIntegrationTest extends CliTest {

    private final String C2S_CMD = "code2seq";

    @Test
    void disallowBothWholeProgramAndPerSprite() {
        int returnCode = commandLine.execute(C2S_CMD, "--whole-program", "--scripts");
        assertThat(returnCode).isNotEqualTo(0);
        assertThat(getOutput()).isEmpty();
    }

    @Test
    void processEmptyProgram() {
        commandLine.execute(C2S_CMD, "-p", "src/test/fixtures/emptyProject.json");
        assertEmptyStdOut();
    }

    @Test
    void processProgramWithMultipleSprites() {
        commandLine.execute(C2S_CMD, "-p", "src/test/fixtures/multipleSprites.json", "--include-stage");
        assertEmptyStdErr();

        final List<String> outputLines = getOutput().lines().toList();
        assertThat(outputLines).hasSize(3);
        assertThat(outputLines).containsExactly(
            "cat 39,29|57|41|8|25|153|27,39 39,29|57|41|8|25|156,39 hi|!,27|153|25|156,hi|!",
            "abby green|flag,67|8|25|153|27,green|flag",
            "stage green|flag,67|8|25|26|29,green|flag"
        );
    }

    @Test
    void processProgramWithMultipleSpritesWholeProgram() {
        commandLine.execute(C2S_CMD, "-p", "src/test/fixtures/multipleSprites.json", "--whole-program");
        assertEmptyStdErr();

        final String output = getOutput();
        final List<String> outputLines = output.lines().toList();
        assertThat(outputLines).hasSize(1);

        assertThat(output).startsWith("program ");

        final Stream<String> expectedPaths = Stream.of(
            "39,29|57|41|8|25|153|27,39",
            "39,29|57|41|8|25|156,39",
            "hi|!,27|153|25|156,hi|!",
            "green|flag,67|8|25|153|27,green|flag"
        );
        assertAll(expectedPaths.map(path -> () -> assertThat(output).contains(path)));
    }

    @Test
    void processProgramWithMultipleSpritesPerScript() {
        commandLine.execute(C2S_CMD, "-p", "src/test/fixtures/multipleSprites.json", "--scripts");
        assertEmptyStdErr();
        assertStdOutContains("scriptId_-481429174");
    }

    @Test
    void processProgramWithMultipleSpritesPerScriptToFile(@TempDir File tempDir) throws IOException {
        commandLine.execute(
            C2S_CMD, "-p", "src/test/fixtures/multipleSprites.json", "--scripts", "-o", tempDir.toString()
        );
        assertEmptyStdErr();
        assertEmptyStdOut();

        final String output = Files.readString(tempDir.toPath().resolve("multipleSprites.script.txt"));
        assertThat(output)
            .contains("scriptId_-481429174 39,29|57|41|8|25|153|27,39 39,29|57|41|8|25|156,39 hi|!,27|153|25|156,hi|!");
        assertThat(output).contains("scriptId_1341144038 green|flag,67|8|25|153|27,green|flag");
    }
}
