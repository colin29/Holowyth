package com.wizered67.vnlib.conversations.xmlio;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.StringBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Modification of the LibGDX XmlReader that separates text into a child element.
 * This allows for maintaining order of text and elements when dealing with mixed elements.
 * As a consequence, Element#getText will return null for all elements besides text elements.
 * Most of the code is redundant and the only changes are in the MixedXmlReader#text method.
 * @author Nathan Sweet and Adam Victor
 */
public class MixedXmlReader extends XmlReader {
    private final Array<Element> elements = new Array(8);
    private Element root, current;
    private final StringBuilder textBuffer = new StringBuilder(64);

    public Element parse(String xml) {
        char[] data = xml.toCharArray();
        return parse(data, 0, data.length);
    }

    public Element parse(Reader reader) {
        try {
            char[] data = new char[1024];
            int offset = 0;
            while (true) {
                int length = reader.read(data, offset, data.length - offset);
                if (length == -1) break;
                if (length == 0) {
                    char[] newData = new char[data.length * 2];
                    System.arraycopy(data, 0, newData, 0, data.length);
                    data = newData;
                } else
                    offset += length;
            }
            return parse(data, 0, offset);
        } catch (IOException ex) {
            throw new SerializationException(ex);
        } finally {
            StreamUtils.closeQuietly(reader);
        }
    }

    public Element parse(InputStream input) {
        try {
            return parse(new InputStreamReader(input, "UTF-8"));
        } catch (IOException ex) {
            throw new SerializationException(ex);
        } finally {
            StreamUtils.closeQuietly(input);
        }
    }

    public Element parse(FileHandle file) {
        try {
            return parse(file.reader("UTF-8"));
        } catch (Exception ex) {
            throw new SerializationException("Error parsing file: " + file, ex);
        }
    }

