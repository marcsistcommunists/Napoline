package game;

import config.CountryConfig;
import map.Region;
import java.util.*;

/**
 * Состояние страны в игре.
 */
public class CountryState {
    public final CountryConfig config;
    public int gold = 1000;
    public int recruits = 500;
    public final List<Region> regions = new ArrayList<>();
    
    public CountryState(CountryConfig config) {
        this.config = config;
    }
    
    public void addRegion(Region region) {
        if (!regions.contains(region)) {
            regions.add(region);
        }
    }
    
    public void removeRegion(Region region) {
        regions.remove(region);
    }
    
    public int getTotalIncome() {
        int income = 0;
        for (Region region : regions) {
            income += region.getIncome();
        }
        return income;
    }
}
