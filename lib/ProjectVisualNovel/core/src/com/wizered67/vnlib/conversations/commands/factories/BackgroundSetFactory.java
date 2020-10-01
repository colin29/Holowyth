package com.wizered67.vnlib.conversations.commands.factories;

import com.badlogic.gdx.utils.XmlReader;
import com.wizered67.vnlib.conversations.commands.impl.scene.BackgroundSetCommand;
import com.wizered67.vnlib.conversations.xmlio.ConversationLoader;

/**
 * Factory for creating a BackgroundSetCommand from an XML element.
 * @author Adam Victor
 */
public class BackgroundSetFactory implements ConversationCommandFactory<BackgroundSetCommand> {

    private final static BackgroundSetFactory INSTANCE = new BackgroundSetFactory();

    public static BackgroundSetFactory getInstance() {
        return INSTANCE;
    }

    private BackgroundSetFactory() {}

    public BackgroundSetCommand makeCommand(ConversationLoader loader, XmlReader.Element element) {
        String id = element.getAttribute("id");
        float xPos = element.getFloatAttribute("x", 0);
        float yPos = element.getFloatAttribute("y", 0);
        return new BackgroundSetCommand(id, xPos, yPos);
    }
}
