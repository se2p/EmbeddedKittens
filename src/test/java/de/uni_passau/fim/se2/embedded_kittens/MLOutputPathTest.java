/*
 * Copyright (C) 2021-2024 EmbeddedKittens contributors
 *
 * This file is part of EmbeddedKittens.
 *
 * EmbeddedKittens is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * EmbeddedKittens is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EmbeddedKittens. If not, see <http://www.gnu.org/licenses/>.
 *
 * SPDX-FileCopyrightText: 2021-2024 EmbeddedKittens contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package de.uni_passau.fim.se2.embedded_kittens;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class MLOutputPathTest {

    @Test
    void testDisallowDirectories() {
        Path f = Path.of("src", "test", "fixtures", "emptyProject.json");
        assertThat(f.toFile().exists()).isTrue();

        assertThrows(IllegalArgumentException.class, () -> MLOutputPath.directory(f));
    }

    @Test
    void testAllowNonExistingDirectories() {
        Path d = Path.of("tmp", UUID.randomUUID().toString());
        assertThat(d.toFile().exists()).isFalse();
        MLOutputPath outputPath = MLOutputPath.directory(d);
        assertThat(outputPath.getPath().toFile()).isEqualTo(d.toFile());
    }

    @Test
    void testConsoleOutput() {
        MLOutputPath p = MLOutputPath.console();
        assertThat(p.isConsoleOutput()).isTrue();
        assertThat(p.toString()).isEqualTo("CONSOLE");
    }
}
