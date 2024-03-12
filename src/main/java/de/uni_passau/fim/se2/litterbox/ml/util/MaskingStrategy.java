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

import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class MaskingStrategy {

    private final MaskingType maskingType;
    private final String blockId;
    private final String inputKey;

    private MaskingStrategy(final MaskingType maskingType, final String blockId) {
        this(maskingType, blockId, null);
    }

    private MaskingStrategy(final MaskingType maskingType, final String blockId, final String inputKey) {
        this.maskingType = maskingType;
        this.blockId = blockId;
        this.inputKey = inputKey;
    }

    public static MaskingStrategy none() {
        return new MaskingStrategy(MaskingType.None, null);
    }

    public static MaskingStrategy block(final String blockId) {
        Preconditions.checkNotNull(blockId);
        return new MaskingStrategy(MaskingType.Block, blockId);
    }

    public static MaskingStrategy fixedOption(final String blockId) {
        Preconditions.checkNotNull(blockId);
        return new MaskingStrategy(MaskingType.FixedOption, blockId);
    }

    public static MaskingStrategy input(final String blockId, final String inputkey) {
        Preconditions.checkNotNull(blockId);
        Preconditions.checkNotNull(inputkey);
        return new MaskingStrategy(MaskingType.Input, blockId, inputkey);
    }

    public MaskingType getMaskingType() {
        return maskingType;
    }

    public String getBlockId() {
        return blockId;
    }

    public String getInputKey() {
        return inputKey;
    }
}