    public Element parse(char[] data, int offset, int length) {
        int cs, p = offset, pe = length;

        int s = 0;
        String attributeName = null;
        boolean hasBody = false;

        // line 93 "XmlReader.java"
        {
            cs = xml_start;
        }

        // line 97 "XmlReader.java"
        {
            int _klen;
            int _trans = 0;
            int _acts;
            int _nacts;
            int _keys;
            int _goto_targ = 0;

            _goto:
            while (true) {
                switch (_goto_targ) {
                    case 0:
                        if (p == pe) {
                            _goto_targ = 4;
                            continue _goto;
                        }
                        if (cs == 0) {
                            _goto_targ = 5;
                            continue _goto;
                        }
                    case 1:
                        _match:
                        do {
                            _keys = _xml_key_offsets[cs];
                            _trans = _xml_index_offsets[cs];
                            _klen = _xml_single_lengths[cs];
                            if (_klen > 0) {
                                int _lower = _keys;
                                int _mid;
                                int _upper = _keys + _klen - 1;
                                while (true) {
                                    if (_upper < _lower) break;

                                    _mid = _lower + ((_upper - _lower) >> 1);
                                    if (data[p] < _xml_trans_keys[_mid])
                                        _upper = _mid - 1;
                                    else if (data[p] > _xml_trans_keys[_mid])
                                        _lower = _mid + 1;
                                    else {
                                        _trans += (_mid - _keys);
                                        break _match;
                                    }
                                }
                                _keys += _klen;
                                _trans += _klen;
                            }

                            _klen = _xml_range_lengths[cs];
                            if (_klen > 0) {
                                int _lower = _keys;
                                int _mid;
                                int _upper = _keys + (_klen << 1) - 2;
                                while (true) {
                                    if (_upper < _lower) break;

                                    _mid = _lower + (((_upper - _lower) >> 1) & ~1);
                                    if (data[p] < _xml_trans_keys[_mid])
                                        _upper = _mid - 2;
                                    else if (data[p] > _xml_trans_keys[_mid + 1])
                                        _lower = _mid + 2;
                                    else {
                                        _trans += ((_mid - _keys) >> 1);
                                        break _match;
                                    }
                                }
                                _trans += _klen;
                            }
                        } while (false);

                        _trans = _xml_indicies[_trans];
                        cs = _xml_trans_targs[_trans];

                        if (_xml_trans_actions[_trans] != 0) {
                            _acts = _xml_trans_actions[_trans];
                            _nacts = (int) _xml_actions[_acts++];
                            while (_nacts-- > 0) {
                                switch (_xml_actions[_acts++]) {
                                    case 0:
                                        // line 94 "XmlReader.rl"
                                    {
                                        s = p;
                                    }
                                    break;
                                    case 1:
                                        // line 95 "XmlReader.rl"
                                    {
                                        char c = data[s];
                                        if (c == '?' || c == '!') {
                                            if (data[s + 1] == '[' && //
                                                    data[s + 2] == 'C' && //
                                                    data[s + 3] == 'D' && //
                                                    data[s + 4] == 'A' && //
                                                    data[s + 5] == 'T' && //
                                                    data[s + 6] == 'A' && //
                                                    data[s + 7] == '[') {
                                                s += 8;
                                                p = s + 2;
                                                while (data[p - 2] != ']' || data[p - 1] != ']' || data[p] != '>')
                                                    p++;
                                                text(new String(data, s, p - s - 2));
                                            } else if (c == '!' && data[s + 1] == '-' && data[s + 2] == '-') {
                                                p = s + 3;
                                                while (data[p] != '-' || data[p + 1] != '-' || data[p + 2] != '>')
                                                    p++;
                                                p += 2;
                                            } else
                                                while (data[p] != '>')
                                                    p++;
                                            {
                                                cs = 15;
                                                _goto_targ = 2;
                                                if (true) continue _goto;
                                            }
                                        }
                                        hasBody = true;
                                        open(new String(data, s, p - s));
                                    }
                                    break;
                                    case 2:
                                        // line 125 "XmlReader.rl"
                                    {
                                        hasBody = false;
                                        close();
                                        {
                                            cs = 15;
                                            _goto_targ = 2;
                                            if (true) continue _goto;
                                        }
                                    }
                                    break;
                                    case 3:
                                        // line 130 "XmlReader.rl"
                                    {
                                        close();
                                        {
                                            cs = 15;
                                            _goto_targ = 2;
                                            if (true) continue _goto;
                                        }
                                    }
                                    break;
                                    case 4:
                                        // line 134 "XmlReader.rl"
                                    {
                                        if (hasBody) {
                                            cs = 15;
                                            _goto_targ = 2;
                                            if (true) continue _goto;
                                        }
                                    }
                                    break;
                                    case 5:
                                        // line 137 "XmlReader.rl"
                                    {
                                        attributeName = new String(data, s, p - s);
                                    }
                                    break;
                                    case 6:
                                        // line 140 "XmlReader.rl"
                                    {
                                        attribute(attributeName, new String(data, s, p - s));
                                    }
                                    break;
                                    case 7:
                                        // line 143 "XmlReader.rl"
                                    {
                                        int end = p;
                                        while (end != s) {
                                            switch (data[end - 1]) {
                                                case ' ':
                                                case '\t':
                                                case '\n':
                                                case '\r':
                                                    end--;
                                                    continue;
                                            }
                                            break;
                                        }
                                        int current = s;
                                        boolean entityFound = false;
                                        while (current != end) {
                                            if (data[current++] != '&') continue;
                                            int entityStart = current;
                                            while (current != end) {
                                                if (data[current++] != ';') continue;
                                                textBuffer.append(data, s, entityStart - s - 1);
                                                String name = new String(data, entityStart, current - entityStart - 1);
                                                String value = entity(name);
                                                textBuffer.append(value != null ? value : name);
                                                s = current;
                                                entityFound = true;
                                                break;
                                            }
                                        }
                                        if (entityFound) {
                                            if (s < end) textBuffer.append(data, s, end - s);
                                            text(textBuffer.toString());
                                            textBuffer.setLength(0);
                                        } else
                                            text(new String(data, s, end - s));
                                    }
                                    break;
                                    // line 286 "XmlReader.java"
                                }
                            }
                        }

                    case 2:
                        if (cs == 0) {
                            _goto_targ = 5;
                            continue _goto;
                        }
                        if (++p != pe) {
                            _goto_targ = 1;
                            continue _goto;
                        }
                    case 4:
                    case 5:
                }
                break;
            }
        }

        // line 190 "XmlReader.rl"

        if (p < pe) {
            int lineNumber = 1;
            for (int i = 0; i < p; i++)
                if (data[i] == '\n') lineNumber++;
            throw new SerializationException("Error parsing XML on line " + lineNumber + " near: "
                    + new String(data, p, Math.min(32, pe - p)));
        } else if (elements.size != 0) {
            Element element = elements.peek();
            elements.clear();
            throw new SerializationException("Error parsing XML, unclosed element: " + element.getName());
        }
        Element root = this.root;
        this.root = null;
        return root;
    }

