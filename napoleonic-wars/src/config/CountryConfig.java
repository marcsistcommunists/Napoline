package config;

import java.util.*;

/**
 * Класс страны с параметрами из конфигурации.
 */
public class CountryConfig {
    public String id;
    public String name;
    public String leader;
    public String flagPath;
    public String color;
    public List<String> initialRegions = new ArrayList<>();
    public List<String> availableUnitTypes = new ArrayList<>();
    
    @SuppressWarnings("unchecked")
    public static CountryConfig fromMap(Map<String, Object> map) {
        CountryConfig config = new CountryConfig();
        config.id = (String) map.get("id");
        config.name = (String) map.get("name");
        config.leader = (String) map.get("leader");
        config.flagPath = (String) map.get("flagPath");
        config.color = (String) map.get("color");
        
        if (map.get("initialRegions") instanceof List) {
            for (Object r : (List<?>) map.get("initialRegions")) {
                config.initialRegions.add((String) r);
            }
        }
        
        if (map.get("availableUnitTypes") instanceof List) {
            for (Object t : (List<?>) map.get("availableUnitTypes")) {
                config.availableUnitTypes.add((String) t);
            }
        }
        
        return config;
    }
}
