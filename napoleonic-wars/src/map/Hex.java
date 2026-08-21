package map;

import army.Army;
import java.util.*;

/**
 * Гекс - базовая клетка глобальной карты.
 */
public class Hex {
    // Осевые координаты (q, r)
    public final int q;
    public final int r;
    
    // Тип местности
    public TerrainType terrain;
    
    // Регион (может быть null)
    public Region region;
    
    // Город на гексе (может быть null)
    public City city;
    
    // Армии на гексе
    private final List<Army> armies = new ArrayList<>();
    
    public Hex(int q, int r) {
        this.q = q;
        this.r = r;
        this.terrain = TerrainType.PLAIN;
    }
    
    public Hex(int q, int r, TerrainType terrain) {
        this.q = q;
        this.r = r;
        this.terrain = terrain;
    }
    
    /**
     * Получить кубические координаты из осевых.
     */
    public int getX() { return q; }
    public int getY() { return -q - r; }
    public int getZ() { return r; }
    
    /**
     * Расстояние до другого гекса в гексах.
     */
    public int distanceTo(Hex other) {
        return (Math.abs(getX() - other.getX()) + 
                Math.abs(getY() - other.getY()) + 
                Math.abs(getZ() - other.getZ())) / 2;
    }
    
    /**
     * Получить соседние гексы.
     */
    public List<Hex> getNeighbors() {
        List<Hex> neighbors = new ArrayList<>(6);
        int[][] directions = {
            {1, 0}, {1, -1}, {0, -1},
            {-1, 0}, {-1, 1}, {0, 1}
        };
        for (int[] d : directions) {
            neighbors.add(new Hex(q + d[0], r + d[1]));
        }
        return neighbors;
    }
    
    public void addArmy(Army army) {
        if (!armies.contains(army)) {
            armies.add(army);
        }
    }
    
    public void removeArmy(Army army) {
        armies.remove(army);
    }
    
    public List<Army> getArmies() {
        return new ArrayList<>(armies);
    }
    
    public boolean hasEnemyArmy(String myCountryId) {
        for (Army army : armies) {
            if (!army.getCountryId().equals(myCountryId)) {
                return true;
            }
        }
        return false;
    }
    
    public Army getEnemyArmy(String myCountryId) {
        for (Army army : armies) {
            if (!army.getCountryId().equals(myCountryId)) {
                return army;
            }
        }
        return null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hex)) return false;
        Hex hex = (Hex) o;
        return q == hex.q && r == hex.r;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(q, r);
    }
    
    @Override
    public String toString() {
        return String.format("Hex(%d,%d) [%s]", q, r, terrain);
    }
}
