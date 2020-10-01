package com.wizered67.vnlib.saving;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.minlog.Log;
import com.wizered67.vnlib.MusicManager;
import com.wizered67.vnlib.VNHubManager;
import com.wizered67.vnlib.assets.Assets;
import com.wizered67.vnlib.conversations.Conversation;
import com.wizered67.vnlib.conversations.ConversationController;
import com.wizered67.vnlib.conversations.scene.SceneEntity;
import com.wizered67.vnlib.conversations.scene.SceneManager;
import com.wizered67.vnlib.gui.GUIManager;
import com.wizered67.vnlib.saving.serializers.*;
import com.wizered67.vnlib.saving.serializers.luaserializers.LuaBooleanSerializer;
import com.wizered67.vnlib.saving.serializers.luaserializers.LuaDoubleSerializer;
import com.wizered67.vnlib.saving.serializers.luaserializers.LuaIntegerSerializer;
import com.wizered67.vnlib.saving.serializers.luaserializers.LuaStringSerializer;
import com.wizered67.vnlib.scripting.GameScript;
import com.wizered67.vnlib.scripting.ScriptManager;

import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaString;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Saves and loads serialized data using Kryo.
 * @author Adam Victor
 */
public class SaveManager {
    private static Kryo kryo = new Kryo();
    /** Adds all necessary serializers to Kryo for serializing important objects. */
    public static void init() {
        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        kryo.addDefaultSerializer(MusicManager.class, MusicManagerSerializer.class);
        kryo.addDefaultSerializer(ConversationController.class, ConversationControllerSerializer.class);
        kryo.addDefaultSerializer(SceneManager.class, SceneManagerSerializer.class);
        kryo.addDefaultSerializer(LuaBoolean.class, LuaBooleanSerializer.class);
        kryo.addDefaultSerializer(LuaDouble.class, LuaDoubleSerializer.class);
        kryo.addDefaultSerializer(LuaInteger.class, LuaIntegerSerializer.class);
        kryo.addDefaultSerializer(LuaString.class, LuaStringSerializer.class);
        kryo.addDefaultSerializer(GameScript.class, GameScriptSerializer.class);
        kryo.addDefaultSerializer(ScriptManager.class, ScriptManagerSerializer.class);
        kryo.addDefaultSerializer(Conversation.class, ConversationSerializer.class);
        kryo.addDefaultSerializer(Sprite.class, SpriteSerializer.class);
        kryo.addDefaultSerializer(SceneEntity.class, SceneEntitySerializer.class);
        kryo.addDefaultSerializer(Assets.class, AssetsSerializer.class);
        kryo.addDefaultSerializer(OrthographicCamera.class, OrthographicCameraSerializer.class);

        kryo.setReferences(true);
        Log.set(Log.LEVEL_TRACE);
        kryo.register(Color.class, new Serializer<Color>() {
            public Color read (Kryo kryo, Input input, Class<Color> type) {
                Color color = new Color();
                Color.rgba8888ToColor(color, input.readInt());
                return color;
            }

            public void write (Kryo kryo, Output output, Color color) {
                output.writeInt(Color.rgba8888(color));
            }
        });
    }
    /** Saves all game data to the file FILEHANDLE. */
    public static void save(FileHandle fileHandle) {
        SaveData data = new SaveData();
        data.musicManager = VNHubManager.musicManager();
        ConversationController conversationController = VNHubManager.conversationController();
        data.conversationController = conversationController;
        data.assets = VNHubManager.assetManager();
        //LuaScriptManager sm = (LuaScriptManager) ConversationController.scriptManager("Lua");
        Map<String, ScriptManager> managers = ConversationController.allScriptManagers();
        for (String name : managers.keySet()) {
            data.scriptingVariables.put(name, managers.get(name).saveMap());
        }
        saveData(fileHandle, data);
    }
    /** Writes all data in SAVEDATA to the file FILEHANDLE. */
    private static void saveData(FileHandle fileHandle, SaveData saveData) {
        Output output = new Output(new DeflaterOutputStream(fileHandle.write(false)));
        kryo.writeObject(output, saveData);
        output.close();
    }
    /** Loads all game data from the file FILEHANDLE. */
    public static void load(FileHandle fileHandle) {
        SaveData saveData = loadData(fileHandle);
        for (String name : saveData.scriptingVariables.keySet()) {
            Map<String, Object> variables = saveData.scriptingVariables.get(name);
            ConversationController.scriptManager(name).reload(variables);
        }
    }
    /** Returns a SaveData object with the data from file FILEHANDLE. */
    public static SaveData loadData(FileHandle fileHandle) {
        Input input = new Input(new InflaterInputStream(fileHandle.read()));
        SaveData data = kryo.readObject(input, SaveData.class);
        input.close();
        return data;
    }
}
