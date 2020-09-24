package com.wizered67.game.saving.serializers;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.wizered67.game.VNHubManager;
import com.wizered67.game.conversations.Conversation;
import com.wizered67.game.conversations.xmlio.ConversationParsingException;

import java.util.HashMap;

/**
 * Serializer used for saving and loading Conversations. Only stores the
 * assignments map and name of conversation and uses these to reload.
 * @author Adam Victor
 */
public class ConversationSerializer extends Serializer<Conversation> {

    @Override
    public void write (Kryo kryo, Output output, Conversation object) {
        output.writeString(object.getName());
        kryo.writeObjectOrNull(output, object.getAllAssignments(), HashMap.class);
    }
    @Override
    public Conversation read (Kryo kryo, Input input, Class<Conversation> type) {
        String filename = input.readString();
        HashMap assignments = kryo.readObjectOrNull(input, HashMap.class);
        try {
            Conversation conversation = VNHubManager.conversationController().loadConversation(filename);
            conversation.setAssignments(assignments);
            return conversation;
        } catch (ConversationParsingException e) {
            throw new GdxRuntimeException("Could not load conversation " + filename);
        }
    }
}
