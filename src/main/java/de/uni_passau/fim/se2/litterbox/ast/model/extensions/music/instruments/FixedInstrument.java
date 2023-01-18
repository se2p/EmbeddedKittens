package de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MusicExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class FixedInstrument extends AbstractNode implements Instrument, ASTLeaf {
    private final BlockMetadata metadata;
    private FixedInstrumentType type;

    public FixedInstrument(String typeName, BlockMetadata metadata) {
        super(metadata);
        this.type = FixedInstrumentType.fromString(typeName);
        this.metadata = metadata;
    }

    public FixedInstrumentType getType() {
        return type;
    }

    @Override
    public BlockMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(MusicExtensionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }

    public enum FixedInstrumentType {

        ONE("1"), TWO("2"), THREE("3"), FOUR("4"), FIVE("5"), SIX("6"),
        SEVEN("7"), EIGHT("8"), NINE("9"), TEN("10"), ELEVEN("11"), TWELVE("12"),
        THIRTEEN("13"), FOURTEEN("14"), FIFTEEN("15"), SIXTEEN("16"), SEVENTEEN("17"), EIGHTEEN("18"),
        NINETEEN("19"), TWENTY("20"), TWENTYONE("21");

        private final String type;

        FixedInstrumentType(String type) {
            this.type = Preconditions.checkNotNull(type);
        }

        public static FixedInstrumentType fromString(String type) {
            for (FixedInstrumentType f : values()) {
                if (f.getType().equals(type.toLowerCase())) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown FixedInstrument: " + type);
        }

        public String getType() {
            return type;
        }
    }
}
