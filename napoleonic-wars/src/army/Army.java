package army;

import map.Hex;
import java.util.*;

/**
 * Армия - контейнер для полков (максимум 20).
 */
public class Army {
    private final String id;
    private final String name;
    private final String countryId;
    
    private Hex position;
    private int movementPoints;
    private final List<Regiment> regiments = new ArrayList<>();
    
    public Army(String id, String name, String countryId) {
        this.id = id;
        this.name = name;
        this.countryId = countryId;
        this.movementPoints = 10; // Базовые очки движения
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getCountryId() {
        return countryId;
    }
    
    public Hex getPosition() {
        return position;
    }
    
    public void setPosition(Hex hex) {
        if (this.position != null) {
            this.position.removeArmy(this);
        }
        this.position = hex;
        if (hex != null) {
            hex.addArmy(this);
        }
    }
    
    public int getMovementPoints() {
        return movementPoints;
    }
    
    public void setMovementPoints(int points) {
        this.movementPoints = points;
    }
    
    public void addMovementPoints(int points) {
        this.movementPoints += points;
    }
    
    public boolean canMove() {
        return movementPoints > 0;
    }
    
    public void spendMovementPoints(int points) {
        this.movementPoints -= points;
    }
    
    /**
     * Вычисление скорости армии на основе состава полков.
     * Артиллерия замедляет, кавалерия ускоряет.
     */
    public int calculateSpeed() {
        if (regiments.isEmpty()) return 0;
        
        int totalSpeed = 0;
        int cavalryCount = 0;
        int artilleryCount = 0;
        
        for (Regiment r : regiments) {
            totalSpeed += r.getSpeed();
            
            String type = r.getTypeId();
            if (type.equals("cuirassiers") || type.equals("dragoons") || type.equals("uhlans")) {
                cavalryCount++;
            } else if (type.equals("artillery")) {
                artilleryCount++;
            }
        }
        
        // Средневзвешенная скорость
        int avgSpeed = totalSpeed / regiments.size();
        
        // Бонус от кавалерии (+0.5 за каждый полк)
        float cavalryBonus = cavalryCount * 0.5f;
        
        // Штраф от артиллерии (-1 за каждый полк)
        int artilleryPenalty = artilleryCount * 1;
        
        return Math.max(1, (int)(avgSpeed + cavalryBonus - artilleryPenalty));
    }
    
    /**
     * Получить стоимость перемещения на один гекс с учётом местности.
     */
    public int getMovementCost(map.TerrainType terrain) {
        int baseCost = 1;
        float modifier = terrain.getMovementModifier();
        return (int) Math.ceil(baseCost / modifier);
    }
    
    public void addRegiment(Regiment regiment) {
        if (regiments.size() >= 20) {
            throw new IllegalStateException("Армия достигла максимума полков (20)");
        }
        regiments.add(regiment);
    }
    
    public void removeRegiment(Regiment regiment) {
        regiments.remove(regiment);
    }
    
    public List<Regiment> getRegiments() {
        return new ArrayList<>(regiments);
    }
    
    public int getRegimentCount() {
        return regiments.size();
    }
    
    public Regiment getRegiment(int index) {
        if (index >= 0 && index < regiments.size()) {
            return regiments.get(index);
        }
        return null;
    }
    
    /**
     * Проверка, есть ли живые полки.
     */
    public boolean hasLivingRegiments() {
        for (Regiment r : regiments) {
            if (!r.isDefeated()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Удалить все уничтоженные полки.
     */
    public void removeDefeatedRegiments() {
        regiments.removeIf(Regiment::isDefeated);
    }
    
    /**
     * Получить суммарную мощь армии.
     */
    public int getTotalPower() {
        int power = 0;
        for (Regiment r : regiments) {
            if (!r.isDefeated()) {
                power += r.getDamage() * (r.getHealth() / (float) r.getMaxHealth());
            }
        }
        return power;
    }
    
    @Override
    public String toString() {
        return String.format("Army(%s, %s) at (%d,%d) [%d regiments]", 
            id, name, position != null ? position.q : 0, position != null ? position.r : 0, regiments.size());
    }
}
