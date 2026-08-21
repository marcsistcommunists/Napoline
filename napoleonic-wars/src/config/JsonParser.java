package config;

import java.io.*;
import java.util.*;

/**
 * Простой JSON парсер для конфигурационных файлов.
 * Поддерживает базовые типы: строки, числа, булевы значения, массивы и объекты.
 */
public class JsonParser {
    
    private String json;
    private int pos = 0;
    
    public static Object parse(String jsonString) {
        JsonParser parser = new JsonParser();
        parser.json = jsonString;
        return parser.parseValue();
    }
    
    public static Map<String, Object> parseObject(String jsonString) {
        return (Map<String, Object>) parse(jsonString);
    }
    
    public static List<Object> parseArray(String jsonString) {
        return (List<Object>) parse(jsonString);
    }
    
    private Object parseValue() {
        skipWhitespace();
        if (pos >= json.length()) return null;
        
        char c = json.charAt(pos);
        switch (c) {
            case '{': return parseObjectValue();
            case '[': return parseArrayValue();
            case '"': return parseString();
            case 't': case 'f': return parseBoolean();
            case 'n': return parseNull();
            default: return parseNumber();
        }
    }
    
    private Map<String, Object> parseObjectValue() {
        Map<String, Object> map = new LinkedHashMap<>();
        pos++; // skip '{'
        skipWhitespace();
        
        if (json.charAt(pos) == '}') {
            pos++;
            return map;
        }
        
        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            pos++; // skip ':'
            Object value = parseValue();
            map.put(key, value);
            
            skipWhitespace();
            if (json.charAt(pos) == '}') {
                pos++;
                return map;
            }
            pos++; // skip ','
        }
    }
    
    private List<Object> parseArrayValue() {
        List<Object> list = new ArrayList<>();
        pos++; // skip '['
        skipWhitespace();
        
        if (json.charAt(pos) == ']') {
            pos++;
            return list;
        }
        
        while (true) {
            list.add(parseValue());
            skipWhitespace();
            if (json.charAt(pos) == ']') {
                pos++;
                return list;
            }
            pos++; // skip ','
        }
    }
    
    private String parseString() {
        pos++; // skip opening '"'
        StringBuilder sb = new StringBuilder();
        
        while (pos < json.length() && json.charAt(pos) != '"') {
            char c = json.charAt(pos);
            if (c == '\\' && pos + 1 < json.length()) {
                pos++;
                char escaped = json.charAt(pos);
                switch (escaped) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    default: sb.append(escaped);
                }
            } else {
                sb.append(c);
            }
            pos++;
        }
        pos++; // skip closing '"'
        return sb.toString();
    }
    
    private Number parseNumber() {
        int start = pos;
        if (json.charAt(pos) == '-') pos++;
        
        while (pos < json.length() && Character.isDigit(json.charAt(pos))) pos++;
        
        boolean isDouble = false;
        if (pos < json.length() && json.charAt(pos) == '.') {
            isDouble = true;
            pos++;
            while (pos < json.length() && Character.isDigit(json.charAt(pos))) pos++;
        }
        
        if (pos < json.length() && (json.charAt(pos) == 'e' || json.charAt(pos) == 'E')) {
            isDouble = true;
            pos++;
            if (pos < json.length() && (json.charAt(pos) == '+' || json.charAt(pos) == '-')) pos++;
            while (pos < json.length() && Character.isDigit(json.charAt(pos))) pos++;
        }
        
        String numStr = json.substring(start, pos);
        return isDouble ? Double.parseDouble(numStr) : Long.parseLong(numStr);
    }
    
    private boolean parseBoolean() {
        if (json.startsWith("true", pos)) {
            pos += 4;
            return true;
        } else if (json.startsWith("false", pos)) {
            pos += 5;
            return false;
        }
        throw new RuntimeException("Invalid boolean at position " + pos);
    }
    
    private Void parseNull() {
        if (json.startsWith("null", pos)) {
            pos += 4;
            return null;
        }
        throw new RuntimeException("Invalid null at position " + pos);
    }
    
    private void skipWhitespace() {
        while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
            pos++;
        }
    }
}
