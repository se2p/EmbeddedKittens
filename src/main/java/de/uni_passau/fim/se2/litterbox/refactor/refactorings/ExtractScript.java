package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class ExtractScript implements Refactoring {

    private ActorDefinition stage;
    private final Script script;
    private final ScriptList scriptList;
    private static final String NAME = "extract_script";

    public ExtractScript(Script script) {
        this.script = Preconditions.checkNotNull(script);
        this.scriptList = (ScriptList) script.getParentNode();
    }

    @Override
    public Program apply(Program program) {
        getStage(program);
        addScriptToStage();
        removeScriptFromSprite();
        return program.deepCopy();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        String scriptString = script.getScratchBlocks();
        return NAME + " on script:\n" + scriptString + "\n";
    }

    private void getStage(Program program) {
        ActorDefinitionList actors = program.getActorDefinitionList();
        for (ActorDefinition actor : actors.getDefinitions()) {
            if (actor.isStage()) {
                stage = actor;
                break;
            }
        }
    }

    private void addScriptToStage() {
        stage.getScripts().getScriptList().add(script);
    }

    private void removeScriptFromSprite() {
        scriptList.getScriptList().remove(script);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ExtractScript)) {
            return false;
        }
        return script.equals(((ExtractScript) other).script);
    }

    @Override
    public int hashCode() {
        return script.hashCode();
    }
}
