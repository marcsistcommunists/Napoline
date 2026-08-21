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
        private static final int HEX_WIDTH = (int)(HEX_SIZE * Math.sqrt(3));
        private static final int HEX_HEIGHT = HEX_SIZE * 2;
        
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
            
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            
            // Отрисовка гексов
            for (Hex hex : game.getGameMap().getAllHexes()) {
                Point screenPos = hexToScreen(hex.q, hex.r, centerX, centerY);
                drawHex(g2d, screenPos.x, screenPos.y, hex);
            }
            
            // Отрисовка выделения
            if (selectedHex != null) {
                Point screenPos = hexToScreen(selectedHex.q, selectedHex.r, centerX, centerY);
                drawHexHighlight(g2d, screenPos.x, screenPos.y, Color.YELLOW);
            }
        }
        
        private Point hexToScreen(int q, int r, int centerX, int centerY) {
            int x = centerX + (int)(HEX_WIDTH * q + HEX_WIDTH / 2f * r);
            int y = centerY + (int)(HEX_HEIGHT * 3f / 4f * r);
            return new Point(x, y);
        }
        
        private void drawHex(Graphics2D g2d, int x, int y, Hex hex) {
            int[] xPoints = new int[6];
            int[] yPoints = new int[6];
            
            for (int i = 0; i < 6; i++) {
                double angle = Math.PI / 3 * i;
                xPoints[i] = x + (int)(HEX_SIZE * Math.cos(angle));
                yPoints[i] = y + (int)(HEX_SIZE * Math.sin(angle));
            }
            
            // Цвет по типу местности
            Color terrainColor = getTerrainColor(hex.terrain);
            g2d.setColor(terrainColor);
            g2d.fillPolygon(xPoints, yPoints, 6);
            
            // Цвет региона по владельцу
            if (hex.region != null && hex.region.getOwnerId() != null) {
                Color ownerColor = getOwnerColor(hex.region.getOwnerId());
                g2d.setColor(ownerColor);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawPolygon(xPoints, yPoints, 6);
            } else {
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawPolygon(xPoints, yPoints, 6);
            }
            
            // Отрисовка города
            if (hex.city != null) {
                g2d.setColor(Color.WHITE);
                g2d.fillOval(x - 5, y - 5, 10, 10);
                g2d.setColor(Color.BLACK);
                g2d.drawOval(x - 5, y - 5, 10, 10);
            }
            
            // Отрисовка армий
            for (Army army : hex.getArmies()) {
                drawArmy(g2d, x, y, army);
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
        
        private void drawHexHighlight(Graphics2D g2d, int x, int y, Color color) {
            int[] xPoints = new int[6];
            int[] yPoints = new int[6];
            
            for (int i = 0; i < 6; i++) {
                double angle = Math.PI / 3 * i;
                xPoints[i] = x + (int)(HEX_SIZE * Math.cos(angle));
                yPoints[i] = y + (int)(HEX_SIZE * Math.sin(angle));
            }
            
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawPolygon(xPoints, yPoints, 6);
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            if (game == null) return;
            
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            
            // Поиск ближайшего гекса
            Hex closestHex = null;
            int minDistance = Integer.MAX_VALUE;
            
            for (Hex hex : game.getGameMap().getAllHexes()) {
                Point screenPos = hexToScreen(hex.q, hex.r, centerX, centerY);
                int distance = (int)Math.hypot(screenPos.x - e.getX(), screenPos.y - e.getY());
                
                if (distance < HEX_SIZE && distance < minDistance) {
                    minDistance = distance;
                    closestHex = hex;
                }
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
                sb.append("Гекс: (").append(hex.q).append(",").append(hex.r).append(")\n");
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
                Hex targetHex = game.getGameMap().getHex(selectedHex.q + dir[0], selectedHex.r + dir[1]);
                
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
