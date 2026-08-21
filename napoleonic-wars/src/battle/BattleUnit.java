package battle;

import army.Regiment;
import map.Hex;

/**
 * Боевая единица на тактической карте боя.
 */
public class BattleUnit {
    private final Regiment regiment;
    private Hex position;
    private boolean hasActed = false;
    private boolean isReloading = false;
    
    public BattleUnit(Regiment regiment, Hex position) {
        this.regiment = regiment;
        this.position = position;
    }
    
    public Regiment getRegiment() {
        return regiment;
    }
    
    public Hex getPosition() {
        return position;
    }
    
    public void setPosition(Hex position) {
        this.position = position;
    }
    
    public boolean hasActed() {
        return hasActed;
    }
    
    public void setHasActed(boolean hasActed) {
        this.hasActed = hasActed;
    }
    
    public boolean isReloading() {
        return isReloading;
    }
    
    public void setReloading(boolean reloading) {
        isReloading = reloading;
    }
    
    public int getMovementRange() {
        return regiment.getSpeed();
    }
    
    public int getAttackRange() {
        return regiment.getRange();
    }
    
    public int getDamage() {
        return regiment.getDamage();
    }
    
    public boolean canAttack() {
        return !regiment.isDefeated() && !isReloading;
    }
    
    @Override
    public String toString() {
        return String.format("BattleUnit(%s) at (%d,%d)", 
            regiment.getTypeId(), position.getQ(), position.getR());
    }
}
