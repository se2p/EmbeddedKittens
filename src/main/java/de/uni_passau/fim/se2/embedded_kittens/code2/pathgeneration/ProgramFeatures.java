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
package de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration.program_relation.ProgramRelation;
import de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration.program_relation.ProgramRelationFactory;

public class ProgramFeatures {

    private final String name;
    private final ProgramRelationFactory programRelationFactory;

    private final List<ProgramRelation> features = new ArrayList<>();

    public ProgramFeatures(final String name, final ProgramRelationFactory programRelationFactory) {
        this.name = name;
        this.programRelationFactory = programRelationFactory;
    }

    @Override
    public String toString() {
        return name + ' ' + features.stream().map(ProgramRelation::toString).collect(Collectors.joining(" "));
    }

    public String toStringWithoutNodeName() {
        return features.stream().map(ProgramRelation::toString).collect(Collectors.joining(" "));
    }

    public void addFeature(String source, String path, String target) {
        final ProgramRelation newRelation = programRelationFactory.build(source, target, path);
        features.add(newRelation);
    }

    public boolean isEmpty() {
        return features.isEmpty();
    }

    public String getName() {
        return name;
    }

    public List<ProgramRelation> getFeatures() {
        return features;
    }
}
