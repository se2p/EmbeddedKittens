#! /usr/bin/env sh
#
# Copyright (C) 2021-2024 LitterBox-ML contributors
#
# This file is part of LitterBox-ML.
#
# LitterBox-ML is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or (at
# your option) any later version.
#
# LitterBox-ML is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with LitterBox-ML. If not, see <http://www.gnu.org/licenses/>.
#


awk -F"," '{
        instructions += $4 + $5;
        covered += $5;
        branches += $6 + $7;
        branchesCovered += $7;
    } END {
        print "Instructions Covered:", covered, "/", instructions;
        print "Instruction Coverage:", 100*covered/instructions, "%";
        print "Branches Covered:", branchesCovered, "/", branches;
        print "Branch Coverage:", 100*branchesCovered/branches, "%";
    }' "$1"
