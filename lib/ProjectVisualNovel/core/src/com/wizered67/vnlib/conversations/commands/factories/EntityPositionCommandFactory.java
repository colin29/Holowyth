package com.wizered67.vnlib.conversations.commands.factories;

import com.badlogic.gdx.utils.XmlReader;
import com.wizered67.vnlib.conversations.commands.impl.scene.EntityPositionCommand;
import com.wizered67.vnlib.conversations.xmlio.ConversationLoader;

/**
 * Factory for creating a EntityPositionCommand from an XML element.
 *
 * @author Adam Victor
 */
public class EntityPositionCommandFactory implements ConversationCommandFactory<EntityPositionCommand> {
    private final static EntityPositionCommandFactory INSTANCE = new EntityPositionCommandFactory();

    public static EntityPositionCommandFactory getInstance() {
        return INSTANCE;
    }

    private EntityPositionCommandFactory() {
    }

    @Override
    public EntityPositionCommand makeCommand(ConversationLoader loader, XmlReader.Element element) {
        return EntityPositionCommand.makeCommand(null, element);
    }
}