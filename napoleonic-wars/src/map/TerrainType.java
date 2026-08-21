package map;

/**
 * Типы местности, влияющие на движение и бой.
 */
public enum TerrainType {
    PLAIN("Равнина", 1.0f, 0),
    FOREST("Лес", 0.5f, -1),
    MOUNTAIN("Горы", 0.3f, -2),
    HILL("Холмы", 0.7f, 0),
    RIVER("Река", 0.4f, -1),
    CITY("Город", 1.0f, 1),
    ROAD("Дорога", 1.5f, 0),
    SWAMP("Болото", 0.3f, -1);
    
    private final String name;
    private final float movementModifier;
    private final int defenseBonus;
    
    TerrainType(String name, float movementModifier, int defenseBonus) {
        this.name = name;
        this.movementModifier = movementModifier;
        this.defenseBonus = defenseBonus;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * Модификатор движения (1.0 = нормально, < 1.0 = медленнее).
     */
    public float getMovementModifier() {
        return movementModifier;
    }
    
    /**
     * Бонус к защите при бою на этой местности.
     */
    public int getDefenseBonus() {
        return defenseBonus;
    }
}
