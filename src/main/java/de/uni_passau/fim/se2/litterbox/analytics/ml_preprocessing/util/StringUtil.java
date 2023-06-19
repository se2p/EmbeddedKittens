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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtil {

    private static final Pattern SPLIT_PATTERN = Pattern.compile(
            // digit followed by non-digit or other way round
            "(?<=\\d)(?=\\D)|(?<=\\D)(?=\\d)"
                    // lowercase followed by uppercase
                    + "|(?<=\\p{Ll})(?=\\p{Lu})"
                    // punctuation without question and exclamation marks
                    + "|[\\p{Punct}&&[^?!]]"
                    // uppercase, if followed by one uppercase and one lowercase letter
                    // i.e. do not split all-caps words
                    + "|(?<=\\p{Lu})(?=\\p{Lu}\\p{Ll})"
                    + "|\\s+"
    );

    private static final Pattern PUNCTUATION_SPLIT_PATTERN = Pattern.compile("(?<=[^?!])(?=[?!])");

    private StringUtil() {
        throw new IllegalCallerException("utility class constructor");
    }

    public static List<String> splitToSubtokens(final String token) {
        return splitToSubtokenStream(token).toList();
    }

    public static List<String> splitToNormalisedSubtokens(final String token) {
        return splitToNormalisedSubtokenStream(token).toList();
    }

    public static Stream<String> splitToNormalisedSubtokenStream(final String token) {
        return splitToSubtokenStream(token)
                .map(StringUtil::normaliseSubtoken)
                .filter(s -> !s.isEmpty());
    }

    public static Stream<String> splitToSubtokenStream(final String token) {
        final String[] split = SPLIT_PATTERN.split(token.trim());
        return Stream.of(split)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .flatMap(s -> Arrays.stream(PUNCTUATION_SPLIT_PATTERN.split(s)))
                .filter(s -> !s.isEmpty());
    }

    /**
     * Splits the string into subtokens first and then normalises each one using {@link #normaliseSubtoken(String)}.
     *
     * <p>Subtokens are joined together with underscores to form the final result.
     *
     * @param token Some string.
     * @return The normalised string.
     */
    public static String normaliseString(final String token) {
        return splitToNormalisedSubtokenStream(token)
                .collect(Collectors.joining("_"))
                .replaceAll("_+", "_");
    }

    /**
     * Converts tokens into a normalised form without special characters.
     *
     * <p>Applied normalisations:
     * <ul>
     *     <li>Converts to lowercase.</li>
     *     <li>Replaces whitespace with underscores.</li>
     *     <li>Replaces punctuation except {@code ?} and {@code !} with {@code _}.</li>
     *     <li>Replaces repeated {@code _} with a single one.</li>
     * </ul>
     *
     * @param s Some string.
     * @return The input string in its normalised form.
     */
    public static String normaliseSubtoken(final String s) {
        final String label = s.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "_")
                .replaceAll("[\\p{Punct}&&[^!?]]+", "_")
                .replaceAll("_+", "_");

        if (label.isEmpty()) {
            return "EMPTY";
        } else {
            return label;
        }
    }
}
