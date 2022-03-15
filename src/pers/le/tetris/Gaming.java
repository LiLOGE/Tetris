package pers.le.tetris;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Gaming extends JPanel {

    private static boolean pause; // true-暂停状态，false-非暂停状态。设为静态变量以实现两个Gaming对象进程同步暂停状态
    private boolean gameOver; // true-游戏结束，false-游戏未结束
    private static int period = 200; // 骨牌每次下落的间隔时间。 = 1000 / 速度
    private static Random random = new Random();

    // 主、副Gaming对象。设为静态以便于其它对象获得主、副Gaming对象
    public static Gaming mainGaming;
    public static Gaming secondaryGaming;

    private boolean mode; // 当前Gaming对象的主/副标识
    private int score = 0; // 游戏得分
    private static Font font;

    public static final int BlockSize = 20; // 方块边长
    public static final int MAP_WIDTH = 16; // 地图宽度（格）
    public static final int MAP_HEIGHT = 26; // 地图宽度（格）

    private boolean[][] blockMap; // 地图数组，用以存储已固定的方块
    private boolean[][] currentBlock; // 正在下落的骨牌
    private boolean[][] nextBlock; // 下一块骨牌
    private Color nextColor; // 正在下落的骨牌的颜色
    private Color currentColor; // 下一个骨牌的颜色
    private Point currentBlockPosition; // 正在下落的骨牌的左上角位置

    private Timer timer = new Timer(); // 计时器
    private TimerTask timerTask; // 计时器的任务

    public Gaming() {
        this(View.MAIN_WINDOW);
    }

    public Gaming(Boolean mode) {
        font = new Font(Font.SANS_SERIF, Font.PLAIN, BlockSize);
        this.mode = mode;
        if (mode == View.MAIN_WINDOW) {
            mainGaming = this;
        } else {
            secondaryGaming = this;
        }
        // 双人模式时，两个窗口均有可能获得焦点，因此Gaming对象都要注册键盘监听器
        this.addKeyListener(new OnKeyListener());
        init();
    }

    // 更改下落周期
    public void setPeriod(int period) {
        Gaming.period = period;
        timer.cancel(); // 取消timer上的任务
        timer.purge(); // 移除timer上已被取消的任务
        timer = new Timer(); // 创建新的定时器
        timerTask.cancel(); // 取消任务
        timerTask = getTimerTask(); // 创建新的任务
        timer.scheduleAtFixedRate(timerTask, 0, period); // 启动新的任务
        if (mode != View.SECONDARY_WINDOW && secondaryGaming != null)
            secondaryGaming.setPeriod(period); // 若是双人模式则修改副Gaming的下落周期
    }

    // 更新暂停状态
    public void setPause(boolean pause) {
        Gaming.pause = pause;
        // 若是双人模式则修改副Gaming的暂停状态
        if (mode != View.SECONDARY_WINDOW && secondaryGaming != null)
            secondaryGaming.setPause(pause);
        this.repaint();
    }

    // 获得暂停状态
    public static boolean getPause() {
        return Gaming.pause;
    }

    // 获得游戏结束状态
    public boolean getGameOver() {
        return gameOver;
    }

    // 获得下落周期
    public static int getPeriod() {
        return period;
    }

    // 旋转下落中的骨牌
    public void rotateCurrentBlock() {
        // 使用临时变量tempBlock、tempPosition暂时存放旋转后的骨牌及其位置
        boolean[][] tempBlock = rotateBlock(currentBlock, 1); // 旋转一次（以左上角为轴）
        // 计算新的位置，让它看起来绕中点旋转
        Point tempPosition = new Point(currentBlockPosition.x - (currentBlock.length - currentBlock[0].length) / 2, currentBlockPosition.y + (currentBlock.length - currentBlock[0].length) / 2);
        if (tempPosition.x < 0)
            tempPosition.x = 0; // 旋转后若超出左边界则向右移
        if (tempBlock[0].length + tempPosition.x > Gaming.MAP_WIDTH)
            tempPosition.x = Gaming.MAP_WIDTH - tempBlock[0].length; // 旋转后若超出右边界则向左移
        // 如果旋转后不与已固定的方块重叠且没有超出边界则更新currentBlock、currentBlockPosition
        if (!overlap(tempBlock, tempPosition)) {
            currentBlock = tempBlock;
            currentBlockPosition = tempPosition;
            this.repaint();
        }
    }

    // 向左移动一格
    public void moveLeft() {
        // 正在下落的骨牌不在最左边才能向左移动一格
        if (currentBlockPosition.x != 0) {
            // 使用临时变量tempPosition暂时存放移动后的骨牌位置
            Point tempPoint = new Point(currentBlockPosition.x - 1, currentBlockPosition.y);
            // 如果移动后后不与已固定的方块重叠且没有超出边界则更新currentBlockPosition
            if (!overlap(currentBlock, tempPoint)) {
                currentBlockPosition = tempPoint;
                this.repaint();
            }
        }
    }

    // 向右移动一格
    public void moveRight() {
        // 正在下落的骨牌不在最右边才能向右移动一格
        if (currentBlockPosition.x + currentBlock[0].length < Gaming.MAP_WIDTH) {
            // 使用临时变量tempPosition暂时存放移动后的骨牌位置
            Point tempPoint = new Point(currentBlockPosition.x + 1, currentBlockPosition.y);
            // 如果移动后后不与已固定的方块重叠且没有超出边界则更新currentBlockPosition
            if (!overlap(currentBlock, tempPoint)) {
                currentBlockPosition = tempPoint;
                this.repaint();
            }
        }
    }

    // 向下移动一格
    public void moveDown() {
        // 正在下落的骨牌不在最下边才能向下移动一格
        if (currentBlockPosition.y + currentBlock.length < Gaming.MAP_HEIGHT) {
            // 使用临时变量tempPosition暂时存放移动后的骨牌位置
            Point tempPoint = new Point(currentBlockPosition.x, currentBlockPosition.y + 1);
            // 如果移动后后不与已固定的方块重叠且没有超出边界则更新currentBlockPosition
            if (!overlap(currentBlock, tempPoint)) {
                currentBlockPosition = tempPoint;
                this.repaint();
            }
        }
    }

    // 初始化游戏资源
    public void init() {
        score = 0;
        gameOver = false;
        currentBlock = getNewBlock();
        currentColor = getNewColor();
        nextBlock = getNewBlock();
        nextColor = getNewColor();
        currentBlockPosition = new Point(MAP_WIDTH / 2 - currentBlock[0].length / 2, -currentBlock.length);
        initBlockMap(); // 清空已固定的方块
        if (timerTask == null) {
            timerTask = getTimerTask(); // 定时器获得任务
            timer.scheduleAtFixedRate(timerTask, 0, period); // 启动定时器
            this.repaint();
        }
    }

    private Color getNewColor() {
        return new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }

    // 定时器的任务
    private TimerTask getTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                // 如果暂停了或者结束了，就什么也不干
                if (!pause && !gameOver) {
                    // 如果下落中的骨牌触碰到地图底部或其底部触碰到已固定的方块则执行以下操作
                    if (touch()) {
                        fixBlock(); // 将当前骨牌固定
                        clearLines(); // 清除成行的方块并积分
                        // 调用gameOver()计算游戏是否结束了，并更新gameOver标志
                        if (gameOver = gameOver()) {
                            // 游戏结束了释放键盘按键
                            if (mode == View.MAIN_WINDOW)
                                for (int i = 0; i < 4; i++)
                                    OnKeyListener.keysState[i] = false;
                            else
                                for (int i = 4; i < 8; i++)
                                    OnKeyListener.keysState[i] = false;
                        }
                        // 更新骨牌
                        currentColor = nextColor;
                        currentBlock = nextBlock;
                        nextBlock = getNewBlock();
                        nextColor = getNewColor();
                        currentBlockPosition.setLocation(MAP_WIDTH / 2 - currentBlock[0].length / 2, -currentBlock.length);
                    } else {
                        // 当前骨牌下落一格
                        currentBlockPosition.y++;
                    }
                    Gaming.this.repaint();
                }
            }
        };
    }

    // 初始化（清空）地图（已固定的放块）
    private void initBlockMap() {
        if (blockMap == null)
            blockMap = new boolean[Gaming.MAP_HEIGHT][Gaming.MAP_WIDTH];
        for (int i = 0; i < Gaming.MAP_HEIGHT; i++) {
            for (int j = 0; j < Gaming.MAP_WIDTH; j++) {
                blockMap[i][j] = false;
            }
        }
    }

    // 生成新的骨牌
    private static boolean[][] getNewBlock() {
        int n = random.nextInt(Blocks.Shape.length * 4); // 每种骨牌有四种方向
        return rotateBlock(Blocks.Shape[n / 4], n % 4); // 将n / 4中形状旋转n % 4次
    }

    // 将srcBlock旋转times次，返回destBlock
    private static boolean[][] rotateBlock(boolean[][] srcBlock, int times) {
        times %= 4;
        if (times == 0)
            return srcBlock;
        // 源骨牌的宽高
        int srcW = srcBlock[0].length;
        int srcH = srcBlock.length;
        boolean[][] destBlock = new boolean[srcW][srcH];
        for (; times > 0; times--) {
            //顺时针旋转90°
            for (int i = 0; i < srcW; i++) {
                for (int j = 0; j < srcH; j++) {
                    destBlock[i][j] = srcBlock[srcH - j - 1][i];
                }
            }
        }
        return destBlock;
    }

    // 判断下落中的骨牌是否触碰到地图底部或其底部是否触碰到已固定的方块
    private boolean touch() {
        if (currentBlockPosition.y + currentBlock.length >= Gaming.MAP_HEIGHT)
            return true;
        for (int i = 0; i < currentBlock.length; i++) {
            for (int j = 0; j < currentBlock[i].length; j++) {
                if (currentBlock[i][j] && currentBlockPosition.y + i + 1 >= 0 && blockMap[currentBlockPosition.y + i + 1][currentBlockPosition.x + j]) {
                    return true;
                }
            }
        }
        return false;
    }

    // 判断指定位置的指定骨牌是否与已固定的方块重叠，或是否超出边界底部
    private boolean overlap(boolean[][] block, Point position) {
        if (position.y + block.length > Gaming.MAP_HEIGHT)
            return true;
        for (int i = 0; i < block.length; i++) {
            for (int j = 0; j < block[i].length; j++) {
                if (block[i][j] && position.y + i >= 0 && position.x + j >= 0 && blockMap[position.y + i][position.x + j]) {
                    return true;
                } 
            }
        }
        return false;
    }

    // 清除地图中成行的方块，并积分
    private void clearLines() {
        int lines = 0;
        p:
        for (int i = 0; i < blockMap.length; i++) {
            for (int j = 0; j < blockMap[i].length; j++) {
                if (!blockMap[i][j])
                    continue p; // 当前行存在空块则跳过该行
            }
            // 程序执行至此，说明第i行不存在空缺
            lines++;
            // 清除该行，其上所有行往下移动一格
            for (int k = i; k > 0; k--) {
                blockMap[k] = blockMap[k - 1];
            }
            for (int k = 0; k < Gaming.MAP_WIDTH; k++)
                blockMap[0][k] = false;
        }
        score += lines * lines * 10; // 更新分数
    }

    // 判断游戏是否结束
    private boolean gameOver() {
        // 如果顶行存在方块则游戏结束
        for (boolean block : blockMap[0])
            if (block)
                return true;
        return false;
    }

    // 将当前骨牌固定
    private void fixBlock() {
        for (int i = 0; i < currentBlock.length; i++) {
            for (int j = 0; j < currentBlock[i].length; j++) {
                if (currentBlock[i][j] && currentBlockPosition.y + i >= 0) {
                    blockMap[currentBlockPosition.y + i][currentBlockPosition.x + j] = true;
                }
            }
        }
    }

    //绘制游戏界面
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(font);
        FontMetrics fontMetrics = g.getFontMetrics(font);
        // 绘制墙
        for (int i = 0; i < MAP_HEIGHT + 1; i++) {
            g.drawRect(0, i * BlockSize, BlockSize, BlockSize);
            g.drawRect((MAP_WIDTH + 1) * BlockSize, i * BlockSize, BlockSize, BlockSize);
        }
        for (int i = 0; i < MAP_WIDTH; i++) {
            g.drawRect((1 + i) * BlockSize, MAP_HEIGHT * BlockSize, BlockSize, BlockSize);
        }

        // 绘制已固定的块（blockMap）
        g.setColor(new Color(0x003F5F));
        for (int i = 0; i < blockMap.length; i++) {
            for (int j = 0; j < blockMap[i].length; j++) {
                if (blockMap[i][j])
                    g.fillRect(BlockSize * (j + 1), BlockSize * i, BlockSize, BlockSize);
            }
        }

        // 绘制当前骨牌（currentBlock）
        g.setColor(currentColor);
        for (int i = 0; i < currentBlock.length; i++) {
            for (int j = 0; j < currentBlock[i].length; j++) {
                if (currentBlock[i][j])
                    g.fillRect(BlockSize * (j + 1 + currentBlockPosition.x), BlockSize * (i + currentBlockPosition.y), BlockSize, BlockSize);
            }
        }

        // 绘制下一块骨牌（nextBlock）
        g.setColor(nextColor);
        for (int i = 0; i < nextBlock.length; i++) {
            for (int j = 0; j < nextBlock[i].length; j++) {
                if (nextBlock[i][j])
                    g.fillRect((23 - (nextBlock[0].length + 1) / 2 + j) * BlockSize, (9 - (nextBlock.length + 1) / 2 + i) * BlockSize, BlockSize, BlockSize);
            }
        }

        // 绘制网格（共两个网格，四组线条）
        g.setColor(new Color(207, 207, 207));
        for (int i = 0; i < MAP_HEIGHT; i++) {
            g.drawLine(BlockSize, i * BlockSize, (MAP_WIDTH + 1) * BlockSize, i * BlockSize);
        }
        for (int i = 0; i < MAP_WIDTH - 1; i++) {
            g.drawLine((i + 2) * BlockSize, 0, (i + 2) * BlockSize, MAP_HEIGHT * BlockSize);
        }
        g.setColor(new Color(195, 195, 195));
        for (int i = 0; i < 5; i++) {
            g.drawLine(BlockSize * (21 + i), 7 * BlockSize, (21 + i) * BlockSize, 11 * BlockSize);
        }
        for (int i = 0; i < 5; i++) {
            g.drawLine(21 * BlockSize, (7 + i) * BlockSize, 25 * BlockSize, (7 + i) * BlockSize);
        }

        g.setColor(new Color(0xAF0000));
        g.drawString("游戏分数", 23 * BlockSize - fontMetrics.stringWidth("游戏分数") / 2, 3 * BlockSize);
        g.drawString("" + score, 23 * BlockSize - fontMetrics.stringWidth("" + score) / 2, 5 * BlockSize);
        g.setColor(new Color(0x00AF00));
        g.drawString("开发者 LE", 23 * BlockSize - fontMetrics.stringWidth("开发者 LE") / 2, 25 * BlockSize);
        if (pause) {
            // 绘制暂停框
            g.setColor(new Color(247, 247, 247));
            g.fillRect(6 * BlockSize, 10 * BlockSize, 6 * BlockSize, 2 * BlockSize);
            g.setColor(new Color(63, 63, 191));
            g.drawRect(6 * BlockSize, 10 * BlockSize, 6 * BlockSize, 2 * BlockSize);
            g.drawString("PAUSE", 9 * BlockSize - fontMetrics.stringWidth("PAUSE") / 2, 11 * BlockSize + fontMetrics.getHeight() / 4);
        }
        if (gameOver) {
            // 绘制游戏结束框
            g.setColor(new Color(247, 247, 247));
            g.fillRect(5 * BlockSize, 10 * BlockSize, 8 * BlockSize, 2 * BlockSize);
            g.setColor(new Color(191, 63, 63));
            g.drawRect(5 * BlockSize, 10 * BlockSize, 8 * BlockSize, 2 * BlockSize);
            g.drawString("GAME OVER", 9 * BlockSize - fontMetrics.stringWidth("GAME OVER") / 2, 11 * BlockSize + fontMetrics.getHeight() / 4);
        }
    }

}