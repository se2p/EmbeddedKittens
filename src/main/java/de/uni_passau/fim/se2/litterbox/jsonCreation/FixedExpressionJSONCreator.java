package de.uni_passau.fim.se2.litterbox.jsonCreation;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Next;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Prev;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Random;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.CloneOfMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Edge;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.MousePointer;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.SpriteTouchable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.jsonCreation.BlockJsonCreatorHelper.createBlockWithoutMutationString;
import static de.uni_passau.fim.se2.litterbox.jsonCreation.BlockJsonCreatorHelper.createFields;
import static de.uni_passau.fim.se2.litterbox.jsonCreation.StmtListJSONCreator.EMPTY_VALUE;


/**
 * This class is for creating the JSONs of predetermined drop downs, that could be replaced by expressions such as
 * {@link de.uni_passau.fim.se2.litterbox.ast.model.position.Position} or
 * {@link de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice}.
 */
public class FixedExpressionJSONCreator implements ScratchVisitor {
    private List<String> finishedJSONStrings;
    private String previousBlockId = null;
    private String topExpressionId = null;

    public IdJsonStringTuple createFixedExpressionJSON(String parentId, ASTNode expression) {
        finishedJSONStrings = new ArrayList<>();
        topExpressionId = null;
        previousBlockId = parentId;
        expression.accept(this);
        StringBuilder jsonString = new StringBuilder();
        for (int i = 0; i < finishedJSONStrings.size() - 1; i++) {
            jsonString.append(finishedJSONStrings.get(i)).append(",");
        }
        if (finishedJSONStrings.size() > 0) {
            jsonString.append(finishedJSONStrings.get(finishedJSONStrings.size() - 1));
        }
        return new IdJsonStringTuple(topExpressionId, jsonString.toString());
    }

    @Override
    public void visit(RandomPos node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), RANDOM);
    }

    @Override
    public void visit(MousePos node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), MOUSE);
    }

    @Override
    public void visit(FromExpression node) {
        if (node.getStringExpr() instanceof AsString) {
            AsString asString = (AsString) node.getStringExpr();
            if (asString.getOperand1() instanceof StrId) {
                createFieldsExpression((NonDataBlockMetadata) node.getMetadata(),
                        ((StrId) asString.getOperand1()).getName());
            }
        }
    }

    @Override
    public void visit(Next node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), NEXT_BACKDROP);
    }

    @Override
    public void visit(Prev node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), PREVIOUS_BACKDROP);
    }

    @Override
    public void visit(Random node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), RANDOM_BACKDROP);
    }

    @Override
    public void visit(WithExpr node) {
        if (node.getExpression() instanceof StrId) {
            createFieldsExpression((NonDataBlockMetadata) node.getMetadata(),
                    ((StrId) node.getExpression()).getName());
        }

    }

    @Override
    public void visit(CreateCloneOf node) {
        StringExpr stringExpr = node.getStringExpr();
        if (stringExpr instanceof AsString && ((AsString) stringExpr).getOperand1() instanceof StrId) {
            StrId strid = (StrId) ((AsString) node.getStringExpr()).getOperand1();
            CloneOfMetadata metadata = (CloneOfMetadata) node.getMetadata();
            createFieldsExpression((NonDataBlockMetadata) metadata.getCloneMenuMetadata(),
                    strid.getName());
        }
    }

    @Override
    public void visit(MousePointer node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), MOUSE);
    }

    @Override
    public void visit(Edge node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), TOUCHING_EDGE);
    }

    @Override
    public void visit(SpriteTouchable node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(),
                ((StringLiteral) node.getStringExpr()).getText());
    }

    private void createFieldsExpression(NonDataBlockMetadata metadata, String fieldsValue) {
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }

        String fieldsString = createFields(metadata.getFields().getList().get(0).getFieldsName(), fieldsValue, null);
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, EMPTY_VALUE, fieldsString));
    }
}
