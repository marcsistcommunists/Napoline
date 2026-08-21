package map;

import army.Army;
import java.util.*;

/**
 * Гекс - базовая клетка глобальной карты.
 * Использует flat-top ориентацию (плоская вершина сверху) со смещением по столбцам (odd-q).
 * Это даёт классическую "вертикальную" сетку как в Civilization с явными столбцами.
 */
public class Hex {
    // Offset координаты (для хранения в карте и отрисовки)
    private final int col; // q - столбец
    private final int row; // r - ряд
    
    // Axial координаты (вычисляются из offset)
    private final int q;
    private final int r;
    
    // Тип местности
    public TerrainType terrain;
    
    // Регион (может быть null)
    public Region region;
    
    // Город на гексе (может быть null)
    public City city;
    
    // Армии на гексе
    private final java.util.List<Army> armies = new java.util.ArrayList<>();
    
    // Кэш полигона для отрисовки
    private java.awt.Polygon cachedPolygon;
    private java.awt.Point cachedCenter;
    private int lastHexSize = -1;
    private int lastXOffset = -1;
    private int lastYOffset = -1;
    
    public Hex(int col, int row) {
        this.col = col;
        this.row = row;
        // Конвертация из offset (odd-q) в axial для flat-top
        this.q = col;
        this.r = row - (col - (col & 1)) / 2;
        this.terrain = TerrainType.PLAIN;
    }
    
    public Hex(int col, int row, TerrainType terrain) {
        this.col = col;
        this.row = row;
        // Конвертация из offset (odd-q) в axial для flat-top
        this.q = col;
        this.r = row - (col - (col & 1)) / 2;
        this.terrain = terrain;
    }
    
    // Геттеры для offset координат
    public int getCol() { return col; }
    public int getRow() { return row; }
    
    // Геттеры для axial координат
    public int getQ() { return q; }
    public int getR() { return r; }
    
    /**
     * Получить центр гекса в пикселях.
     * Для flat-top: ширина = 2*size, высота = sqrt(3)*size
     * Нечетные столбцы сдвинуты вниз на половину высоты
     */
    public java.awt.Point getCenter(int hexSize, int xOffset, int yOffset) {
        if (cachedCenter != null && lastHexSize == hexSize && 
            lastXOffset == xOffset && lastYOffset == yOffset) {
            return cachedCenter;
        }
        
        // Размеры для flat-top гекса
        double width = hexSize * 2;              // Ширина гекса
        double height = hexSize * Math.sqrt(3);  // Расстояние по вертикали между центрами
        
        // X позиция: номер столбца * 3/4 ширины (перекрытие)
        double x = xOffset + width * 0.75 * col + width / 2;
        
        // Y позиция: просто номер ряда * высоту
        double y = yOffset + height * row + height / 2;
        
        // Сдвиг нечетных столбцов вниз на половину высоты
        if ((col & 1) == 1) {
            y += height / 2;
        }
        
        cachedCenter = new java.awt.Point((int)Math.round(x), (int)Math.round(y));
        lastHexSize = hexSize;
        lastXOffset = xOffset;
        lastYOffset = yOffset;
        
        return cachedCenter;
    }
    
    /**
     * Получить полигон гекса для отрисовки.
     * Flat-top: плоская вершина сверху, углы: 0°, 60°, 120°, 180°, 240°, 300°
     */
    public java.awt.Polygon getPolygon(int hexSize, int xOffset, int yOffset) {
        if (cachedPolygon != null && lastHexSize == hexSize && 
            lastXOffset == xOffset && lastYOffset == yOffset) {
            return cachedPolygon;
        }
        
        java.awt.Point center = getCenter(hexSize, xOffset, yOffset);
        int[] xPoints = new int[6];
        int[] yPoints = new int[6];
        
        // Углы для flat-top гекса: начинаем с правой вершины (0°) и идём против часовой
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(30 + 60 * i);
            xPoints[i] = (int)Math.round(center.x + hexSize * Math.cos(angle));
            yPoints[i] = (int)Math.round(center.y + hexSize * Math.sin(angle));
        }
        
        cachedPolygon = new java.awt.Polygon(xPoints, yPoints, 6);
        lastHexSize = hexSize;
        lastXOffset = xOffset;
        lastYOffset = yOffset;
        
        return cachedPolygon;
    }
    
    /**
     * Проверка попадания точки в гекс.
     */
    public boolean contains(int mouseX, int mouseY, int hexSize, int xOffset, int yOffset) {
        return getPolygon(hexSize, xOffset, yOffset).contains(mouseX, mouseY);
    }
    
    /**
     * Расстояние до другого гекса в гексах (используя axial координаты).
     */
    public int distanceTo(Hex other) {
        return (Math.abs(q - other.q) + 
                Math.abs(r - other.r) + 
                Math.abs((-q-r) - (-other.q-other.r))) / 2;
    }
    
    /**
     * Получить соседние гексы в offset координатах.
     * Для odd-q раскладки с flat-top ориентацией направления зависят от четности столбца.
     */
    public java.util.List<Hex> getNeighbors() {
        java.util.List<Hex> neighbors = new java.util.ArrayList<>(6);
        // Направления для odd-q раскладки (flat-top, смещение по столбцам)
        int[][] directions;
        if ((col & 1) == 0) {
            // Четный столбец
            directions = new int[][]{{-1, 0}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}};
        } else {
            // Нечетный столбец
            directions = new int[][]{{-1, -1}, {-1, 0}, {0, -1}, {1, 0}, {0, 1}, {-1, 1}};
        }
        
        for (int[] d : directions) {
            neighbors.add(new Hex(col + d[0], row + d[1]));
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
    
    public java.util.List<Army> getArmies() {
        return new java.util.ArrayList<>(armies);
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
        return col == hex.col && row == hex.row;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(col, row);
    }
    
    @Override
    public String toString() {
        return String.format("Hex[%d,%d](%d,%d) [%s]", col, row, q, r, terrain);
    }
}
