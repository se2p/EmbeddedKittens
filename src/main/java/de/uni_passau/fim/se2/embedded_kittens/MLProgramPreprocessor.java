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

import java.util.stream.Stream;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public abstract class MLProgramPreprocessor<R> {

    protected final MLPreprocessorCommonOptions commonOptions;

    protected MLProgramPreprocessor(final MLPreprocessorCommonOptions commonOptions) {
        this.commonOptions = commonOptions;
    }

    /**
     * Converts the result from the preprocessing into a format that can be written to a file.
     *
     * @param result The preprocessing result.
     * @return A representation of the result that can be written to a file.
     */
    public abstract String resultToString(R result);

    public abstract Stream<R> processSprites(Program program);

    public abstract Stream<R> processWholeProgram(Program program);
}
