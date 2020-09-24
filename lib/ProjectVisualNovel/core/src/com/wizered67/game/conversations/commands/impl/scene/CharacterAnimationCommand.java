package com.wizered67.game.conversations.commands.impl.scene;

import com.wizered67.game.conversations.CompleteEvent;
import com.wizered67.game.conversations.ConversationController;
import com.wizered67.game.conversations.commands.ConversationCommand;
import com.wizered67.game.conversations.scene.SceneCharacter;

/**
 * A ConversationCommand that changes an existing SceneCharacter's animation.
 * @author Adam Victor
 */
public class CharacterAnimationCommand implements ConversationCommand {
    /** Whether this command is done executing. If wait is false, then automatically true.
     * Otherwise only true once the Animation has finished. */
    private boolean done;
    /** The name of the animation to switch to. */
    private String animationId;
    /** Whether to wait for the Animation to complete before moving to the next
     * ConversationCommand. */
    private boolean wait;
    /** The identifier of the SceneCharacter that should have its animation changed. */
    private String characterId;

    /** No arguments constructor. */
    public CharacterAnimationCommand() {
        wait = false;
        done = true;
        animationId = "";
        characterId = "";
    }

    /** Creates a new CharacterAnimationCommand that changes SceneCharacter NAME's
     * animation to ANIM when executed. Waits to complete before going to the next ConversationCommand
     * iff W.
     */
    public CharacterAnimationCommand(String characterId, String animationId, boolean w) {
        wait = w;
        done = !wait;
        this.animationId = animationId;
        this.characterId = characterId;
    }
    /** Executes the command on the CONVERSATION CONTROLLER. */
    @Override
    public void execute(ConversationController conversationController) {
        SceneCharacter c = conversationController.currentSceneManager().getOrAddCharacterByIdentifier(characterId);
        if (c == null) {
            done = true;
            return;
        }

        done = !wait;
        if (!c.setCurrentAnimation(animationId)) {
            done = true;
        }
    }
    /** Whether to wait before proceeding to the next command in the branch. */
    @Override
    public boolean waitToProceed() {
        return !done;
    }
    /** Checks whether the CompleteEvent C completes this command,
     * and if so acts accordingly. */
    @Override
    public void complete(CompleteEvent c) {
        if (c.type == CompleteEvent.Type.ANIMATION_END) {
            if (c.data.equals(animationId)) {
                done = true;
            }

        }
    }
}
