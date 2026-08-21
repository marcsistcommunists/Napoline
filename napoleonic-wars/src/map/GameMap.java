package map;

import java.util.*;

/**
 * Глобальная карта игры.
 */
public class GameMap {
    private final Map<String, Hex> hexes = new HashMap<>();
    private final Map<String, Region> regions = new HashMap<>();
    private final int radius;
    
    public GameMap(int radius) {
        this.radius = radius;
        generateBaseMap();
    }
    
    /**
     * Генерация базовой карты заданного радиуса.
     */
    private void generateBaseMap() {
        Random rand = new Random(42); // Фиксированный seed для воспроизводимости
        
        // Используем col, row вместо q, r для конструктора Hex
        // Генерируем прямоугольную сетку с гексагональным смещением
        int mapWidth = radius * 2 + 1;
        int mapHeight = radius * 2 + 1;
        
        for (int row = 0; row < mapHeight; row++) {
            for (int col = 0; col < mapWidth; col++) {
                // Конвертируем col, row в axial q, r для проверки границ круга
                int q = col - (row - (row & 1)) / 2;
                int r = row;
                
                // Проверяем, находится ли гекс в пределах радиуса (в axial координатах)
                if (Math.abs(q) + Math.abs(r) + Math.abs(-q - r) <= radius * 3) {
                    Hex hex = new Hex(col, row, randomTerrain(rand));
                    String key = getHexKey(col, row);
                    hexes.put(key, hex);
                }
            }
        }
    }
    
    private TerrainType randomTerrain(Random rand) {
        int roll = rand.nextInt(100);
        if (roll < 50) return TerrainType.PLAIN;
        if (roll < 65) return TerrainType.FOREST;
        if (roll < 75) return TerrainType.HILL;
        if (roll < 85) return TerrainType.MOUNTAIN;
        if (roll < 92) return TerrainType.RIVER;
        if (roll < 97) return TerrainType.SWAMP;
        return TerrainType.ROAD;
    }
    
    public static String getHexKey(int col, int row) {
        return col + "," + row;
    }
    
    public Hex getHex(int col, int row) {
        return hexes.get(getHexKey(col, row));
    }
    
    public Hex getHex(String key) {
        return hexes.get(key);
    }
    
    public Collection<Hex> getAllHexes() {
        return hexes.values();
    }
    
    public List<Hex> getNeighbors(Hex hex) {
        List<Hex> neighbors = new ArrayList<>();
        // Соседи для odd-r горизонтальной раскладки
        int[][] directions;
        if ((hex.getRow() & 1) == 0) {
            // Четный ряд
            directions = new int[][]{{-1, 0}, {0, -1}, {1, -1}, {1, 0}, {0, 1}, {-1, 1}};
        } else {
            // Нечетный ряд
            directions = new int[][]{{-1, 0}, {-1, -1}, {0, -1}, {1, 0}, {0, 1}, {-1, 1}};
        }
        
        for (int[] dir : directions) {
            Hex neighbor = getHex(hex.getCol() + dir[0], hex.getRow() + dir[1]);
            if (neighbor != null) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }
    
    /**
     * Поиск пути между двумя гексами (простой BFS).
     */
    public List<Hex> findPath(Hex start, Hex end, String countryId) {
        if (start == null || end == null) return Collections.emptyList();
        
        Map<Hex, Hex> cameFrom = new HashMap<>();
        Set<Hex> visited = new HashSet<>();
        Queue<Hex> queue = new LinkedList<>();
        
        queue.offer(start);
        visited.add(start);
        
        while (!queue.isEmpty()) {
            Hex current = queue.poll();
            
            if (current.equals(end)) {
                return reconstructPath(cameFrom, current);
            }
            
            for (Hex neighbor : getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    // Проверка на вражеские армии
                    if (!neighbor.hasEnemyArmy(countryId) || neighbor.equals(end)) {
                        visited.add(neighbor);
                        cameFrom.put(neighbor, current);
                        queue.offer(neighbor);
                    }
                }
            }
        }
        
        return Collections.emptyList(); // Путь не найден
    }
    
    private List<Hex> reconstructPath(Map<Hex, Hex> cameFrom, Hex current) {
        List<Hex> path = new ArrayList<>();
        path.add(current);
        
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(current);
        }
        
        Collections.reverse(path);
        return path;
    }
    
    public void addRegion(Region region) {
        regions.put(region.getId(), region);
    }
    
    public Region getRegion(String id) {
        return regions.get(id);
    }
    
    public Collection<Region> getRegions() {
        return regions.values();
    }
    
    public int getRadius() {
        return radius;
    }
    
    /**
     * Получить все гексы в пределах диапазона от центрального.
     */
    public List<Hex> getHexesInRange(Hex center, int range) {
        List<Hex> result = new ArrayList<>();
        for (Hex hex : hexes.values()) {
            if (hex.distanceTo(center) <= range) {
                result.add(hex);
            }
        }
        return result;
    }
}
