/*
 * Copyright (C) 2019-2022 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IssueSet extends Issue {

    private final List<ActorDefinition> actors = new ArrayList<>();
    private final List<Script> scripts = new ArrayList<>();
    private final List<ASTNode> nodes = new ArrayList<>();
    private final List<Metadata> multipleMetadata = new ArrayList<>();

    public IssueSet(IssueFinder finder, IssueSeverity severity, Program program, List<ActorDefinition> actors, List<Script> scripts, List<ASTNode> nodes, List<Metadata> multipleMetadata, Hint hint) {
        super(finder, severity, program, actors.get(0), scripts.get(0), nodes.get(0), multipleMetadata.get(0), hint);
        this.actors.addAll(actors);
        this.scripts.addAll(scripts);
        this.nodes.addAll(nodes);
        this.multipleMetadata.addAll(multipleMetadata);
    }

    @Override
    public boolean isCodeLocation(ASTNode node) {
        return nodes.contains(node);
    }

    @Override
    public boolean hasMultipleBlocks() {
        return nodes.size() > 1;
    }

    public List<ActorDefinition> getActors() {
        return Collections.unmodifiableList(actors);
    }

    public List<Script> getScripts() {
        return Collections.unmodifiableList(scripts);
    }

    public List<ASTNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public List<Metadata> getMultipleMetadata() {
        return Collections.unmodifiableList(multipleMetadata);
    }
}
