package map;

import java.util.*;

/**
 * Регион (провинция) - группа смежных гексов, принадлежащих одной стране.
 */
public class Region {
    private final String id;
    private final String name;
    private final List<Hex> hexes = new ArrayList<>();
    private String ownerId;
    private Hex capital;
    
    public Region(String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void addHex(Hex hex) {
        if (!hexes.contains(hex)) {
            hexes.add(hex);
            hex.region = this;
        }
    }
    
    public List<Hex> getHexes() {
        return new ArrayList<>(hexes);
    }
    
    public String getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    
    public Hex getCapital() {
        return capital;
    }
    
    public void setCapital(Hex capital) {
        this.capital = capital;
    }
    
    /**
     * Доход региона (налоги).
     */
    public int getIncome() {
        int baseIncome = 10;
        if (capital != null && capital.city != null) {
            baseIncome += capital.city.getPopulation() / 1000;
        }
        return baseIncome;
    }
    
    @Override
    public String toString() {
        return String.format("Region(%s, %s) [%s]", id, name, ownerId);
    }
}
