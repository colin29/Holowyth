package com.wizered67.vnlib.conversations.commands.factories;

import com.badlogic.gdx.utils.XmlReader;
import com.wizered67.vnlib.conversations.commands.impl.scene.SetSceneCommand;
import com.wizered67.vnlib.conversations.xmlio.ConversationLoader;

/**
 * Factory for creating a SetSceneCommand from an XML element.
 *
 * @author Adam Victor
 */
public class SetSceneCommandFactory implements ConversationCommandFactory<SetSceneCommand> {
    private final static SetSceneCommandFactory INSTANCE = new SetSceneCommandFactory();

    public static SetSceneCommandFactory getInstance() {
        return INSTANCE;
    }

    private SetSceneCommandFactory() {
    }

    @Override
    public SetSceneCommand makeCommand(ConversationLoader loader, XmlReader.Element element) {
        String name = element.getAttribute("name");
        return new SetSceneCommand(name);
    }
}