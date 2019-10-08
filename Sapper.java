import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;

public class Sapper extends JFrame {

    final String PROGRAM_NAME = "Sapper";
    final String FLAG_SYMBOL = "|>";
    final int CELL_SIZE = 40;
    final int NUMBER_OF_CELLS = 10;
    final int MARGIN_X = 6;
    final int MARGIN_Y = 45;
    final int START_POINT = 200;
    final int MOUSE_BUTTON_LEFT = 1;
    final int MOUSE_BUTTON_RIGHT = 3;
    final int NUMBER_OF_MINES = 15;
    final int[] COLOR_OF_NUMBERS = {0x3333FF, 0x006600, 0xFF3333, 0x660000, 0x741B47, 0xFF9933, 0x783F04, 0x0};
    Cell[][] field = new Cell[NUMBER_OF_CELLS][NUMBER_OF_CELLS];
    Random random = new Random();
    int countOpenedCells;
    boolean youWon, explosionMine;
    int bangX, bangY;

    public static void main(String[] args) {

        new Sapper();
    }

    Sapper() {
        setTitle(PROGRAM_NAME);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(START_POINT, START_POINT, NUMBER_OF_CELLS*CELL_SIZE + MARGIN_X, NUMBER_OF_CELLS*CELL_SIZE
                + MARGIN_Y);
        final TimerLabel timeLabel = new TimerLabel();
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        setResizable(false);
        final Canvas canvas = new Canvas();
        canvas.setBackground(Color.white);
        canvas.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                int x = e.getX() / CELL_SIZE;
                int y = e.getY() / CELL_SIZE;
                if (!explosionMine && !youWon) {
                    if (e.getButton() == MOUSE_BUTTON_LEFT) {
                        if (field[y][x].isNotOpen()) {
                            openCells(x, y);
                            youWon = countOpenedCells == NUMBER_OF_CELLS * NUMBER_OF_CELLS - NUMBER_OF_MINES;
                            if (explosionMine) {
                                bangX = x;
                                bangY = y;
                            }
                        }
                    }
                    if (e.getButton() == MOUSE_BUTTON_RIGHT) {
                        field[y][x].inverseFlag();
                    }
                    if (explosionMine || youWon) {
                        timeLabel.stopTimer();
                    }
                    canvas.repaint();
                }
            }
        });

        add(BorderLayout.CENTER, canvas);
        add(BorderLayout.NORTH, timeLabel);
        setVisible(true);
        initField();
    }

    void openCells(int x, int y) { //Метод открытия ячеек
        if (x < 0 || x > NUMBER_OF_CELLS - 1 || y < 0 || y > NUMBER_OF_CELLS - 1) return;
        if (!field[y][x].isNotOpen()) return;
        field[y][x].open();
        if (field[y][x].getCountBomb() > 0 || explosionMine) return;
        for (int dx = -1; dx < 2; dx++)
            for (int dy = -1; dy < 2; dy++) openCells(x + dx, y + dy);
    }

    void initField() { //Метод инициализации полей
        int x, y, countMines = 0;

        for (x = 0; x < NUMBER_OF_CELLS; x++) {
            for (y = 0; y < NUMBER_OF_CELLS; y++) {
                field[y][x] = new Cell();
            }
        }

        while (countMines <  NUMBER_OF_MINES) {
            do {
                x = random.nextInt(NUMBER_OF_CELLS);
                y = random.nextInt(NUMBER_OF_CELLS);
            } while (field[y][x].isMined());
            field[y][x].mine();
            countMines++;
        }

        for (x = 0; x < NUMBER_OF_CELLS; x++) {
            for (y = 0; y < NUMBER_OF_CELLS; y++) {
                if (!field[y][x].isMined()) {
                    int count = 0;
                    for (int dx = -1; dx < 2; dx++) {
                        for (int dy = -1; dy < 2; dy++) {
                            int nX = x + dx;
                            int nY = y + dy;
                            if (nX < 0 || nY < 0 || nX > NUMBER_OF_CELLS - 1 || nY > NUMBER_OF_CELLS - 1) {
                                nX = x;
                                nY = y;
                            }
                            count += (field[nY][nX].isMined()) ? 1 : 0;
                        }
                        field[y][x].setCountBomb(count);
                    }
                }
            }
        }
    }

    public class Cell { //Клас описывающий ячейку

        private boolean isOpen, isMine, isFlag;
        private int countBombNear;

        void open() {
            isOpen = true;
            explosionMine = isMine;
            if (!isMine) {
                countOpenedCells++;
            }
        }

        void mine() {
            isMine = true;
        }

        boolean isNotOpen() {
            return !isOpen;
        }

        void inverseFlag() {
            isFlag = !isFlag;
        }

        boolean isMined() {
            return isMine;
        }

        void setCountBomb(int count) {
            countBombNear = count;
        }

        int getCountBomb() {
            return countBombNear;
        }

        void drawBomb(Graphics g, int x, int y, Color color) { //Метод рисование бомбы
            g.setColor(color);
            g.fillOval(x * CELL_SIZE + 10, y * CELL_SIZE + 10, 18, 18);
        }

        void paintString(Graphics g, String str, int x, int y, Color color) {//Метод рисования флага и количества момб
            g.setColor(color);
            g.setFont(new Font("", Font.BOLD, 20));
            g.drawString(str, x * CELL_SIZE + 12, y * CELL_SIZE + 26);
        }

        void draw(Graphics g, int x, int y) {
            g.setColor(Color.lightGray);
            g.drawRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            if (!isOpen) {
                if ((explosionMine || youWon) && isMine) {
                    drawBomb(g, x, y, Color.black);
                } else {
                    g.setColor(Color.lightGray);
                    g.fill3DRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE, true);
                    if (isFlag) {
                        paintString(g, FLAG_SYMBOL, x, y, Color.red);
                    }
                }
            } else if (isMine) {
                drawBomb(g, x, y, explosionMine ? Color.red : Color.black);
            } else if (countBombNear > 0) {
                paintString(g, Integer.toString(countBombNear), x, y, new Color(COLOR_OF_NUMBERS[countBombNear - 1]));
            }
        }
    }

    class Canvas extends JPanel { //Класс описывающий холст
        @Override
        public void paint (Graphics g) {
            super.paint(g);
            for (int x = 0; x < NUMBER_OF_CELLS; x++) {
                for (int y = 0; y < NUMBER_OF_CELLS; y++) {
                    field[y][x].draw(g, x, y);
                }
            }
        }
    }

    class TimerLabel extends JLabel { //Класс описывающий таймер

        Timer timer = new Timer();

        TimerLabel() { timer.scheduleAtFixedRate(timerTask, 0, 1000); }
        TimerTask timerTask = new TimerTask() {
            volatile int time;
            Runnable refresher = new Runnable() {
                public void run() {
                    TimerLabel.this.setText(String.format("%02d:%02d", time / 60, time % 60));
                }
            };
            public void run() {
                time++;
                SwingUtilities.invokeLater(refresher);
            }
        };

        void stopTimer() {
            timer.cancel();
        }
    }
}
