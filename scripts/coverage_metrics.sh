#! /usr/bin/env bash
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


coverage_metrics=$(sh scripts/coverage.sh "$1")

instruction_coverage=$(echo "$coverage_metrics" | sed -nr "s/Instruction Coverage: ([0-9]+.?[0-9]*) %/\1/p" | awk '{ print $1 / 100.0 }')
branch_coverage=$(echo "$coverage_metrics" | sed -nr "s/Branch Coverage: ([0-9]+.?[0-9]*) %/\1/p" | awk '{ print $1 / 100.0 }')

printf 'instruction_coverage_total %f\n' "$instruction_coverage"
printf 'branch_coverage_total %f\n' "$branch_coverage"
