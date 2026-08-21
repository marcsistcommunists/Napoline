package ui;

import config.*;
import game.*;
import map.*;
import army.*;
import battle.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Главное окно игры с графическим интерфейсом.
 */
public class GameWindow extends JFrame {
    
    private Game game;
    private Hex selectedHex;
    private Army selectedArmy;
    
    private final MapPanel mapPanel;
    private final InfoPanel infoPanel;
    private final ControlPanel controlPanel;
    
    public GameWindow() {
        super("Napoleonic Wars Strategy");
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        
        // Создание панелей
        mapPanel = new MapPanel();
        infoPanel = new InfoPanel();
        controlPanel = new ControlPanel();
        
        // Компоновка
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapPanel, infoPanel);
        splitPane.setDividerLocation(1000);
        
        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        
        // Инициализация игры
        initializeGame();
        
        setVisible(true);
    }
    
    private void initializeGame() {
        try {
            ConfigLoader config = new ConfigLoader("config");
            config.loadAll();
            
            game = new Game(config);
            game.initialize();
            
            mapPanel.repaint();
            updateInfoPanel();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error initializing game: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void updateInfoPanel() {
        if (game != null) {
            infoPanel.updateInfo(selectedHex, selectedArmy, game);
        }
    }
    
    /**
     * Панель отрисовки карты.
     */
    private class MapPanel extends JPanel implements MouseListener {
        
        private static final int HEX_SIZE = 30;
        // Flat-top ориентация: ширина = 2 * size, высота = sqrt(3) * size
        private static final int HEX_WIDTH = HEX_SIZE * 2;
        private static final int HEX_HEIGHT = (int)(HEX_SIZE * Math.sqrt(3));
        // Расстояние между центрами по горизонтали: 3/4 * ширина
        private static final int DX = (int)(HEX_WIDTH * 0.75);
        // Расстояние между центрами по вертикали: высота
        private static final int DY = HEX_HEIGHT;
        
        public MapPanel() {
            setBackground(new Color(30, 60, 30));
            addMouseListener(this);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (game == null || game.getGameMap() == null) return;
            
            int startX = 50;
            int startY = 50;
            
            // Отрисовка гексов столбцами
            for (int q = 0; q < game.getGameMap().getWidth(); q++) {
                for (int r = 0; r < game.getGameMap().getHeight(); r++) {
                    Hex hex = game.getGameMap().getHex(q, r);
                    if (hex == null) continue;
                    
                    // Вычисляем экранные координаты центра
                    // Для нечетных столбцов сдвиг по Y на половину высоты
                    int xOffset = q * DX;
                    int yOffset = r * DY;
                    if (q % 2 == 1) {
                        yOffset += DY / 2;
                    }
                    
                    int centerX = startX + xOffset;
                    int centerY = startY + yOffset;
                    
                    drawHex(g2d, hex, centerX, centerY);
                }
            }
            
            // Отрисовка выделения
            if (selectedHex != null) {
                // Находим экранные координаты для выделенного гекса
                int q = selectedHex.getQ();
                int r = selectedHex.getR();
                int xOffset = q * DX;
                int yOffset = r * DY;
                if (q % 2 == 1) {
                    yOffset += DY / 2;
                }
                int centerX = startX + xOffset;
                int centerY = startY + yOffset;
                drawHexHighlight(g2d, selectedHex, centerX, centerY, Color.YELLOW);
            }
        }
        
        private void drawHex(Graphics2D g2d, Hex hex, int centerX, int centerY) {
            Polygon poly = hex.getPolygon(HEX_SIZE, centerX, centerY);
            
            // Цвет по типу местности
            Color terrainColor = getTerrainColor(hex.terrain);
            g2d.setColor(terrainColor);
            g2d.fillPolygon(poly);
            
            // Цвет региона по владельцу
            if (hex.region != null && hex.region.getOwnerId() != null) {
                Color ownerColor = getOwnerColor(hex.region.getOwnerId());
                g2d.setColor(ownerColor);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawPolygon(poly);
            } else {
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawPolygon(poly);
            }
            
            // Отрисовка города
            if (hex.city != null) {
                g2d.setColor(Color.WHITE);
                g2d.fillOval(centerX - 5, centerY - 5, 10, 10);
                g2d.setColor(Color.BLACK);
                g2d.drawOval(centerX - 5, centerY - 5, 10, 10);
            }
            
            // Отрисовка армий
            for (Army army : hex.getArmies()) {
                drawArmy(g2d, centerX, centerY, army);
            }
        }
        
        private Color getTerrainColor(TerrainType terrain) {
            switch (terrain) {
                case PLAIN: return new Color(100, 140, 80);
                case FOREST: return new Color(40, 100, 40);
                case MOUNTAIN: return new Color(100, 100, 100);
                case HILL: return new Color(120, 110, 70);
                case RIVER: return new Color(60, 100, 140);
                case CITY: return new Color(80, 80, 100);
                case ROAD: return new Color(140, 130, 100);
                case SWAMP: return new Color(80, 90, 60);
                default: return Color.GREEN;
            }
        }
        
        private Color getOwnerColor(String ownerId) {
            CountryConfig country = game.getConfig().getCountry(ownerId);
            if (country != null && country.color != null) {
                return Color.decode("#" + country.color);
            }
            return Color.GRAY;
        }
        
        private void drawArmy(Graphics2D g2d, int x, int y, Army army) {
            Color armyColor = getOwnerColor(army.getCountryId());
            g2d.setColor(armyColor);
            
            // Рисуем треугольник для армии
            int size = 8;
            int[] xPoints = {x, x - size/2, x + size/2};
            int[] yPoints = {y - size, y + size/2, y + size/2};
            g2d.fillPolygon(xPoints, yPoints, 3);
            
            // Количество полков
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            String count = String.valueOf(army.getRegimentCount());
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(count);
            g2d.drawString(count, x - textWidth/2, y + size + 10);
        }
        
        private void drawHexHighlight(Graphics2D g2d, Hex hex, int centerX, int centerY, Color color) {
            Polygon poly = hex.getPolygon(HEX_SIZE, centerX, centerY);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawPolygon(poly);
        }
        
        private void drawHexHighlight(Graphics2D g2d, Hex hex, Color color) {
            // Для обратной совместимости - но теперь не используется
            Polygon poly = hex.getPolygon(HEX_SIZE, 0, 0);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawPolygon(poly);
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            if (game == null) return;
            
            int startX = 50;
            int startY = 50;
            
            // Поиск ближайшего гекса
            Hex closestHex = null;
            int minDistance = Integer.MAX_VALUE;
            
            for (int q = 0; q < game.getGameMap().getWidth(); q++) {
                for (int r = 0; r < game.getGameMap().getHeight(); r++) {
                    Hex hex = game.getGameMap().getHex(q, r);
                    if (hex == null) continue;
                    
                    // Вычисляем экранные координаты центра
                    int xOffset = q * DX;
                    int yOffset = r * DY;
                    if (q % 2 == 1) {
                        yOffset += DY / 2;
                    }
                    int centerX = startX + xOffset;
                    int centerY = startY + yOffset;
                    
                    if (hex.contains(e.getX(), e.getY(), HEX_SIZE, centerX, centerY)) {
                        closestHex = hex;
                        break;
                    }
                }
                if (closestHex != null) break;
            }
            
            selectedHex = closestHex;
            selectedArmy = (closestHex != null && !closestHex.getArmies().isEmpty()) 
                ? closestHex.getArmies().get(0) : null;
            
            updateInfoPanel();
            repaint();
        }
        
        @Override public void mousePressed(MouseEvent e) {}
        @Override public void mouseReleased(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
    }
    
    /**
     * Информационная панель.
     */
    private class InfoPanel extends JPanel {
        
        private final JTextArea infoText;
        
        public InfoPanel() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder("Информация"));
            
            infoText = new JTextArea();
            infoText.setEditable(false);
            infoText.setFont(new Font("Monospaced", Font.PLAIN, 12));
            add(new JScrollPane(infoText), BorderLayout.CENTER);
        }
        
        public void updateInfo(Hex hex, Army army, Game game) {
            StringBuilder sb = new StringBuilder();
            
            sb.append("Ход: ").append(game.getCurrentTurn()).append("\n");
            sb.append("Игрок: ").append(game.getCurrentPlayerId()).append("\n\n");
            
            if (hex != null) {
                sb.append("Гекс: (").append(hex.getQ()).append(",").append(hex.getR()).append(")\n");
                sb.append("Местность: ").append(hex.terrain.getName()).append("\n");
                
                if (hex.region != null) {
                    sb.append("Регион: ").append(hex.region.getName())
                      .append(" (").append(hex.region.getOwnerId()).append(")\n");
                }
                
                if (hex.city != null) {
                    sb.append("Город: ").append(hex.city.getName())
                      .append(" (Население: ").append(hex.city.getPopulation()).append(")\n");
                }
                
                if (!hex.getArmies().isEmpty()) {
                    sb.append("\nАрмии:\n");
                    for (Army a : hex.getArmies()) {
                        sb.append("  ").append(a.getName())
                          .append(" [").append(a.getCountryId()).append("]\n");
                        sb.append("    Полков: ").append(a.getRegimentCount()).append("\n");
                        sb.append("    ОД: ").append(a.getMovementPoints()).append("\n");
                        
                        for (Regiment reg : a.getRegiments()) {
                            sb.append("    - ").append(reg.getTypeId())
                              .append(" HP:").append(reg.getHealth()).append("/")
                              .append(reg.getMaxHealth())
                              .append(" DMG:").append(reg.getDamage())
                              .append(" EXP:").append(reg.getExperience()).append("\n");
                        }
                    }
                }
            } else {
                sb.append("Выберите гекс для просмотра информации");
            }
            
            infoText.setText(sb.toString());
        }
    }
    
    /**
     * Панель управления.
     */
    private class ControlPanel extends JPanel {
        
        public ControlPanel() {
            setLayout(new FlowLayout(FlowLayout.LEFT));
            
            JButton endTurnButton = new JButton("Конец хода");
            endTurnButton.addActionListener(e -> endTurn());
            add(endTurnButton);
            
            JButton moveButton = new JButton("Переместить");
            moveButton.addActionListener(e -> showMoveDialog());
            add(moveButton);
            
            JLabel turnLabel = new JLabel("Наполеоновские войны - Пошаговая стратегия");
            turnLabel.setFont(new Font("Arial", Font.BOLD, 14));
            add(turnLabel);
        }
        
        private void endTurn() {
            if (game != null) {
                game.endTurn();
                mapPanel.repaint();
                updateInfoPanel();
                
                JOptionPane.showMessageDialog(GameWindow.this,
                    "Ход завершён. Сейчас ходит: " + game.getCurrentPlayerId(),
                    "Конец хода", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        
        private void showMoveDialog() {
            if (selectedArmy == null) {
                JOptionPane.showMessageDialog(GameWindow.this,
                    "Выберите армию для перемещения",
                    "Внимание", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (!selectedArmy.canMove()) {
                JOptionPane.showMessageDialog(GameWindow.this,
                    "У армии нет очков движения",
                    "Внимание", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Простой диалог выбора направления
            String[] options = {"Север", "Северо-восток", "Юго-восток", 
                               "Юг", "Юго-запад", "Северо-запад"};
            int choice = JOptionPane.showOptionDialog(GameWindow.this,
                "Выберите направление для " + selectedArmy.getName(),
                "Перемещение армии",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
            
            if (choice >= 0 && selectedHex != null) {
                int[][] directions = {{0, -1}, {1, -1}, {1, 0}, {0, 1}, {-1, 1}, {-1, 0}};
                int[] dir = directions[choice];
                Hex targetHex = game.getGameMap().getHex(selectedHex.getQ() + dir[0], selectedHex.getR() + dir[1]);
                
                if (targetHex != null) {
                    if (game.moveArmy(selectedArmy, targetHex)) {
                        mapPanel.repaint();
                        updateInfoPanel();
                    } else {
                        JOptionPane.showMessageDialog(GameWindow.this,
                            "Невозможно переместиться сюда",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameWindow());
    }
}
