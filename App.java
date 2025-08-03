import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class App {

    public static void main(String[] args) {
        int startingRows = 8;
        if (args.length > 0) {
            try {
                startingRows = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid input. Starting with default 8.");
            }
        }

        int finalStartingRows = startingRows;
        SwingUtilities.invokeLater(() -> {
            showTitleScreen(() -> showInstructionScreen(() -> startGame(finalStartingRows)));
        });
    }

    static void startGame(int rows) {
        int level = (rows - 8) / 2 + 1;
        SwingUtilities.invokeLater(() -> {
            new Minesweeper(rows, level, (won, nextLevel) -> {
                if (won) {
                    int nextRows = 8 + (nextLevel - 1) * 2;
                    startGame(nextRows);
                } else {
                    System.out.println("Game over at size: " + rows + "x" + rows);
                    System.exit(0);
                }
            });
        });
    }

    static void showTitleScreen(Runnable onStart) {
        JFrame titleFrame = new JFrame("Welcome to Space Minesweeper!");
        titleFrame.setSize(800, 600);
        titleFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        titleFrame.setLocationRelativeTo(null);
        titleFrame.setLayout(new BorderLayout());

        StarfieldPanel starfield = new StarfieldPanel();
        starfield.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Cosmic Mines", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Consolas", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);

        JLabel instructionLabel = new JLabel("Press SPACE to start", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        instructionLabel.setForeground(Color.LIGHT_GRAY);

        starfield.add(titleLabel, BorderLayout.CENTER);
        starfield.add(instructionLabel, BorderLayout.SOUTH);

        titleFrame.add(starfield);
        titleFrame.setFocusable(true);
        titleFrame.setVisible(true);
        titleFrame.requestFocusInWindow();

        titleFrame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    titleFrame.dispose();
                    onStart.run();
                }
            }
        });
    }

    static void showInstructionScreen(Runnable onDone) {
        JFrame frame = new JFrame("Instructions");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        InstructionScreen instructionPanel = new InstructionScreen(onDone);
        frame.add(instructionPanel);
        frame.setVisible(true);
        instructionPanel.requestFocusInWindow();
    }

    static class InstructionScreen extends JPanel {
        private String[] lines = {
            "Welcome to Cosmic Mines!",
            "",
            "HOW TO PLAY:",
            "",
            "- Left-click to reveal a tile.",
            "- Right-click to mark a mine.",
            "- Clear all safe tiles to win.",
            "- You have 3 hints to help you.",
            "- Make sure to beat the clock!",
            "- One wrong move and it's over!",
            "",
            "Press SPACE to begin your journey..."
        };

        private boolean[] boldLines = {
            true,   // Welcome to Cosmic Mines!
            false,
            true,   // HOW TO PLAY:
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            true    // Press SPACE to begin your journey...
        };

        private int currentLine = 0;
        private Timer lineTimer;
        private Runnable onDone;

        private Image[] images = new Image[4];
        private Point[] imagePositions = new Point[4];
        private Random rand = new Random();

        private Star[] stars;
        private Timer starTimer;

        public InstructionScreen(Runnable onDone) {
            this.onDone = onDone;
            setBackground(new Color(0, 0, 48));  // darker navy background
            setFocusable(true);

            images[0] = loadImage("b1.png");
            images[1] = loadImage("b2.png");
            images[2] = loadImage("b3.png");
            images[3] = loadImage("b4.png");

            int panelWidth = 800;
            int panelHeight = 600;
            int textStartY = 100;

            // Left side images with vertical spacing
            imagePositions[0] = new Point(60, textStartY + 0 * 90);   // top-left
            imagePositions[1] = new Point(60, textStartY + 2 * 90);   // below the first on left side

            // Right side images moved closer to center
            int rightX = panelWidth - 160; // moved left from 690 to 640
            imagePositions[2] = new Point(rightX, textStartY + 1 * 90); // top-right
            imagePositions[3] = new Point(rightX, textStartY + 3 * 90); // below the third on right side

            initStars(80);

            starTimer = new Timer(50, e -> {
                for (Star star : stars) {
                    star.move(getWidth(), getHeight());
                }
                repaint();
            });
            starTimer.start();

            lineTimer = new Timer(600, e -> {
                currentLine++;
                if (currentLine > lines.length) currentLine = lines.length;
                repaint();
            });
            lineTimer.start();

            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                        lineTimer.stop();
                        starTimer.stop();
                        Window w = SwingUtilities.getWindowAncestor(InstructionScreen.this);
                        if (w != null) w.dispose();
                        onDone.run();
                    }
                }
            });
        }

        private Image loadImage(String path) {
            ImageIcon icon = new ImageIcon(path);
            return icon.getImage();
        }

        private void initStars(int count) {
            stars = new Star[count];
            for (int i = 0; i < count; i++) {
                stars[i] = new Star(rand.nextInt(800), rand.nextInt(600));
            }
        }

        class Star {
            int x, y, size;
            float brightness;
            boolean brightening;
            Random rand = new Random();

            public Star(int x, int y) {
                this.x = x;
                this.y = y;
                this.size = rand.nextInt(2) + 1;
                this.brightness = rand.nextFloat() * 0.7f + 0.3f;
                this.brightening = rand.nextBoolean();
            }

            void move(int width, int height) {
                if (brightening) {
                    brightness += 0.01f;
                    if (brightness >= 1f) {
                        brightness = 1f;
                        brightening = false;
                    }
                } else {
                    brightness -= 0.01f;
                    if (brightness <= 0.3f) {
                        brightness = 0.3f;
                        brightening = true;
                    }
                }
            }

            void draw(Graphics2D g2) {
                int alpha = (int) (brightness * 255);
                g2.setColor(new Color(255, 255, 255, alpha));
                g2.fillOval(x, y, size, size);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            // Draw starfield behind everything
            if (stars != null) {
                for (Star star : stars) {
                    star.draw(g2);
                }
            }

            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int width = getWidth();
            int lineHeight = 36;
            int startY = 100;

            // Draw text lines up to currentLine
            for (int i = 0; i < currentLine && i < lines.length; i++) {
                String line = lines[i];
                boolean bold = false;
                if (i < boldLines.length) {
                    bold = boldLines[i];
                }

                g2.setColor(Color.WHITE);
                if (bold) {
                    g2.setFont(new Font("Serif", Font.BOLD, 28));
                } else {
                    g2.setFont(new Font("Serif", Font.PLAIN, 24));
                }

                FontMetrics fm = g2.getFontMetrics();
                int strWidth = fm.stringWidth(line);
                int x = (width - strWidth) / 2;
                int y = startY + i * lineHeight;

                g2.drawString(line, x, y);
            }

            // Draw images once the first line is revealed (title)
            if (currentLine >= 1) {
                for (int i = 0; i < images.length; i++) {
                    if (images[i] != null && imagePositions[i] != null) {
                        g2.drawImage(images[i], imagePositions[i].x, imagePositions[i].y, this);
                    }
                }
            }

            g2.dispose();
        }
    }
    
    static void showGameOverScreen(boolean playerWon, int rows) {
    	JFrame gameOverFrame = new JFrame(playerWon ? "You Win!" : "Game Over");
    	gameOverFrame.setSize(800, 600);
    	gameOverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	gameOverFrame.setLocationRelativeTo(null);
    	gameOverFrame.setLayout(new BorderLayout());

    	StarfieldPanel backgroundPanel = new StarfieldPanel();
    	backgroundPanel.setLayout(new BorderLayout());
 	
    	JLabel mainLabel = new JLabel(playerWon ? "You Win!" : "Game Over", SwingConstants.CENTER);
    	mainLabel.setFont(new Font("Consolas", Font.BOLD, 48));
    	mainLabel.setForeground(Color.RED);
    	mainLabel.setOpaque(false);

    	JLabel instructionLabel = new JLabel("Press SPACE to play again", SwingConstants.CENTER);
    	instructionLabel.setFont(new Font("Arial", Font.PLAIN, 24));
    	instructionLabel.setForeground(Color.LIGHT_GRAY);
    	instructionLabel.setOpaque(false);

    	backgroundPanel.add(mainLabel, BorderLayout.CENTER);
    	backgroundPanel.add(instructionLabel, BorderLayout.SOUTH);

    	gameOverFrame.setContentPane(backgroundPanel);
    	gameOverFrame.setVisible(true);
    	backgroundPanel.requestFocusInWindow();

    	backgroundPanel.addKeyListener(new KeyAdapter() {
        	@Override
        	public void keyPressed(KeyEvent e) {
            	if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                	gameOverFrame.dispose();
                	startGame(rows);
            	}
        	}
    	});

    	backgroundPanel.setFocusable(true);
    	backgroundPanel.requestFocusInWindow();
	}

    static class StarfieldPanel extends JPanel {
        private Star[] stars;
        private Timer timer;
        private Random rand = new Random();
        private boolean initialized = false;

        public StarfieldPanel() {
            setBackground(Color.BLACK);
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    if (!initialized && getWidth() > 0 && getHeight() > 0) {
                        initStars(150);
                        initialized = true;
                        timer = new Timer(30, ev -> {
                            for (Star star : stars) {
                                star.move();
                            }
                            repaint();
                        });
                        timer.start();
                    }
                }
            });
        }

        private void initStars(int count) {
            stars = new Star[count];
            for (int i = 0; i < count; i++) {
                stars[i] = new Star();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (stars != null) {
                for (Star star : stars) {
                    star.draw(g);
                }
            }
        }

        class Star {
            int x, y, size, speed;
            Color color;
            Random rand = new Random();

            public Star() {
                reset();
            }

            void reset() {
                x = rand.nextInt(Math.max(getWidth(), 1));
                y = rand.nextInt(Math.max(getHeight(), 1));
                size = rand.nextInt(2) + 1;
                speed = rand.nextInt(2) + 1;
                int gray = rand.nextInt(100) + 155;
                color = new Color(gray, gray, gray);
            }

            void move() {
                y += speed;
                if (y > getHeight()) {
                    reset();
                    y = 0;
                }
            }

            void draw(Graphics g) {
                g.setColor(color);
                g.fillOval(x, y, size, size);
            }
        }
    }
}
