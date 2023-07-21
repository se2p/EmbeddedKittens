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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.MonitorMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;

import java.util.List;
import java.util.Optional;

public class AstNodeUtil {
    private AstNodeUtil() {
        throw new IllegalCallerException("utility class");
    }

    public static boolean isMetadata(final ASTNode node) {
        return node instanceof Metadata
                || node instanceof CommentMetadataList
                || node instanceof ImageMetadataList
                || node instanceof MonitorMetadataList
                || node instanceof SoundMetadataList;
    }

    public static List<ActorDefinition> getActors(final Program program, boolean includeStage) {
        return program.getActorDefinitionList()
                .getDefinitions()
                .stream()
                .filter(actor -> includeStage || actor.isSprite())
                .toList();
    }

    /**
     * Finds the actor the given node belongs to.
     * @param node Some {@link ASTNode}.
     * @return The actor the node belongs to, empty if the node belongs to no actor.
     */
    public static Optional<ActorDefinition> findActor(final ASTNode node) {
        ASTNode currentNode = node;

        while (currentNode != null) {
            if (currentNode instanceof ActorDefinition actorDefinition) {
                return Optional.of(actorDefinition);
            }
            currentNode = currentNode.getParentNode();
        }

        return Optional.empty();
    }

    /**
     * Replaces all parameter placeholders with the given substitution.
     *
     * <p>Replaces
     * <ul>
     *     <li>string parameters ({@code %s})</li>
     *     <li>boolean parameters ({@code %b})</li>
     *     <li>numeric parameters ({@code %n})</li>
     * </ul>
     *
     * @param procedureName The name of the procedure including the parameter placeholders.
     * @param replacement The substitution string.
     * @return The procedure name with replaced parameter placeholders.
     */
    public static String replaceProcedureParams(final String procedureName, final String replacement) {
        return procedureName.replace("%s", replacement)
                .replace("%b", replacement)
                .replace("%n", replacement)
                .replaceAll("\\s+", " ")
                .trim();
    }
}
