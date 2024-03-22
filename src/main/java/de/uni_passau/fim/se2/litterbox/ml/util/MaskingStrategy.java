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
package de.uni_passau.fim.se2.litterbox.ml.util;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.StopOtherScriptsInSprite;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public sealed abstract class MaskingStrategy {
    public abstract boolean shouldBeMasked(final ASTNode node);

    public static MaskingStrategy none() {
        return None.getInstance();
    }

    public static MaskingStrategy block(final String blockId) {
        Preconditions.checkNotNull(blockId);
        return new Block(blockId);
    }

    public static MaskingStrategy input(final String blockId, final String inputKey) {
        Preconditions.checkNotNull(blockId);
        Preconditions.checkNotNull(inputKey);
        return new Input(blockId, inputKey);
    }

    public static MaskingStrategy fixedOption(final String blockId) {
        Preconditions.checkNotNull(blockId);
        return new FixedOption(blockId);
    }

    public static final class None extends MaskingStrategy {
        private static None INSTANCE;

        private None() {
        }

        @Override
        public boolean shouldBeMasked(final ASTNode node) {
            return false;
        }

        private static None getInstance() {
            if (INSTANCE == null) {
                INSTANCE = new None();
            }

            return INSTANCE;
        }
    }

    public static final class Block extends MaskingStrategy {
        private final String blockId;

        private Block(final String blockId) {
            this.blockId = blockId;
        }

        @Override
        public boolean shouldBeMasked(final ASTNode node) {
            return blockId.equals(AstNodeUtil.getBlockId(node));
        }
    }

    public static final class Input extends MaskingStrategy {
        private final String blockId;
        private final String inputKey;

        private Input(final String blockId, final String inputKey) {
            this.blockId = blockId;
            this.inputKey = inputKey;
        }

        @Override
        public boolean shouldBeMasked(final ASTNode node) {
            return blockId.equals(AstNodeUtil.getBlockId(node.getParentNode()))
                && AstNodeUtil.isInputOfKind(node, inputKey);
        }
    }

    public static final class FixedOption extends MaskingStrategy {
        private final String blockId;

        private FixedOption(final String blockId) {
            this.blockId = blockId;
        }

        @Override
        public boolean shouldBeMasked(final ASTNode node) {
            final boolean isStopBlock = node instanceof StopAll
                    || node instanceof StopThisScript
                    || node instanceof StopOtherScriptsInSprite;

            // Special handling required for stop blocks due to inconsistent representation in the LitterBox AST.
            return isStopBlock
                ? blockId.equals(AstNodeUtil.getBlockId(node))
                : blockId.equals(AstNodeUtil.getBlockId(node.getParentNode()));
        }
    }
}
