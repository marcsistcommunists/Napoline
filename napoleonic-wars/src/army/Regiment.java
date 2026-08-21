package army;

import config.RegimentTypeConfig;

/**
 * Полк - базовая боевая единица.
 */
public class Regiment {
    private final String id;
    private final String typeId;
    private final String countryId;
    
    // Характеристики
    private int health;
    private final int maxHealth;
    private int damage;
    private int range;
    private int speed;
    private int experience;
    private int morale = 100;
    
    // Снаряжение
    private String weapon;
    private String flag;
    private String bonus;
    private String horse;
    private String cannon;
    
    // Изображение
    private final int spriteIndex;
    private boolean isDefeated = false;
    
    public Regiment(String id, String typeId, String countryId, RegimentTypeConfig typeConfig) {
        this.id = id;
        this.typeId = typeId;
        this.countryId = countryId;
        this.maxHealth = typeConfig.health;
        this.health = maxHealth;
        this.damage = typeConfig.damage;
        this.range = typeConfig.range;
        this.speed = typeConfig.speed;
        this.experience = 0;
        this.spriteIndex = (int)(Math.random() * 5); // Случайный спрайт из пула
    }
    
    public String getId() {
        return id;
    }
    
    public String getTypeId() {
        return typeId;
    }
    
    public String getCountryId() {
        return countryId;
    }
    
    public int getHealth() {
        return health;
    }
    
    public int getMaxHealth() {
        return maxHealth;
    }
    
    public float getHealthPercent() {
        return (float) health / maxHealth;
    }
    
    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(maxHealth, health));
        if (this.health == 0) {
            this.isDefeated = true;
        }
    }
    
    public void heal(int amount) {
        this.health = Math.min(maxHealth, this.health + amount);
    }
    
    public int getDamage() {
        // Бонус от опыта: +10% за каждый уровень
        return (int) (damage * (1 + experience * 0.1));
    }
    
    public int getRange() {
        return range;
    }
    
    public int getSpeed() {
        return speed;
    }
    
    public int getExperience() {
        return experience;
    }
    
    public void addExperience(int exp) {
        this.experience += exp;
    }
    
    public int getMorale() {
        return morale;
    }
    
    public void setMorale(int morale) {
        this.morale = Math.max(0, Math.min(100, morale));
    }
    
    public boolean isDefeated() {
        return isDefeated;
    }
    
    public int getSpriteIndex() {
        return spriteIndex;
    }
    
    // Снаряжение
    public String getWeapon() { return weapon; }
    public void setWeapon(String weapon) { this.weapon = weapon; }
    
    public String getFlag() { return flag; }
    public void setFlag(String flag) { this.flag = flag; }
    
    public String getBonus() { return bonus; }
    public void setBonus(String bonus) { this.bonus = bonus; }
    
    public String getHorse() { return horse; }
    public void setHorse(String horse) { this.horse = horse; }
    
    public String getCannon() { return cannon; }
    public void setCannon(String cannon) { this.cannon = cannon; }
    
    @Override
    public String toString() {
        return String.format("Regiment(%s, %s) HP=%d/%d EXP=%d", 
            id, typeId, health, maxHealth, experience);
    }
}
