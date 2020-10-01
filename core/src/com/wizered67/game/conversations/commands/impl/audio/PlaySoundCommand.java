package com.wizered67.game.conversations.commands.impl.audio;

import com.badlogic.gdx.audio.Sound;
import com.wizered67.game.VNHubManager;
import com.wizered67.game.conversations.CompleteEvent;
import com.wizered67.game.conversations.ConversationController;
import com.wizered67.game.conversations.commands.ConversationCommand;

/**
 * A ConversationCommand that plays a sound effect.
 * @author Adam Victor
 */
public class PlaySoundCommand implements ConversationCommand {
    /** Identifier of the sound to play. */
    private String sound;

    /** No arguments constructor. */
    public PlaySoundCommand() {
        sound = "";
    }
    /** Creates a new PlaySoundCommand that plays the sound S when executed. */
    public PlaySoundCommand(String s) {
        sound = s;
    }
    /** Executes the command on the CONVERSATION CONTROLLER. */
    @Override
    public void execute(ConversationController conversationController) {
        if (VNHubManager.assetManager().isLoaded(sound)) {
            Sound s = VNHubManager.assetManager().get(sound, Sound.class);
            s.play();
        } else {
            VNHubManager.error("No sound loaded: " + sound);
        }
    }
    /** Whether to wait before proceeding to the next command in the branch. */
    @Override
    public boolean waitToProceed() {
        return false;
    }
    /** Checks whether the CompleteEvent C completes this command,
     * and if so acts accordingly. */
    @Override
    public void complete(CompleteEvent c) {

    }
}
