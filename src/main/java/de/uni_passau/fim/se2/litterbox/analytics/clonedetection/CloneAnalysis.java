/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.clonedetection;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

import java.util.*;
import java.util.stream.Collectors;

public class CloneAnalysis {

    public static int MIN_SIZE = 6;
    public static int MAX_GAP = 2;
    public static int MIN_SIZE_NEXT_TO_GAP = 2;

    private int minSize = MIN_SIZE;
    private int maxGap = MAX_GAP;
    private int minSizeNextToGap = MIN_SIZE_NEXT_TO_GAP;

    private ASTNode root1;
    private ASTNode root2;

    public CloneAnalysis() {
    }

    public CloneAnalysis(int minSize, int maxGap) {
        this.minSize = minSize;
        this.maxGap = maxGap;
    }

    /**
     * Check a fraction of the AST for internal clones, i.e., compare it against itself
     *
     * @param root starting point in the AST for the clone analysis
     * @return all clones found
     */
    public Set<CodeClone> check(ASTNode root, CodeClone.CloneType cloneType) {
        return check(root, root, cloneType);
    }

    /**
     * Check a pair of ASTs for clones.
     *
     * @param root1 first tree
     * @param root2 second tree
     * @return all clones found
     */
    public Set<CodeClone> check(ASTNode root1, ASTNode root2, CodeClone.CloneType cloneType) {

        this.root1 = root1;
        this.root2 = root2;

        // We compare at the level of statements, not tokens
        StatementListVisitor statementListVisitor = new StatementListVisitor();
        List<Stmt> statements1 = statementListVisitor.getStatements(root1);
        List<Stmt> statements2 = statementListVisitor.getStatements(root2);

        // For comparison, we normalize literals and tokens
        NormalizationVisitor normalizationVisitor = new NormalizationVisitor();
        List<Stmt> normalizedStatements1 = statements1.stream().map(normalizationVisitor::apply).collect(Collectors.toList());
        List<Stmt> normalizedStatements2 = statements2.stream().map(normalizationVisitor::apply).collect(Collectors.toList());

        // Comparison matrix on the normalized statements
        boolean[][] similarityMatrix = getSimilarityMatrix(normalizedStatements1, normalizedStatements2, root1 == root2);

        // Return all clones identifiable in the matrix
        Set<CodeClone> allClones = getAllClones(statements1, statements2, similarityMatrix, cloneType);

        if (root1 == root2) {
            // If a script is compared against itself, then there will always be a trivial type 1 clone
            allClones = allClones.stream().filter(c -> c.size() != statements1.size()).collect(Collectors.toSet());

            // Also exclude clones within a script that overlap
            allClones = allClones.stream().filter(c -> !hasOverlap(c.getFirstStatements(), c.getSecondStatements())).collect(Collectors.toSet());
        }

        return allClones;
    }

    private boolean hasOverlap(List<Stmt> statements1, List<Stmt> statements2) {
        for (Stmt stmt1 : statements1) {
            for (Stmt stmt2 : statements2) {
                if (stmt1 == stmt2) {
                    return true;
                }
            }
        }
        return false;
        // TODO: Why does this not work with identity hashsets?
//        Set<Stmt> statements = Sets.newIdentityHashSet();
//        statements.addAll(statements1);
//        statements..removeAll(statements2);
//        return statements.size() != statements1.size();
    }

    private boolean[][] getSimilarityMatrix(List<Stmt> normalizedStatements1, List<Stmt> normalizedStatements2, boolean isComparisonWithSelf) {
        int width = normalizedStatements1.size();
        int height = normalizedStatements2.size();
        boolean[][] matrix = new boolean[width][height];

        for (int i = 0; i < width; i++) {
            int start = isComparisonWithSelf ? i : 0;
            for (int j = start; j < height; j++) {
                matrix[i][j] = normalizedStatements1.get(i).equals(normalizedStatements2.get(j));
            }
        }

        return matrix;
    }

    private Set<CodeClone> getAllClones(List<Stmt> statements1, List<Stmt> statements2, boolean[][] similarityMatrix, CodeClone.CloneType cloneType) {
        Set<CodeClone> clones = new LinkedHashSet<>();

        Set<CloneBlock> blocks = getAllBlocks(similarityMatrix);
        for (CloneBlock block : blocks) {

            if (cloneType == CodeClone.CloneType.TYPE3) {
                // Type 3
                if (block.size() < minSizeNextToGap) {
                    continue;
                }
                for (CloneBlock otherBlock : getNeighbouringBlocks(block, blocks)) {
                    CodeClone clone = new CodeClone(root1, root2);
                    clone.setType(CodeClone.CloneType.TYPE3);
                    block.fillClone(clone, statements1, statements2);
                    otherBlock.fillClone(clone, statements1, statements2);
                    if (clone.size() >= minSize) {
                        clones.add(clone);
                    }
                }
            } else {
                // Type 1/2
                if (block.size() >= minSize) {
                    CodeClone clone = new CodeClone(root1, root2);
                    block.fillClone(clone, statements1, statements2);
                    if (!clone.getFirstStatements().equals(clone.getSecondStatements())) {
                        if (cloneType == CodeClone.CloneType.TYPE1) {
                            continue;
                        }
                        clone.setType(CodeClone.CloneType.TYPE2);
                    }
                    clones.add(clone);
                }
            }
        }

        return clones;
    }

    private Set<CloneBlock> getNeighbouringBlocks(CloneBlock block, Set<CloneBlock> otherBlocks) {
        Set<CloneBlock> cloneBlocks = new LinkedHashSet<>();

        int size = block.size();
        for (CloneBlock otherBlock : otherBlocks) {
            if (otherBlock == block) {
                continue;
            }

            int otherSize = otherBlock.size();
            if (otherSize < minSizeNextToGap) {
                continue;
            }

            if (size + otherSize < minSize) {
                continue;
            }

            if (otherBlock.extendsWithGap(block, maxGap)) {
                cloneBlocks.add(otherBlock);
            }
        }

        return cloneBlocks;
    }

    private Set<CloneBlock> getAllBlocks(boolean[][] similarityMatrix) {
        Set<CloneBlock> cloneBlocks = new LinkedHashSet<>();
        int width = similarityMatrix.length;
        if (width == 0) {
            // Empty matrix
            return cloneBlocks;
        }
        int height = similarityMatrix[0].length;

        boolean[][] coveredFields = new boolean[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (coveredFields[i][j]) {
                    continue;
                }
                CloneBlock block = getBlockAt(similarityMatrix, i, j);
                if (block.size() > 0) {
                    cloneBlocks.add(block);
                    block.fillPositionMap(coveredFields);
                }
            }
        }
        return cloneBlocks;
    }

    private CloneBlock getBlockAt(boolean[][] similarityMatrix, int x, int y) {
        CloneBlock block = new CloneBlock();
        while (x < similarityMatrix.length && y < similarityMatrix[x].length && similarityMatrix[x][y]) {
            block.add(x, y);
            x++;
            y++;
        }
        return block;
    }

}
