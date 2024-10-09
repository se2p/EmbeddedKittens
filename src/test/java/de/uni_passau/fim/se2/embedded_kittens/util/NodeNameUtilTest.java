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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorType;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.CommentMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.ImageMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.SoundMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;

class NodeNameUtilTest {

    @ParameterizedTest(name = "{displayName} [{index}] actorName={0}")
    @ValueSource(strings = { " \t\n\r  ", "43789243789", "*&($" })
    void testNormalizedEmpty(final String actorName) {
        final Optional<String> normalized = NodeNameUtil.normalizeSpriteName(buildActor(actorName));
        assertEquals(Optional.empty(), normalized);
    }

    @ParameterizedTest
    @MethodSource("getRegularNormalizedPairs")
    void testNormalizeName(final String normalized, final String actorName) {
        assertEquals(Optional.of(normalized), NodeNameUtil.normalizeSpriteName(buildActor(actorName)));
    }

    private static Stream<Arguments> getRegularNormalizedPairs() {
        return Stream.of(
            Arguments.of("download", "download (48)"),
            Arguments.of("pinos|de|boliche|removebg|preview", "pinos-de-boliche_1975-89-removebg-preview3"),
            Arguments.of("test|one", "test ONE"),
            Arguments.of("test|six|multiple|parts", "test|six{multiple@ parts"),
            Arguments.of("test|three", "test,\"three'"),
            Arguments.of("test|two", "test\ntwo"),
            Arguments.of("testαλfive", "testαλfive"),
            Arguments.of("αλ|λϝδ|δ", "αλΛϝδΔ")
        );
    }

    @ParameterizedTest
    @MethodSource("latinOnlyNormalizationChecks")
    void testNormalizeNameLatinOnly(final Optional<String> normalized, final String actorName) {
        assertEquals(normalized, NodeNameUtil.normalizeSpriteNameLatinOnly(buildActor(actorName)));
    }

    private static Stream<Arguments> latinOnlyNormalizationChecks() {
        return Stream.of(
            Arguments.of(Optional.of("download"), "download (48)"),
            Arguments
                .of(Optional.of("pinos|de|boliche|removebg|preview"), "piños_de-boliche_1975-89-removebg-preview3"),
            Arguments.of(Optional.of("testfive"), "testαλfive"),
            Arguments.of(Optional.empty(), "αλΛϝδΔ"),
            Arguments.of(Optional.of("aa|o|ixi"), "äà3öø_íxì")
        );
    }

    @ParameterizedTest
    @MethodSource("getDefaultNameActors")
    void testHasDefaultName(final ActorDefinition actor, final boolean hasDefaultName) {
        assertEquals(
            hasDefaultName,
            NodeNameUtil.hasDefaultName(actor),
            () -> String.format(
                "Expecting '%s' to be a default name: %b",
                actor.getIdent().getName(),
                hasDefaultName
            )
        );
    }

    private static Stream<Arguments> getDefaultNameActors() {
        return Stream.of(
            Arguments.of(buildActor("Sprite1"), true),
            Arguments.of(buildActor("Sprite13461278"), true),
            Arguments.of(buildActor("sprite_23"), false),
            Arguments.of(buildActor(""), false),
            Arguments.of(buildActor("Figur"), true),
            Arguments.of(buildActor("Figur2"), true),
            Arguments.of(buildActor("Αντικείμενο"), true),
            Arguments.of(buildActor("αντικείμενο"), true),
            Arguments.of(buildActor("αντικείμενο123"), true)
        );
    }

    @Test
    void regressionTestTruncatedWithTrailingDelimiter() {
        final ActorDefinition actor = buildActor(
            "kisspng-digital-cameras-computer-icons-clip-art-encapsulat-photo"
                + "-camera-png-icons-and-graphics-page-9-png-5cec96d4d759e2"
        );
        // 99 characters long, truncating to 100 would cause a trailing |
        final String expected = "kisspng|digital|cameras|computer|icons|clip|art|encapsulat|photo|camera|png|icons"
            + "|and|graphics|page";

        assertEquals(Optional.of(expected), NodeNameUtil.normalizeSpriteName(actor));
    }

    @Test
    void regressionTestTruncated() {
        final ActorDefinition actor = buildActor("abcdefghij".repeat(11));
        final String expected = "abcdefghij".repeat(10);

        assertEquals(100, expected.length());
        assertEquals(Optional.of(expected), NodeNameUtil.normalizeSpriteName(actor));
    }

    /**
     * Multibyte characters should not be cut in half to not leave invalid Unicode at the end
     */
    @Test
    void regressionTestUnicodeBoldCharacters() {
        final String name = "abcdefghij|".repeat(8)
            + "\uD835\uDDF1\uD835\uDDF6\uD835\uDDF3\uD835\uDDF3\uD835\uDDF2\uD835\uDDFF\uD835\uDDF2\uD835\uDDFB\uD835\uDDF0\uD835\uDDF2";
        final Optional<String> expected = Optional.of(
            "abcdefghij|".repeat(8)
                + "\uD835\uDDF1\uD835\uDDF6\uD835\uDDF3\uD835\uDDF3\uD835\uDDF2\uD835\uDDFF"
        );

        final ActorDefinition actor = buildActor(name);
        final Optional<String> normalized = NodeNameUtil.normalizeSpriteName(actor);

        assertThat(normalized).isEqualTo(expected);
    }

    /**
     * Truncate once for multibyte character, then again for | at end.
     */
    @Test
    void truncateMultipleTimesAtEnd() {
        final String name = "abcdefghij|".repeat(9) + "\uD835\uDDEA";
        final Optional<String> expected = Optional.of("abcdefghij|".repeat(8) + "abcdefghij");

        final ActorDefinition actor = buildActor(name);
        final Optional<String> normalized = NodeNameUtil.normalizeSpriteName(actor);

        assertThat(normalized).isEqualTo(expected);
    }

    private static ActorDefinition buildActor(final String name) {
        final var actorId = new StrId(name);
        final var decls = new DeclarationStmtList(Collections.emptyList());
        final var setStmts = new SetStmtList(Collections.emptyList());
        final var procDefs = new ProcedureDefinitionList(Collections.emptyList());
        final var scripts = new ScriptList(Collections.emptyList());
        final var metadata = new ActorMetadata(
            new CommentMetadataList(Collections.emptyList()),
            0,
            new ImageMetadataList(Collections.emptyList()),
            new SoundMetadataList(Collections.emptyList())
        );

        return new ActorDefinition(ActorType.getSprite(), actorId, decls, setStmts, procDefs, scripts, metadata);
    }
}
