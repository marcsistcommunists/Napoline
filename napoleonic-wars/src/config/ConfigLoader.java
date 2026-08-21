package config;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Загрузчик конфигурационных файлов.
 */
public class ConfigLoader {
    
    private final String configDir;
    private final Map<String, CountryConfig> countries = new LinkedHashMap<>();
    private final Map<String, RegimentTypeConfig> regimentTypes = new LinkedHashMap<>();
    
    public ConfigLoader(String configDir) {
        this.configDir = configDir;
    }
    
    public void loadAll() throws IOException {
        loadCountries();
        loadRegimentTypes();
    }
    
    private void loadCountries() throws IOException {
        Path path = Paths.get(configDir, "countries.json");
        String content = new String(Files.readAllBytes(path));
        
        List<Object> array = JsonParser.parseArray(content);
        for (Object obj : array) {
            if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                CountryConfig config = CountryConfig.fromMap((Map<String, Object>) obj);
                countries.put(config.id, config);
            }
        }
    }
    
    private void loadRegimentTypes() throws IOException {
        Path path = Paths.get(configDir, "regiments_type.json");
        String content = new String(Files.readAllBytes(path));
        
        List<Object> array = JsonParser.parseArray(content);
        for (Object obj : array) {
            if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                RegimentTypeConfig config = RegimentTypeConfig.fromMap((Map<String, Object>) obj);
                regimentTypes.put(config.id, config);
            }
        }
    }
    
    public CountryConfig getCountry(String id) {
        return countries.get(id);
    }
    
    public Collection<CountryConfig> getCountries() {
        return countries.values();
    }
    
    public RegimentTypeConfig getRegimentType(String id) {
        return regimentTypes.get(id);
    }
    
    public Collection<RegimentTypeConfig> getRegimentTypes() {
        return regimentTypes.values();
    }
}
