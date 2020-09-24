package com.wizered67.game.conversations.commands.impl.loading;

import com.wizered67.game.VNHubManager;
import com.wizered67.game.conversations.CompleteEvent;
import com.wizered67.game.conversations.ConversationController;
import com.wizered67.game.conversations.commands.ConversationCommand;

/**
 * Multi-purpose ConversationCommand that can both load or unload resources or groups of resources.
 * @author Adam Victor
 */
public class LoadUnloadCommand implements ConversationCommand {
    private String resourceName;
    private boolean isGroup;
    private boolean isLoad;
    public LoadUnloadCommand() {}

    public LoadUnloadCommand(String name, boolean group, boolean load) {
        resourceName = name;
        isGroup = group;
        isLoad = load;
    }
    /**
     * Executes the command on the CONVERSATION CONTROLLER.
     */
    @Override
    public void execute(ConversationController conversationController) {
        if (isLoad) {
            if (isGroup) {
                VNHubManager.assetManager().loadGroup(resourceName);
            } else {
                VNHubManager.assetManager().load(resourceName);
            }
        } else {
            if (isGroup) {
                VNHubManager.assetManager().unloadGroup(resourceName);
            } else {
                VNHubManager.assetManager().unload(resourceName);
            }
        }
    }

    /**
     * Whether to wait before proceeding to the next command in the branch.
     */
    @Override
    public boolean waitToProceed() {
        return false;
    }

    /**
     * Checks whether the CompleteEvent C completes this command,
     * and if so acts accordingly.
     */
    @Override
    public void complete(CompleteEvent c) {

    }
}
