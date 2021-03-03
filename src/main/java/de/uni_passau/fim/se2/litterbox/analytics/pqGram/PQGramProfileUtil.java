package de.uni_passau.fim.se2.litterbox.analytics.pqGram;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;

import java.util.ArrayList;
import java.util.List;

public final class PQGramProfileUtil {
    private static int p = 2;
    private static int q = 3;
    public static final String NULL_NODE = "*";

    private PQGramProfileUtil() {
    }

    public static PQGramProfile createPQProfile(ASTNode node) {
        PQGramProfile profile = new PQGramProfile();
        List<Label> anc = new ArrayList<>();
        for (int i = 0; i < p; i++) {
            anc.add(new Label(NULL_NODE));
        }

        profile = profileStep(profile, node, getBlockName(node), anc);
        return profile;
    }

    public static double calculateDistance(PQGramProfile profile1, PQGramProfile profile2) {
        if (profile1.getTuples().isEmpty() && profile2.getTuples().isEmpty()) {
            return 0;
        }
        Multiset<LabelTuple> intersection = HashMultiset.create(profile1.getTuples());
        intersection.retainAll(profile2.getTuples());
        double division = (double) intersection.size() / (profile1.getTuples().size() + profile2.getTuples().size());
        return 1 - (2 * division);
    }

    public static double calculateDistance(ASTNode node1, ASTNode node2) {
        return calculateDistance(createPQProfile(node1), createPQProfile(node2));
    }

    private static String getBlockName(ASTNode node) {
        String blockName;
        blockName = node.getClass().getSimpleName();
        return blockName;
    }

    private static PQGramProfile profileStep(PQGramProfile profile, ASTNode root, String rootLabel, List<Label> anc) {
        List<Label> ancHere = new ArrayList<>(anc);
        shift(ancHere, new Label(rootLabel));
        List<Label> sib = new ArrayList<>();
        for (int i = 0; i < q; i++) {
            sib.add(new Label(NULL_NODE));
        }

        List<ASTNode> children = (List<ASTNode>) root.getChildren();
        if (children.size() == 0) {
            profile.addLabelTuple(new LabelTuple(ancHere, sib));
        } else {

            for (ASTNode child : children) {
                String blockName = getBlockName(child);
                shift(sib, new Label(blockName));
                profile.addLabelTuple(new LabelTuple(ancHere, sib));
                profile = profileStep(profile, child, blockName, ancHere);
            }
            for (int k = 0; k < q - 1; k++) {
                shift(sib, new Label(NULL_NODE));
                profile.addLabelTuple(new LabelTuple(ancHere, sib));
            }
        }
        return profile;
    }

    private static void shift(List<Label> register, Label label) {
        register.remove(0);
        register.add(label);
    }

    public static void setP(int p) {
        PQGramProfileUtil.p = p;
    }

    public static void setQ(int q) {
        PQGramProfileUtil.q = q;
    }

    public static int getP() {
        return p;
    }

    public static int getQ() {
        return q;
    }
}
