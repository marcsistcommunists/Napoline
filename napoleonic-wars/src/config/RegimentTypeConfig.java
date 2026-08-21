package config;

import java.util.*;

/**
 * Класс типа полка с параметрами из конфигурации.
 */
public class RegimentTypeConfig {
    public String id;
    public String name;
    public int health;
    public int damage;
    public int range;
    public int speed;
    public int cost;
    public List<String> slots = new ArrayList<>();
    public String imageDirectory;
    
    @SuppressWarnings("unchecked")
    public static RegimentTypeConfig fromMap(Map<String, Object> map) {
        RegimentTypeConfig config = new RegimentTypeConfig();
        config.id = (String) map.get("id");
        config.name = (String) map.get("name");
        config.health = ((Number) map.get("health")).intValue();
        config.damage = ((Number) map.get("damage")).intValue();
        config.range = ((Number) map.get("range")).intValue();
        config.speed = ((Number) map.get("speed")).intValue();
        config.cost = ((Number) map.get("cost")).intValue();
        config.imageDirectory = (String) map.get("imageDirectory");
        
        if (map.get("slots") instanceof List) {
            for (Object s : (List<?>) map.get("slots")) {
                config.slots.add((String) s);
            }
        }
        
        return config;
    }
}
