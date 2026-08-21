package battle;

/**
 * Результат атаки в бою.
 */
public class BattleResult {
    private final BattleUnit attacker;
    private final BattleUnit defender;
    private final int damage;
    private final boolean isDefeated;
    
    public BattleResult(BattleUnit attacker, BattleUnit defender, int damage, boolean isDefeated) {
        this.attacker = attacker;
        this.defender = defender;
        this.damage = damage;
        this.isDefeated = isDefeated;
    }
    
    public BattleUnit getAttacker() {
        return attacker;
    }
    
    public BattleUnit getDefender() {
        return defender;
    }
    
    public int getDamage() {
        return damage;
    }
    
    public boolean isDefeated() {
        return isDefeated;
    }
    
    @Override
    public String toString() {
        return String.format("%s hit %s for %d damage (%s)",
            attacker.getRegiment().getTypeId(),
            defender.getRegiment().getTypeId(),
            damage,
            isDefeated ? "KILLED" : "alive");
    }
}
