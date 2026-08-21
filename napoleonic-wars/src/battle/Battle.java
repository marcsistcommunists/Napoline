package battle;

import army.Army;
import army.Regiment;
import map.*;
import java.util.*;

/**
 * Тактическое сражение между двумя армиями.
 */
public class Battle {
    private final Army attacker;
    private final Army defender;
    private final Hex globalHex;
    
    private final GameMap battleMap;
    private final List<BattleUnit> attackerUnits = new ArrayList<>();
    private final List<BattleUnit> defenderUnits = new ArrayList<>();
    
    private int currentRound = 1;
    private boolean isAttackerTurn = true;
    private boolean isFinished = false;
    private Army winner = null;
    
    public Battle(Army attacker, Army defender, Hex globalHex) {
        this.attacker = attacker;
        this.defender = defender;
        this.globalHex = globalHex;
        
        // Создаём карту боя (15x15 гексов)
        this.battleMap = new GameMap(7);
        
        initializeBattle();
    }
    
    private void initializeBattle() {
        Random rand = new Random();
        
        // Размещаем полки атакующего на одной стороне карты
        int deployRowAttacker = -6;
        int colStep = 3;
        for (Regiment reg : attacker.getRegiments()) {
            if (!reg.isDefeated()) {
                int q = (rand.nextInt(5) - 2) * colStep;
                Hex hex = battleMap.getHex(q, deployRowAttacker);
                if (hex == null) hex = battleMap.getHex(0, deployRowAttacker);
                attackerUnits.add(new BattleUnit(reg, hex));
            }
        }
        
        // Размещаем полки защитника на противоположной стороне
        int deployRowDefender = 6;
        for (Regiment reg : defender.getRegiments()) {
            if (!reg.isDefeated()) {
                int q = (rand.nextInt(5) - 2) * colStep;
                Hex hex = battleMap.getHex(q, deployRowDefender);
                if (hex == null) hex = battleMap.getHex(0, deployRowDefender);
                defenderUnits.add(new BattleUnit(reg, hex));
            }
        }
    }
    
    public Army getAttacker() {
        return attacker;
    }
    
    public Army getDefender() {
        return defender;
    }
    
    public GameMap getBattleMap() {
        return battleMap;
    }
    
    public List<BattleUnit> getAttackerUnits() {
        return new ArrayList<>(attackerUnits);
    }
    
    public List<BattleUnit> getDefenderUnits() {
        return new ArrayList<>(defenderUnits);
    }
    
    public int getCurrentRound() {
        return currentRound;
    }
    
    public boolean isAttackerTurn() {
        return isAttackerTurn;
    }
    
    public boolean isFinished() {
        return isFinished;
    }
    
    public Army getWinner() {
        return winner;
    }
    
    /**
     * Переместить боевую единицу.
     */
    public boolean moveUnit(BattleUnit unit, Hex targetHex) {
        if (!unit.canAttack()) return false;
        if (unit.hasActed()) return false;
        
        int distance = unit.getPosition().distanceTo(targetHex);
        if (distance > unit.getMovementRange()) return false;
        
        // Проверка, что гекс свободен
        for (BattleUnit bu : attackerUnits) {
            if (bu.getPosition().equals(targetHex)) return false;
        }
        for (BattleUnit bu : defenderUnits) {
            if (bu.getPosition().equals(targetHex)) return false;
        }
        
        unit.setPosition(targetHex);
        return true;
    }
    
    /**
     * Атаковать вражескую единицу.
     */
    public BattleResult attack(BattleUnit attackerUnit, BattleUnit defenderUnit) {
        if (!attackerUnit.canAttack()) return null;
        if (attackerUnit.hasActed()) return null;
        
        int distance = attackerUnit.getPosition().distanceTo(defenderUnit.getPosition());
        if (distance > attackerUnit.getAttackRange()) return null;
        
        // Расчёт урона
        int baseDamage = attackerUnit.getDamage();
        
        // Бонус местности защитника
        int terrainBonus = defenderUnit.getPosition().terrain.getDefenseBonus();
        
        // Случайный фактор (0.8 - 1.2)
        float randomFactor = 0.8f + (float)Math.random() * 0.4f;
        
        int finalDamage = (int)(baseDamage * randomFactor) + terrainBonus;
        finalDamage = Math.max(1, finalDamage);
        
        // Нанесение урона
        defenderUnit.getRegiment().setHealth(
            defenderUnit.getRegiment().getHealth() - finalDamage
        );
        
        // Артиллерия должна перезаряжаться
        if (attackerUnit.getRegiment().getTypeId().equals("artillery")) {
            attackerUnit.setReloading(true);
        }
        
        attackerUnit.setHasActed(true);
        
        return new BattleResult(attackerUnit, defenderUnit, finalDamage, 
            defenderUnit.getRegiment().isDefeated());
    }
    
    /**
     * Завершить ход единицы без действий.
     */
    public void holdPosition(BattleUnit unit) {
        unit.setHasActed(true);
    }
    
    /**
     * Завершить фазу хода и перейти к следующей.
     */
    public void endPhase() {
        List<BattleUnit> currentUnits = isAttackerTurn ? attackerUnits : defenderUnits;
        
        // Проверка, все ли единицы походили
        for (BattleUnit unit : currentUnits) {
            if (!unit.hasActed() && !unit.getRegiment().isDefeated()) {
                return; // Не все походили
            }
        }
        
        // Сброс состояния единиц
        for (BattleUnit unit : currentUnits) {
            unit.setHasActed(false);
            if (unit.isReloading()) {
                unit.setReloading(false);
            }
        }
        
        // Смена хода
        isAttackerTurn = !isAttackerTurn;
        
        // Если ход атакующего - новый раунд
        if (isAttackerTurn) {
            currentRound++;
        }
        
        checkBattleEnd();
    }
    
    private void checkBattleEnd() {
        boolean attackerHasUnits = false;
        boolean defenderHasUnits = false;
        
        for (BattleUnit unit : attackerUnits) {
            if (!unit.getRegiment().isDefeated()) {
                attackerHasUnits = true;
                break;
            }
        }
        
        for (BattleUnit unit : defenderUnits) {
            if (!unit.getRegiment().isDefeated()) {
                defenderHasUnits = true;
                break;
            }
        }
        
        if (!attackerHasUnits) {
            isFinished = true;
            winner = defender;
        } else if (!defenderHasUnits) {
            isFinished = true;
            winner = attacker;
        }
    }
    
    /**
     * Получить опыт для выживших полков победителя.
     */
    public void applyVictoryRewards() {
        if (winner == null) return;
        
        List<BattleUnit> winningUnits = winner.equals(attacker) ? attackerUnits : defenderUnits;
        for (BattleUnit unit : winningUnits) {
            if (!unit.getRegiment().isDefeated()) {
                unit.getRegiment().addExperience(1);
            }
        }
        
        // Удаление уничтоженных полков из армий
        attacker.removeDefeatedRegiments();
        defender.removeDefeatedRegiments();
    }
    
    @Override
    public String toString() {
        return String.format("Battle: %s vs %s (Round %d, %s turn)",
            attacker.getName(), defender.getName(), currentRound,
            isAttackerTurn ? "Attacker" : "Defender");
    }
}
