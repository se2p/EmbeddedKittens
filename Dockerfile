#
# Copyright (C) 2021-2024 EmbeddedKittens contributors
#
# This file is part of EmbeddedKittens.
#
# EmbeddedKittens is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or (at
# your option) any later version.
#
# EmbeddedKittens is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with EmbeddedKittens. If not, see <http://www.gnu.org/licenses/>.
#
# SPDX-FileCopyrightText: 2021-2024 EmbeddedKittens contributors
# SPDX-License-Identifier: GPL-3.0-or-later
#

# Container image for building the project
FROM docker.io/library/maven:3-eclipse-temurin-17 AS build
LABEL maintainer="Benedikt Fein <fein@fim.uni-passau.de>"

# Parameter for skipping the tests in the build process
ARG SKIP_TESTS=true

WORKDIR /build

# Copy files and directories needed for building
COPY pom.xml ./
COPY src ./src

# Build the project
# The -e flag is to show errors and -B to run in non-interactive aka “batch” mode
# Lastly, make build-artifact naming version-independent
RUN : \
    && mvn -e -B package -DskipTests=${SKIP_TESTS} \
    && mkdir -p /build/bin \
    && mv target/embedded-kittens-*.full.jar bin/embedded-kittens.jar \
    && :

# Slim container image for running EmbeddedKittens
FROM docker.io/library/eclipse-temurin:17-jre
LABEL maintainer="Benedikt Fein <fein@fim.uni-passau.de>"

WORKDIR /embedded-kittens
VOLUME /embedded-kittens

# Copy the jar from the builder to this container
COPY --from=build /build/bin /embedded-kittens-bin

# The executable is EmbeddedKittens
ENTRYPOINT ["java", "-jar", "/embedded-kittens-bin/embedded-kittens.jar"]

# The default argument is the help menu
# This can be overidden on the command line
CMD ["--help"]

