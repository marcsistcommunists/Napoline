package game;

import army.Army;
import army.Regiment;
import config.*;
import map.*;
import battle.Battle;
import java.util.*;

/**
 * Основное состояние игры.
 */
public class Game {
    private final ConfigLoader config;
    private GameMap gameMap;
    
    private final List<CountryConfig> countries = new ArrayList<>();
    private final Map<String, CountryState> countryStates = new HashMap<>();
    private final List<Army> armies = new ArrayList<>();
    
    private int currentTurn = 1;
    private String currentPlayerId;
    private boolean isGameOver = false;
    
    public Game(ConfigLoader config) {
        this.config = config;
    }
    
    /**
     * Инициализация новой игры.
     */
    public void initialize() {
        // Создаём карту
        gameMap = new GameMap(10);
        
        // Загружаем страны
        for (CountryConfig country : config.getCountries()) {
            countries.add(country);
            countryStates.put(country.id, new CountryState(country));
        }
        
        // Распределяем регионы и создаём стартовые армии
        setupInitialPositions();
        
        // Первый игрок - Франция
        currentPlayerId = "france";
    }
    
    private void setupInitialPositions() {
        Random rand = new Random(42);
        
        for (CountryConfig country : countries) {
            CountryState state = countryStates.get(country.id);
            
            // Назначаем стартовые регионы
            for (String regionId : country.initialRegions) {
                Region region = createRegion(regionId, country.id);
                if (region != null) {
                    state.addRegion(region);
                    
                    // Создаём город в столице региона
                    Hex capitalHex = region.getHexes().get(0);
                    if (capitalHex != null) {
                        City city = new City(region.getName());
                        capitalHex.city = city;
                        capitalHex.terrain = TerrainType.CITY;
                        region.setCapital(capitalHex);
                        
                        // Добавляем доступные типы полков
                        for (String typeId : country.availableUnitTypes) {
                            city.addAvailableRegimentType(typeId);
                        }
                    }
                }
            }
            
            // Создаём стартовую армию в столице
            if (!state.regions.isEmpty()) {
                Region startRegion = state.regions.get(0);
                Hex capital = startRegion.getCapital();
                if (capital != null) {
                    Army army = createArmy(country.id, country.name + " Армия", capital);
                    
                    // Добавляем несколько стартовых полков
                    addRegimentsToArmy(army, "line_infantry", 2);
                    addRegimentsToArmy(army, "cuirassiers", 1);
                }
            }
        }
    }
    
    private Region createRegion(String id, String ownerId) {
        // Проверяем, существует ли уже регион с таким ID
        if (gameMap.getRegion(id) != null) {
            return gameMap.getRegion(id);
        }
        
        Region region = new Region(id, formatRegionName(id));
        region.setOwnerId(ownerId);
        
        // Находим случайный гекс для региона
        List<Hex> availableHexes = new ArrayList<>();
        for (Hex hex : gameMap.getAllHexes()) {
            if (hex.region == null) {
                availableHexes.add(hex);
            }
        }
        
        if (availableHexes.isEmpty()) return null;
        
        // Добавляем несколько гексов к региону
        Random rand = new Random();
        int numHexes = 3 + rand.nextInt(5);
        Hex firstHex = availableHexes.get(rand.nextInt(availableHexes.size()));
        region.addHex(firstHex);
        
        for (int i = 0; i < numHexes && !availableHexes.isEmpty(); i++) {
            Hex neighbor = getNeighborHex(firstHex, rand);
            if (neighbor != null && neighbor.region == null) {
                region.addHex(neighbor);
            }
        }
        
        gameMap.addRegion(region);
        return region;
    }
    
    private Hex getNeighborHex(Hex hex, Random rand) {
        List<Hex> neighbors = gameMap.getNeighbors(hex);
        if (neighbors.isEmpty()) return null;
        return neighbors.get(rand.nextInt(neighbors.size()));
    }
    
    private String formatRegionName(String id) {
        return id.replace('_', ' ').toUpperCase();
    }
    
