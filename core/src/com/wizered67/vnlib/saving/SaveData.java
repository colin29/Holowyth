package com.wizered67.vnlib.saving;

import java.util.HashMap;
import java.util.Map;

import com.wizered67.vnlib.MusicManager;
import com.wizered67.vnlib.assets.Assets;
import com.wizered67.vnlib.conversations.ConversationController;

/**
 * Stores important game information to be serialized for saving and loading.
 * @author Adam Victor
 */
public class SaveData {
    public MusicManager musicManager;
    public ConversationController conversationController;
    /** Maps scripting language names to a map of variable names to values. */
    public Map<String, Map<String, Object>> scriptingVariables = new HashMap<String, Map<String, Object>>();
    public Assets assets;
}
