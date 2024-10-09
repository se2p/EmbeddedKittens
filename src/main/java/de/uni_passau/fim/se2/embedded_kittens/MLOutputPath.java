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

import java.nio.file.Path;

import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

/**
 * Machine learning preprocessors can either print their result to the console or write them to a file.
 */
public class MLOutputPath {

    private final MLOutputPathType pathType;
    private final Path path;

    private MLOutputPath(MLOutputPathType pathType, Path path) {
        this.pathType = pathType;
        this.path = path;
    }

    public static MLOutputPath console() {
        return new MLOutputPath(MLOutputPathType.CONSOLE, null);
    }

    /**
     * Builds an output specification.
     *
     * @param dir The path to an existing directory.
     * @return An output specification for an ML preprocessor.
     */
    public static MLOutputPath directory(final Path dir) {
        Preconditions.checkArgument(
            !dir.toFile().exists() || dir.toFile().isDirectory(),
            "The output path for a machine learning preprocessor must be a directory!"
        );
        return new MLOutputPath(MLOutputPathType.PATH, dir);
    }

    public Path getPath() {
        return path;
    }

    @Override
    public String toString() {
        if (MLOutputPathType.CONSOLE.equals(pathType)) {
            return "CONSOLE";
        }
        else {
            return path.toString();
        }
    }

    public boolean isConsoleOutput() {
        return MLOutputPathType.CONSOLE.equals(pathType);
    }

    private enum MLOutputPathType {
        CONSOLE,
        PATH
    }
}
