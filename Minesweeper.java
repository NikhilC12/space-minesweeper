import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

@FunctionalInterface
interface GameEndCallback {
    void onGameEnd(boolean won, int nextLevel);
}

public class Minesweeper {
    private class MineTile extends JButton {
        int r, c;
        ImageIcon assignedBackground;
        int bgIndex;
        boolean hovered = false;

        public MineTile(int r, int c) {
            this.r = r;
            this.c = c;
            assignedBackground = getRandomBackground();
            configureIcons();

            setFocusPainted(false);
            setBorderPainted(false);
            setMargin(new Insets(0, 0, 0, 0));
            setContentAreaFilled(false);
            setOpaque(false);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (gameOver) return;
                    MineTile tile = (MineTile) e.getSource();

                    if (firstClickedTile == null && e.getButton() == MouseEvent.BUTTON1) {
                        firstClickedTile = tile;
                        setMines(firstClickedTile);
                        startTimer();
                        checkMine(tile.r, tile.c);
                        return;
                    }

                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (tile.isEnabled() && tile.getIcon() == tile.assignedBackground) {
                            if (mineList.contains(tile)) {
                                tile.setIcon(mineIcon);
                                tile.setDisabledIcon(mineIcon);
                                revealMines();
                            } else {
                                checkMine(tile.r, tile.c);
                            }
                        }
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        if (tile.getIcon() == tile.assignedBackground && (mineCount - countFlag()) > 0) {
                            tile.setIcon(flagIcon);
                        } else if (tile.getIcon() == flagIcon) {
                            tile.setIcon(tile.assignedBackground);
                        }
                        updateHeaderText();
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        private ImageIcon getRandomBackground() {
            bgIndex = random.nextInt(originalBackgrounds.length);
            return scaleIcon(originalBackgrounds[bgIndex], tileSize);
        }

        void configureIcons() {
            setIcon(assignedBackground);
            setDisabledIcon(assignedBackground);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (getIcon() != null) {
                Image img = ((ImageIcon) getIcon()).getImage();
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            }
            if (hovered && isEnabled()) {
                g.setColor(new Color(0, 0, 0, 50));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    private ImageIcon scaleIcon(ImageIcon icon, int size) {
        Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private final ImageIcon[] originalBackgrounds = {
        loadIcon("back1.png"),
        loadIcon("back2.png"),
        loadIcon("back3.png")
    };

    private ImageIcon flagIcon = loadIcon("flag.png");
    private ImageIcon mineIcon = loadIcon("bomb.png");
    private ImageIcon[] blankIcons = new ImageIcon[] {
        loadIcon("b1.png"), loadIcon("b2.png"),
        loadIcon("b3.png"), loadIcon("b4.png")
    };
    private ImageIcon oneIcon = loadIcon("one.png");
    private ImageIcon twoIcon = loadIcon("two.png");
    private ImageIcon threeIcon = loadIcon("three.png");
    private ImageIcon fourIcon = loadIcon("four.png");
    private ImageIcon fiveIcon = loadIcon("five.png");
    private ImageIcon sixIcon = loadIcon("six.png");
    private ImageIcon sevenIcon = loadIcon("seven.png");
    private ImageIcon eightIcon = loadIcon("eight.png");

    private ImageIcon loadIcon(String filename) {
        return new ImageIcon(filename);
    }

    Timer swingTimer;
    int elapsedSeconds = 0;
    int timeLimit;

    private MineTile firstClickedTile = null;

    int tileSize;
    int numRows;
    int numCols;
    int boardWidth;
    int boardHeight;

    JFrame frame = new JFrame("Minesweeper");
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();

    int mineCount;
    MineTile[][] board;
    ArrayList<MineTile> mineList = new ArrayList<>();
    Random random = new Random();

    int tilesClicked = 0;
    boolean gameOver = false;
    GameEndCallback callback;

    private int currentLevel;
    private final int maxLevel = 10;

    private int remainingHints;
    private JButton hintButton;

    public Minesweeper(int numRows, int level, GameEndCallback callback) {
        this.currentLevel = level;
        this.numRows = numRows;
        this.numCols = numRows;
        this.tileSize = 750 / numRows;
        this.boardWidth = numCols * tileSize;
        this.boardHeight = numRows * tileSize;
        this.mineCount = (int) ((numRows * numRows) * 0.15);
        this.callback = callback;
        this.timeLimit = 60 * (numRows - 7);
        this.remainingHints = 3;

        board = new MineTile[numRows][numCols];
        hintButton = new JButton("Hint");

        frame.setSize(boardWidth, boardHeight + 50);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        textLabel.setFont(new Font("Consolas", Font.BOLD, 25));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setOpaque(true);

        textPanel.setLayout(new BorderLayout());
        textPanel.add(textLabel, BorderLayout.CENTER);



		hintButton.setEnabled(remainingHints > 0);
		hintButton.setFont(new Font("Consolas", Font.BOLD, 18));
		hintButton.setBackground(new Color(10, 10, 60)); 
		hintButton.setForeground(Color.WHITE);
		hintButton.setFocusPainted(false);
		hintButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
		hintButton.setPreferredSize(new Dimension(100, 40));
		hintButton.setOpaque(true);
		hintButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

		hintButton.addMouseListener(new MouseAdapter() {
    	@Override
    	public void mouseEntered(MouseEvent e) {
        	hintButton.setBackground(new Color(20, 20, 80)); 
    	}

    	@Override
    	public void mouseExited(MouseEvent e) {
        	hintButton.setBackground(new Color(10, 10, 60));
    	}
	});



        hintButton.addActionListener(e -> useHint());
        textPanel.add(hintButton, BorderLayout.EAST);

        frame.add(textPanel, BorderLayout.NORTH);

        boardPanel.setLayout(new GridLayout(numRows, numCols));
        frame.add(boardPanel, BorderLayout.CENTER);

        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                MineTile tile = new MineTile(r, c);
                board[r][c] = tile;
                boardPanel.add(tile);
            }
        }

        updateHeaderText();
        frame.setVisible(true);
    }



    void setMines(MineTile firstTile) {
        int mineLeft = mineCount;
        int orgRow = firstTile.r;
        int orgCol = firstTile.c;

        while (mineLeft > 0) {
            int r = random.nextInt(numRows);
            int c = random.nextInt(numCols);
            MineTile tile = board[r][c];

            if (!mineList.contains(tile) && !(Math.abs(r - orgRow) <= 1 && Math.abs(c - orgCol) <= 1)) {
                mineList.add(tile);
                mineLeft--;
            }
        }
    }

    void revealMines() {
        for (MineTile tile : mineList) {
            tile.setIcon(mineIcon);
            tile.setDisabledIcon(mineIcon);
            tile.setEnabled(false);
        }
        gameOver = true;
        stopTimer();
        textLabel.setText("Game Over!");

        Timer delay = new Timer(2000, e -> {
            frame.dispose();
            App.showGameOverScreen(false, 8);
        });
        delay.setRepeats(false);
        delay.start();
    }

    void checkMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) return;

        MineTile tile = board[r][c];
        if (!tile.isEnabled() || tile.getIcon() == flagIcon) return;

        tile.setEnabled(false);
        tilesClicked++;

        int minesFound = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                minesFound += countMine(r + dr, c + dc);
            }
        }