    // line 324 "XmlReader.java"
    private static byte[] init__xml_actions_0() {
        return new byte[]{0, 1, 0, 1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 7, 2, 0, 6, 2, 1, 4, 2, 2, 4};
    }

    private static final byte _xml_actions[] = init__xml_actions_0();

    private static byte[] init__xml_key_offsets_0() {
        return new byte[]{0, 0, 4, 9, 14, 20, 26, 30, 35, 36, 37, 42, 46, 50, 51, 52, 56, 57, 62, 67, 73, 79, 83, 88, 89, 90, 95,
                99, 103, 104, 108, 109, 110, 111, 112, 115};
    }

    private static final byte _xml_key_offsets[] = init__xml_key_offsets_0();

    private static char[] init__xml_trans_keys_0() {
        return new char[]{32, 60, 9, 13, 32, 47, 62, 9, 13, 32, 47, 62, 9, 13, 32, 47, 61, 62, 9, 13, 32, 47, 61, 62, 9, 13, 32,
                61, 9, 13, 32, 34, 39, 9, 13, 34, 34, 32, 47, 62, 9, 13, 32, 62, 9, 13, 32, 62, 9, 13, 39, 39, 32, 60, 9, 13, 60, 32,
                47, 62, 9, 13, 32, 47, 62, 9, 13, 32, 47, 61, 62, 9, 13, 32, 47, 61, 62, 9, 13, 32, 61, 9, 13, 32, 34, 39, 9, 13, 34,
                34, 32, 47, 62, 9, 13, 32, 62, 9, 13, 32, 62, 9, 13, 60, 32, 47, 9, 13, 62, 62, 39, 39, 32, 9, 13, 0};
    }

    private static final char _xml_trans_keys[] = init__xml_trans_keys_0();

    private static byte[] init__xml_single_lengths_0() {
        return new byte[]{0, 2, 3, 3, 4, 4, 2, 3, 1, 1, 3, 2, 2, 1, 1, 2, 1, 3, 3, 4, 4, 2, 3, 1, 1, 3, 2, 2, 1, 2, 1, 1, 1, 1, 1,
                0};
    }

    private static final byte _xml_single_lengths[] = init__xml_single_lengths_0();

    private static byte[] init__xml_range_lengths_0() {
        return new byte[]{0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1, 0, 0, 0, 0, 1,
                0};
    }

    private static final byte _xml_range_lengths[] = init__xml_range_lengths_0();

    private static short[] init__xml_index_offsets_0() {
        return new short[]{0, 0, 4, 9, 14, 20, 26, 30, 35, 37, 39, 44, 48, 52, 54, 56, 60, 62, 67, 72, 78, 84, 88, 93, 95, 97,
                102, 106, 110, 112, 116, 118, 120, 122, 124, 127};
    }

