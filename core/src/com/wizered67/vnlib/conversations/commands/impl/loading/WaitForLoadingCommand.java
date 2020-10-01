package com.wizered67.vnlib.conversations.commands.impl.loading;

import com.wizered67.vnlib.VNHubManager;
import com.wizered67.vnlib.conversations.CompleteEvent;
import com.wizered67.vnlib.conversations.ConversationController;
import com.wizered67.vnlib.conversations.commands.ConversationCommand;

/**
 * Conversation Command that waits until all queued resources have been loaded.
 * @author Adam Victor
 */
public class WaitForLoadingCommand implements ConversationCommand {

    public WaitForLoadingCommand() {

    }

    /**
     * Executes the command on the CONVERSATION CONTROLLER.
     */
    @Override
    public void execute(ConversationController conversationController) {

    }

    /**
     * Whether to wait before proceeding to the next command in the branch.
     */
    @Override
    public boolean waitToProceed() {
        return VNHubManager.assetManager().getQueuedAssets() != 0;
    }

    /**
     * Checks whether the CompleteEvent C completes this command,
     * and if so acts accordingly.
     */
    @Override
    public void complete(CompleteEvent c) {

    }
}