        if (minesFound > 0) {
            ImageIcon numIcon = switch (minesFound) {
                case 1 -> oneIcon;
                case 2 -> twoIcon;
                case 3 -> threeIcon;
                case 4 -> fourIcon;
                case 5 -> fiveIcon;
                case 6 -> sixIcon;
                case 7 -> sevenIcon;
                case 8 -> eightIcon;
                default -> null;
            };
            tile.setIcon(numIcon);
            tile.setDisabledIcon(numIcon);
        } else {
            ImageIcon blank = blankIcons[random.nextInt(blankIcons.length)];
            tile.setIcon(blank);
            tile.setDisabledIcon(blank);
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    checkMine(r + dr, c + dc);
                }
            }
        }

        updateHeaderText();

        if (tilesClicked == numRows * numCols - mineList.size()) {
            gameOver = true;
            stopTimer();
            textLabel.setText("Mines Cleared!   Time: " + elapsedSeconds + " seconds");

            Timer delay = new Timer(2000, e -> {
                frame.dispose();
                if (currentLevel < maxLevel) {
                    callback.onGameEnd(true, currentLevel + 1);
                } else {
                    JOptionPane.showMessageDialog(null, "Congratulations! You completed all 10 levels!");
                    frame.dispose();
                    //callback.onGameEnd(true, currentLevel);
                    App.showGameOverScreen(true, 8);
                }
            });
            delay.setRepeats(false);
            delay.start();
        }
    }

    int countFlag() {
        int count = 0;
        for (MineTile[] row : board) {
            for (MineTile tile : row) {
                if (tile != null && flagIcon.equals(tile.getIcon())) {
                    count++;
                }
            }
        }
        return count;
    }

    int countMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) return 0;
        return mineList.contains(board[r][c]) ? 1 : 0;
    }

    void updateHeaderText() {
    	textLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 5));
        int timeLeft = Math.max(0, timeLimit - elapsedSeconds);
        textLabel.setFont(new Font("Consolas", Font.BOLD, 18));
        textLabel.setText("Level " + currentLevel + "/" + maxLevel + "   Mines: " + (mineCount - countFlag()) + "   Time left: " + timeLeft + "s   Hints: " + remainingHints);
    	textLabel.setOpaque(true);             
		textLabel.setBackground(Color.BLACK); 
		textLabel.setForeground(Color.WHITE);  
    
    }
    
 
   void useHint() {
    if (firstClickedTile == null) {
        JOptionPane.showMessageDialog(frame, "Click a tile first before using a hint!", "Hint Unavailable", JOptionPane.WARNING_MESSAGE);
        return;
    }

    if (remainingHints > 0) {
        boolean hintFound = false;

        int attempts = 0;
        while (!hintFound && attempts < 1000) {
            attempts++;
            int r = random.nextInt(numRows);
            int c = random.nextInt(numCols);
            MineTile tile = board[r][c];

            if (!mineList.contains(tile) && tile.isEnabled() && tile.getIcon() == tile.assignedBackground) {
                ArrayList<MineTile> adjacentTiles = getAdjacentTiles(r, c);

                boolean bordersRevealedTile = false;
                for (MineTile adjTile : adjacentTiles) {
                    if (!adjTile.isEnabled()) {
                        bordersRevealedTile = true;
                        break;
                    }
                }

                if (bordersRevealedTile) {
                    checkMine(r, c);
                    remainingHints--;
                    updateHeaderText();
                    hintButton.setEnabled(remainingHints > 0);
                    hintFound = true;
                }
            }
        }

        if (!hintFound) {
            JOptionPane.showMessageDialog(frame, "No hintable tile found. Try again later.", "No Hint Found", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}


    
    ArrayList<MineTile> getAdjacentTiles(int r, int c) {
        ArrayList<MineTile> adjacentTiles = new ArrayList<>();
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue; 
                int nr = r + dr;
                int nc = c + dc;
                if (nr >= 0 && nr < numRows && nc >= 0 && nc < numCols) {
                    adjacentTiles.add(board[nr][nc]);
                }
            }
        }
        return adjacentTiles;
    }

    void startTimer() {
        swingTimer = new Timer(1000, e -> {
            elapsedSeconds++;
            updateHeaderText();
            if (elapsedSeconds >= timeLimit) {
                swingTimer.stop();
                revealMines();
            }
        });
        swingTimer.start();
    }

    void stopTimer() {
        if (swingTimer != null) swingTimer.stop();
    }
}