    private static final short _xml_index_offsets[] = init__xml_index_offsets_0();

    private static byte[] init__xml_indicies_0() {
        return new byte[]{0, 2, 0, 1, 2, 1, 1, 2, 3, 5, 6, 7, 5, 4, 9, 10, 1, 11, 9, 8, 13, 1, 14, 1, 13, 12, 15, 16, 15, 1, 16,
                17, 18, 16, 1, 20, 19, 22, 21, 9, 10, 11, 9, 1, 23, 24, 23, 1, 25, 11, 25, 1, 20, 26, 22, 27, 29, 30, 29, 28, 32, 31,
                30, 34, 1, 30, 33, 36, 37, 38, 36, 35, 40, 41, 1, 42, 40, 39, 44, 1, 45, 1, 44, 43, 46, 47, 46, 1, 47, 48, 49, 47, 1,
                51, 50, 53, 52, 40, 41, 42, 40, 1, 54, 55, 54, 1, 56, 42, 56, 1, 57, 1, 57, 34, 57, 1, 1, 58, 59, 58, 51, 60, 53, 61,
                62, 62, 1, 1, 0};
    }

    private static final byte _xml_indicies[] = init__xml_indicies_0();

    private static byte[] init__xml_trans_targs_0() {
        return new byte[]{1, 0, 2, 3, 3, 4, 11, 34, 5, 4, 11, 34, 5, 6, 7, 6, 7, 8, 13, 9, 10, 9, 10, 12, 34, 12, 14, 14, 16, 15,
                17, 16, 17, 18, 30, 18, 19, 26, 28, 20, 19, 26, 28, 20, 21, 22, 21, 22, 23, 32, 24, 25, 24, 25, 27, 28, 27, 29, 31, 35,
                33, 33, 34};
    }

    private static final byte _xml_trans_targs[] = init__xml_trans_targs_0();

    private static byte[] init__xml_trans_actions_0() {
        return new byte[]{0, 0, 0, 1, 0, 3, 3, 20, 1, 0, 0, 9, 0, 11, 11, 0, 0, 0, 0, 1, 17, 0, 13, 5, 23, 0, 1, 0, 1, 0, 0, 0,
                15, 1, 0, 0, 3, 3, 20, 1, 0, 0, 9, 0, 11, 11, 0, 0, 0, 0, 1, 17, 0, 13, 5, 23, 0, 0, 0, 7, 1, 0, 0};
    }

    private static final byte _xml_trans_actions[] = init__xml_trans_actions_0();

    static final int xml_start = 1;
    static final int xml_first_final = 34;
    static final int xml_error = 0;

    static final int xml_en_elementBody = 15;
    static final int xml_en_main = 1;

    // line 209 "XmlReader.rl"

    protected void open(String name) {
        Element child = new Element(name, current);
        Element parent = current;
        if (parent != null) parent.addChild(child);
        elements.add(child);
        current = child;
    }

    protected void attribute(String name, String value) {
        current.setAttribute(name, value);
    }

    protected String entity(String name) {
        if (name.equals("lt")) return "<";
        if (name.equals("gt")) return ">";
        if (name.equals("amp")) return "&";
        if (name.equals("apos")) return "'";
        if (name.equals("quot")) return "\"";
        if (name.startsWith("#x")) return Character.toString((char) Integer.parseInt(name.substring(2), 16));
        return null;
    }

    protected void text(String text) {
        /*
        String existing = current.getText();
        if (existing == null) {
            current.setText(existing);
        } else {
            current.setText(existing != null ? existing + text : text);
        }
        */
        Element child = new Element("text", current);
        child.setText(text);
        Element parent = current;
        if (parent != null) parent.addChild(child);

    }

    protected void close() {
        root = elements.pop();
        current = elements.size > 0 ? elements.peek() : null;
    }
}