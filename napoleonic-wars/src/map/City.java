package map;

import java.util.*;

/**
 * Город на гексе.
 */
public class City {
    private final String name;
    private int population;
    private int gold;
    private final List<String> availableRegimentTypes = new ArrayList<>();
    
    public City(String name) {
        this.name = name;
        this.population = 10000;
        this.gold = 500;
    }
    
    public String getName() {
        return name;
    }
    
    public int getPopulation() {
        return population;
    }
    
    public void setPopulation(int population) {
        this.population = Math.max(0, population);
    }
    
    public int getGold() {
        return gold;
    }
    
    public void addGold(int amount) {
        this.gold += amount;
    }
    
    public void spendGold(int amount) throws IllegalStateException {
        if (gold < amount) {
            throw new IllegalStateException("Недостаточно золота: " + gold + " < " + amount);
        }
        this.gold -= amount;
    }
    
    public void addAvailableRegimentType(String typeId) {
        if (!availableRegimentTypes.contains(typeId)) {
            availableRegimentTypes.add(typeId);
        }
    }
    
    public List<String> getAvailableRegimentTypes() {
        return new ArrayList<>(availableRegimentTypes);
    }
    
    @Override
    public String toString() {
        return String.format("City(%s, pop=%d, gold=%d)", name, population, gold);
    }
}
