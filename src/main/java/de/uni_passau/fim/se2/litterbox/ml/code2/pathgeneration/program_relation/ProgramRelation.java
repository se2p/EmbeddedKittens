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
package de.uni_passau.fim.se2.litterbox.ml.code2.pathgeneration.program_relation;

public class ProgramRelation {
    private final String source;
    private final String target;
    private final String path;

    /**
     * Intentionally package-private, use the {@link ProgramRelationFactory} to create new instances.
     *
     * @param sourceName The terminal at the start of the path.
     * @param targetName The terminal at the end of the path.
     * @param path The path connecting the two terminals.
     */
    ProgramRelation(String sourceName, String targetName, String path) {
        source = sourceName;
        target = targetName;
        this.path = path;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s", source, path, target);
    }
}