    public Army createArmy(String countryId, String name, Hex position) {
        String armyId = "army_" + countryId + "_" + armies.size();
        Army army = new Army(armyId, name, countryId);
        army.setPosition(position);
        armies.add(army);
        return army;
    }
    
    private void addRegimentsToArmy(Army army, String typeId, int count) {
        RegimentTypeConfig typeConfig = config.getRegimentType(typeId);
        if (typeConfig == null) return;
        
        for (int i = 0; i < count; i++) {
            String regId = "reg_" + army.getId() + "_" + army.getRegimentCount();
            Regiment regiment = new Regiment(regId, typeId, army.getCountryId(), typeConfig);
            army.addRegiment(regiment);
        }
    }
    
    public ConfigLoader getConfig() {
        return config;
    }
    
    public GameMap getGameMap() {
        return gameMap;
    }
    
    public List<CountryConfig> getCountries() {
        return countries;
    }
    
    public CountryState getCountryState(String countryId) {
        return countryStates.get(countryId);
    }
    
    public List<Army> getArmies() {
        return new ArrayList<>(armies);
    }
    
    public List<Army> getArmiesByCountry(String countryId) {
        List<Army> result = new ArrayList<>();
        for (Army army : armies) {
            if (army.getCountryId().equals(countryId)) {
                result.add(army);
            }
        }
        return result;
    }
    
    public int getCurrentTurn() {
        return currentTurn;
    }
    
    public String getCurrentPlayerId() {
        return currentPlayerId;
    }
    
    public boolean isGameOver() {
        return isGameOver;
    }
    
    /**
     * Переместить армию на соседний гекс.
     */
    public boolean moveArmy(Army army, Hex targetHex) {
        if (army == null || targetHex == null) return false;
        if (!army.canMove()) return false;
        
        Hex currentHex = army.getPosition();
        if (currentHex == null) return false;
        
        // Проверка, что гексы соседние
        int distance = currentHex.distanceTo(targetHex);
        if (distance > 1) return false;
        
        // Проверка на вражескую армию
        if (targetHex.hasEnemyArmy(army.getCountryId())) {
            return false; // Нельзя двигаться на гекс с врагом
        }
        
        // Стоимость движения
        int cost = army.getMovementCost(targetHex.terrain);
        if (army.getMovementPoints() < cost) return false;
        
        // Перемещение
        army.spendMovementPoints(cost);
        army.setPosition(targetHex);
        
        return true;
    }
    
    /**
     * Начать бой между армиями.
     */
    public Battle startBattle(Army attacker, Army defender) {
        if (attacker == null || defender == null) return null;
        if (attacker.getPosition() == null || defender.getPosition() == null) return null;
        
        Battle battle = new Battle(attacker, defender, attacker.getPosition());
        return battle;
    }
    
    /**
     * Завершить ход текущего игрока.
     */
    public void endTurn() {
        // Восстановление очков движения для всех армий текущего игрока
        for (Army army : armies) {
            if (army.getCountryId().equals(currentPlayerId)) {
                army.setMovementPoints(10);
            }
        }
        
        // Сбор доходов
        CountryState state = countryStates.get(currentPlayerId);
        if (state != null) {
            for (Region region : state.regions) {
                state.gold += region.getIncome();
            }
        }
        
        // Следующий игрок
        int currentIndex = countries.indexOf(config.getCountry(currentPlayerId));
        int nextIndex = (currentIndex + 1) % countries.size();
        currentPlayerId = countries.get(nextIndex).id;
        
        // Если вернулись к первому игроку - новый ход
        if (currentPlayerId.equals("france")) {
            currentTurn++;
        }
        
        checkGameOver();
    }
    
    private void checkGameOver() {
        // Проверка условия победы (осталась одна страна)
        int activeCountries = 0;
        String lastCountry = null;
        
        for (CountryConfig country : countries) {
            CountryState state = countryStates.get(country.id);
            if (state != null && !state.regions.isEmpty()) {
                activeCountries++;
                lastCountry = country.id;
            }
        }
        
        if (activeCountries <= 1) {
            isGameOver = true;
        }
    }
    
    public String getWinner() {
        if (!isGameOver) return null;
        return currentPlayerId;
    }
}
