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
package de.uni_passau.fim.se2.embedded_kittens.util;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtil {

    // Helpful documentation: https://en.wikipedia.org/wiki/Unicode_character_property

    /**
     * Separators and other control characters.
     */
    private static final String SPACES = "[\\p{Z}\\p{C}]";

    /**
     * Punctuations and symbols.
     */
    private static final String SPECIAL_WITHOUT_QUESTION_EXCLAMATION_MARK = "[\\p{P}\\p{S}&&[^?!]]";

    private static final Pattern SPLIT_PATTERN = Pattern.compile(
        // digit followed by non-digit or other way round
        "(?<=\\d)(?=\\D)|(?<=\\D)(?=\\d)"
            // lowercase followed by uppercase
            + "|(?<=\\p{Ll})(?=\\p{Lu})"
            + "|" + SPECIAL_WITHOUT_QUESTION_EXCLAMATION_MARK
            // uppercase, if followed by one uppercase and one lowercase letter
            // i.e. do not split all-caps words
            + "|(?<=\\p{Lu})(?=\\p{Lu}\\p{Ll})"
            + "|" + SPACES
    );

    private static final Pattern PUNCTUATION_SPLIT_PATTERN = Pattern.compile("(?<=[^?!])(?=[?!])");

    private StringUtil() {
        throw new IllegalCallerException("utility class constructor");
    }

    public static List<String> splitToSubtokens(final String token) {
        return splitToSubtokenStream(token).toList();
    }

    public static List<String> splitToNormalisedSubtokens(final String token, final String delimiter) {
        return splitToNormalisedSubtokenStream(token, delimiter).toList();
    }

    public static Stream<String> splitToNormalisedSubtokenStream(final String token, final String delimiter) {
        return splitToSubtokenStream(token)
            .map(subtoken -> StringUtil.normaliseSubtoken(subtoken, delimiter))
            .filter(s -> !s.isEmpty() && !isOnlyDelimiter(s, delimiter));
    }

    private static boolean isOnlyDelimiter(final String token, final String delimiter) {
        return token.matches(Pattern.quote(delimiter) + "+");
    }

    /**
     * Splits a token into subtokens.
     *
     * @param token Some token.
     * @return A sequence of subtokens.
     */
    public static Stream<String> splitToSubtokenStream(final String token) {
        final String[] split = SPLIT_PATTERN.split(token.trim());
        return Stream.of(split)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .flatMap(s -> Arrays.stream(PUNCTUATION_SPLIT_PATTERN.split(s)))
            .filter(s -> !s.isEmpty());
    }

    /**
     * Splits the string into subtokens first and then normalises each one using
     * {@link #normaliseSubtoken(String, String)}.
     *
     * <p>
     * Subtokens are joined together with underscores to form the final result.
     *
     * @param token Some string.
     * @return The normalised string.
     */
    public static String normaliseString(final String token) {
        return normaliseString(token, "_");
    }

    /**
     * Splits the string into subtokens first and then normalises each one using
     * {@link #normaliseSubtoken(String, String)}.
     *
     * <p>
     * Subtokens are joined together with the given delimiter to form the final result.
     *
     * @param token     Some string.
     * @param delimiter The delimiter.
     * @return The normalised string.
     */
    public static String normaliseString(final String token, final String delimiter) {
        return splitToNormalisedSubtokenStream(token, delimiter)
            .collect(Collectors.joining(delimiter))
            .replaceAll(Pattern.quote(delimiter) + "+", delimiter);
    }

    /**
     * Converts tokens into a normalised form without special characters.
     *
     * <p>
     * Applied normalisations:
     * <ul>
     * <li>Converts to lowercase.</li>
     * <li>Replaces whitespace with the delimiter.</li>
     * <li>Replaces punctuation except {@code ?} and {@code !} with {@code delimiter}.</li>
     * <li>Replaces repeated {@code delimiter} with a single one.</li>
     * </ul>
     *
     * @param s         Some string.
     * @param delimiter The delimiter.
     * @return The input string in its normalised form.
     */
    public static String normaliseSubtoken(final String s, final String delimiter) {
        final String quotedDelimiter = Pattern.quote(delimiter);

        return s.trim()
            .toLowerCase(Locale.ROOT)
            .replaceAll(SPACES + "+", delimiter)
            .replaceAll(SPECIAL_WITHOUT_QUESTION_EXCLAMATION_MARK + "+", delimiter)
            // remove repeated delimiter to remove empty subtokens
            .replaceAll(quotedDelimiter + "+", delimiter)
            // remove delimiter from start/end to remove empty leading/trailing subtokens
            .replaceAll("^" + quotedDelimiter, "")
            .replaceAll(quotedDelimiter + "$", "");
    }
}
