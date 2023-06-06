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

import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

public final class NodeNameUtil {

    private NodeNameUtil() {
        throw new IllegalCallerException("utility class constructor");
    }

    /**
     * Normalize sprite name.
     *
     * @param actor The sprite for which the normalized name should be computed.
     * @return The normalized sprite name. An empty optional instead of an empty name.
     */
    public static Optional<String> normalizeSpriteName(final ActorDefinition actor) {
        final String spriteName = actor.getIdent().getName();
        final List<String> splitNameParts = StringUtil.splitToSubtokens(spriteName);
        final String splitName = String.join("|", splitNameParts);

        if (splitName.isEmpty()) {
            return Optional.empty();
        } else if (splitName.length() > 100) {
            return Optional.of(StringUtils.truncate(splitName, 100));
        } else {
            return Optional.of(splitName);
        }
    }

    public static boolean hasDefaultName(final ActorDefinition actor) {
        // no special replacements except removal of the numbers needed: if the non-numeric part is not in the list of
        // known default names, it is not a default name
        final String spriteName = actor.getIdent().getName().toLowerCase().replaceAll("\\d+", "");
        return isDefaultName(spriteName);
    }

    /**
     * Checks if a normalized sprite name is one of the default names.
     *
     * @param normalisedSpriteLabel A <emph>normalised</emph> sprite name.
     * @return If the name is a default name generated by Scratch.
     */
    private static boolean isDefaultName(final String normalisedSpriteLabel) {
        return Constants.DEFAULT_SPRITE_NAMES.contains(normalisedSpriteLabel);
    }
}
