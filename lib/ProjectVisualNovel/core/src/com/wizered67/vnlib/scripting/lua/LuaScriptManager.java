package com.wizered67.vnlib.scripting.lua;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import org.luaj.vm2.lib.jse.JsePlatform;

import com.wizered67.vnlib.VNHubManager;
import com.wizered67.vnlib.scripting.GameScript;
import com.wizered67.vnlib.scripting.ScriptManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A ScriptManager that loads Lua scripts.
 * @author Adam Victor
 */
public class LuaScriptManager implements ScriptManager {
    static final String SCRIPT_DIRECTORY = "Scripts/";
    private final static Pattern CONTAINS_RETURN_PATTERN = Pattern.compile(".*return .*");
    /** Globals used for loading and executing Lua scripts. */
    Globals globals;
    private HashMap<String, GameScript> savedScripts;
    private Set<LuaValue> defaultKeys;
    /** Initializes globals to the Standard Globals. */
    public LuaScriptManager() {
        globals = JsePlatform.standardGlobals();
        setDefaultJavaMethods();
        defaultKeys = new HashSet<>();
        savedScripts = new HashMap<>();
        for (LuaValue key : globals.keys()) {
            defaultKeys.add(key);
        }
    }

    private void setDefaultJavaMethods() {
        globals.load(new com.wizered67.vnlib.scripting.lua.LuajavaGdxReflection());
        //LuaGameMethods luaGameMethods = new LuaGameMethods();
        globals.set("game", globals.get("luajava").get("bindClass").call(LuaValue.valueOf(VNHubManager.class.getName())));
    }

    /** Returns the name of the language used for this ScriptManager. */
    @Override
    public String name() {
        return "Lua";
    }

    @Override
    public GameScript load(String script, boolean isFile) {
        GameScript gs = new LuaScript(this, script, isFile);
        gs.isFile = isFile;
        gs.script = script;
        gs.language = "Lua";
        return gs;
    }

    /**
     * Loads and returns the GameScript SCRIPT as a condition script.
     * If ISFILE it loads it from the filed named SCRIPT. The script may be modified
     * to make it work as a condition.
     */
    @Override
    public GameScript loadConditionScript(String script, boolean isFile) {
        if (!isFile && !CONTAINS_RETURN_PATTERN.matcher(script).matches()) {
            script = "return " + script;
        }
        return load(script, isFile);
    }

    /** Returns the boolean value of Object O, where O is assumed to be some
     * type specific to the scripting language. */
    @Override
    public boolean objectToBoolean(Object o) {
        LuaValue value = (LuaValue) o;
        return value.toboolean();
    }
    /** Returns the string value of Object O, where O is assumed to be some
     * type specific to the scripting language. */
    @Override
    public String objectToString(Object o) {
        LuaValue value = (LuaValue) o;
        return value.toString();
    }

    /**
     * Returns the integer value of Object O, where O is assumed to be some
     * type specific to the scripting language.
     */
    @Override
    public int objectToInteger(Object o) {
        LuaValue value = (LuaValue) o;
        return value.toint();
    }

    /**
     * Returns the double value of Object O, where O is assumed to be some
     * type specific to the scripting language.
     */
    @Override
    public double objectToDouble(Object o) {
        LuaValue value = (LuaValue) o;
        return value.todouble();
    }

    /** Returns the value of variable VAR in a language specific object type. */
    @Override
    public LuaValue getLanguageValue(String var) {
        return globals.get(var);
    }

    /**
     * Returns the value of executing expression EXPR in a language specific object type.
     * Caches the script created.
     */
    @Override
    public Object getExpressionValue(String expr) {
        return getExpressionValue(expr, true);
    }

    /**
     * Returns the value of executing expression EXPR in a language specific object type.
     */
    @Override
    public Object getExpressionValue(String expr, boolean cache) {
        if (savedScripts.containsKey(expr)) {
            return savedScripts.get(expr).execute();
        }
        GameScript script;
        if (!expr.matches(".*return .*")) {
            script = load("return " + expr, false);
        } else {
            script = load(expr, false);
        }
        if (cache) {
            savedScripts.put(expr, script);
        }
        return script.execute();
    }

    /** Returns the value of VAR as type TYPE. */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(String var, Class<T> type) {
        return (T) CoerceLuaToJava.coerce(globals.get(var), type);
    }

    /**
     * Returns the value of variable VAR as a Java integer.
     */
    @Override
    public int getIntegerValue(String var) {
        return objectToInteger(getLanguageValue(var));
    }

    /**
     * Returns the value of variable VAR as a Java double.
     */
    @Override
    public double getDoubleValue(String var) {
        return objectToDouble(getLanguageValue(var));
    }

    /**
     * Returns the value of variable VAR as a Java String.
     */
    @Override
    public String getStringValue(String var) {
        return objectToString(getLanguageValue(var));
    }

    /**
     * Returns the value of variable VAR as a Java boolean.
     */
    @Override
    public boolean getBooleanValue(String var) {
        return objectToBoolean(getLanguageValue(var));
    }

    /** Returns whether a variable named VAR has been defined. */
    @Override
    public boolean isDefined(String var) {
        return !getLanguageValue(var).isnil();
    }
    /** Returns a GameScript that, when executed, assigns variable named VAR to VALUE. */
    @Override
    public GameScript createSetScript(String var, String value) {
        return new LuaScript(this, var + " = " + value, false);
    }
    @Override
    /** Returns a Map of variables names to values for variables that aren't predefined. */
    public Map<String, Object> saveMap() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        for (LuaValue key : globals.keys()) {
            if (!defaultKeys.contains(key)) {
                LuaValue value = globals.get(key);
                if (value != LuaValue.NIL) {
                    map.put(key.toString(), value);
                }
            }
        }
        return map;
    }

    /**
     * Reloads variables from the save map between variable name and value MAP.
     */
    @Override
    public void reload(Map<String, Object> map) {
        for (String key : map.keySet()) {
            globals.set(key, (LuaValue) map.get(key));
        }
    }
    /** Assign variable of name NAME to be equal to VALUE. */
    @Override
    public void setValue(String name, Object value) {
        globals.set(name, CoerceJavaToLua.coerce(value));
    }

}
