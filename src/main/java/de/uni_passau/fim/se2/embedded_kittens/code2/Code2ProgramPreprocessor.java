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
package de.uni_passau.fim.se2.embedded_kittens.code2;

import de.uni_passau.fim.se2.embedded_kittens.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.embedded_kittens.MLProgramPreprocessor;
import de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration.PathType;
import de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration.ProgramFeatures;

public abstract class Code2ProgramPreprocessor extends MLProgramPreprocessor<ProgramFeatures> {

    protected final PathType pathType;
    protected final int maxPathLength;

    protected Code2ProgramPreprocessor(
        final MLPreprocessorCommonOptions commonOptions, final int maxPathLength, final boolean isPerScript
    ) {
        super(commonOptions);

        this.maxPathLength = maxPathLength;

        if (isPerScript) {
            this.pathType = PathType.SCRIPT;
        }
        else {
            this.pathType = PathType.SPRITE;
        }
    }

    boolean isPerScriptOutput() {
        return PathType.SCRIPT.equals(pathType);
    }
}
