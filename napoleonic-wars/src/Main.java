import config.ConfigLoader;
import game.Game;

/**
 * Главная точка входа в игру.
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("Napoleonic Wars Strategy Game");
        System.out.println("==============================");
        
        try {
            // Загрузка конфигурации
            ConfigLoader config = new ConfigLoader("config");
            config.loadAll();
            
            System.out.println("Loaded " + config.getCountries().size() + " countries");
            System.out.println("Loaded " + config.getRegimentTypes().size() + " regiment types");
            
            // Создание и инициализация игры
            Game game = new Game(config);
            game.initialize();
            
            System.out.println("\nGame initialized!");
            System.out.println("Current turn: " + game.getCurrentTurn());
            System.out.println("Current player: " + game.getCurrentPlayerId());
            
            // Вывод информации о странах
            System.out.println("\nCountries:");
            for (var country : game.getCountries()) {
                var state = game.getCountryState(country.id);
                System.out.printf("  %s (%s) - Gold: %d, Regions: %d%n", 
                    country.name, country.leader, state.gold, state.regions.size());
            }
            
            // Вывод информации об армиях
            System.out.println("\nArmies:");
            for (var army : game.getArmies()) {
                System.out.printf("  %s [%s] at (%d,%d) - %d regiments%n",
                    army.getName(), army.getCountryId(),
                    army.getPosition() != null ? army.getPosition().q : 0,
                    army.getPosition() != null ? army.getPosition().r : 0,
                    army.getRegimentCount());
                
                for (var reg : army.getRegiments()) {
                    System.out.printf("    - %s HP:%d/%d DMG:%d RNG:%d SPD:%d%n",
                        reg.getTypeId(), reg.getHealth(), reg.getMaxHealth(),
                        reg.getDamage(), reg.getRange(), reg.getSpeed());
                }
            }
            
            System.out.println("\nGame is ready! Launch GUI with: java ui.GameWindow");
            
        } catch (Exception e) {
            System.err.println("Error initializing game: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
