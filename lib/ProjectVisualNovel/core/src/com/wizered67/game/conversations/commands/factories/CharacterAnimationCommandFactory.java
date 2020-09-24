package com.wizered67.game.conversations.commands.factories;

import com.badlogic.gdx.utils.XmlReader;
import com.wizered67.game.conversations.commands.impl.scene.CharacterAnimationCommand;
import com.wizered67.game.conversations.xmlio.ConversationLoader;

/**
 * Factory for creating a CharacterAnimationCommand from an XML element.
 * @author Adam Victor
 */
public class CharacterAnimationCommandFactory implements ConversationCommandFactory<CharacterAnimationCommand> {

    private final static CharacterAnimationCommandFactory INSTANCE = new CharacterAnimationCommandFactory();

    public static CharacterAnimationCommandFactory getInstance() {
        return INSTANCE;
    }

    private CharacterAnimationCommandFactory() {}

    @Override
    public CharacterAnimationCommand makeCommand(ConversationLoader loader, XmlReader.Element element) {
        String iden = element.getAttribute("id");
        String animation = element.getAttribute("animation", iden); //defaults to iden if not provided
        boolean wait = element.getBoolean("wait", false);
        return new CharacterAnimationCommand(iden, animation, wait);
    }
}