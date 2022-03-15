package pers.le.tetris;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class View extends JFrame {

    // 窗口尺寸
    private static final int widowWidth =
        (Gaming.MAP_WIDTH + 12) * Gaming.BlockSize;
    private static final int windowHeight =
        Gaming.BlockSize * Gaming.MAP_HEIGHT + 80;

    // 定义模式常量
    public static final boolean MAIN_WINDOW = true;
    public static final boolean SECONDARY_WINDOW = false;

    private Gaming gaming; // 当前View的Gaming
    private static View secondaryView; // 副窗口

    private boolean mode; // 记录当前View的模式

    public View() {
        this(MAIN_WINDOW); // 默认为主窗口
    }

    public View(boolean mode) {
        super();
        this.mode = mode;
        init(); // 初始化
    }

    // 初始化，生成界面
    private void init() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // 获取屏幕尺寸
        int locationX; // 窗口左上角的x坐标
        int locationY = (int) screenSize.getHeight() / 2 - windowHeight / 2; //窗口左上角的y坐标
        this.setSize(widowWidth, windowHeight);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setResizable(false);
        gaming = new Gaming(this.mode); // 创建Gaming对象
        gaming.setFocusable(true);
        //主窗口与副窗口获得不同的资源
        if (mode == MAIN_WINDOW) {
            locationX = (int) screenSize.getWidth() * 3 / 4 - widowWidth / 2; // 主窗口左上角的x坐标
            this.setJMenuBar(menuBar()); // 主窗口添加菜单栏
            // 监听窗口关闭事件
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);
                    // 弹出是否确认退出的对话框
                    if (JOptionPane.showConfirmDialog(View.this, "确定要退出吗？", "退出", 0) == JOptionPane.YES_OPTION)
                        System.exit(0); // 点击了是，退出程序
                }
            });
        } else {
            locationX = (int) screenSize.getWidth() / 4 - widowWidth / 2; // 副窗口左上角的x坐标
            // 监听窗口关闭事件
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);
                    closeSecondaryView(); // 关闭副窗口
                }
            });
        }
        this.add(gaming); // 添加gaming
        this.setLocation(locationX, locationY);
    }

    //创建菜单栏并将其返回
    private JMenuBar menuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu gameMenu = new JMenu("游戏");
        JMenuItem newGameItem = gameMenu.add("开始新游戏");
        JMenuItem pauseItem = gameMenu.add("暂停");
        JMenuItem speedItem = gameMenu.add("速度");
        JMenuItem keySensitivityItem = gameMenu.add("按键灵敏度");
        JMenuItem exitItem = gameMenu.add("退出");

        JMenu modeMenu = new JMenu("模式");
        JMenuItem singleMode = modeMenu.add("单人模式");
        JMenuItem doubleMode = modeMenu.add("双人对战");

        // 为菜单项注册点击事件监听器，传入当前View（即主窗口）对象和菜单项的ID
        newGameItem.addActionListener(new OnClickListener(this, ID.New_Game_Item));
        pauseItem.addActionListener(new OnClickListener(this, ID.Pause_Item));
        speedItem.addActionListener(new OnClickListener(this, ID.Speed_Item));
        keySensitivityItem.addActionListener(new OnClickListener(this, ID.Key_Sensitivity_Item));
        exitItem.addActionListener(new OnClickListener(this, ID.Exit_Item));
        singleMode.addActionListener(new OnClickListener(this, ID.Single_Mode_Item));
        doubleMode.addActionListener(new OnClickListener(this, ID.Double_Mode_Item));

        menuBar.add(gameMenu);
        menuBar.add(modeMenu);

        return menuBar;
    }

    // 启用了双人模式，新建一个副窗口
    public void newSecondaryView() {
        if (secondaryView == null) {
            secondaryView = new View(View.SECONDARY_WINDOW);
            secondaryView.setVisible(true);
            Gaming.mainGaming.init(); // 主游戏重新开始游戏
            setPause(false); //取消暂停
        }
    }

    // 关闭双人模式，销毁副窗口全部资源
    public void closeSecondaryView() {
        if (secondaryView != null) {
            secondaryView.dispose();
            secondaryView = null;
        }
    }

    // 暂停/继续
    public void setPause(boolean pause) {
        // 释放键盘上的全部按键
        for (int i = 0; i < OnKeyListener.keysState.length; i++)
            OnKeyListener.keysState[i] = false;
        gaming.setPause(pause);
        if (pause)
            this.getJMenuBar().getMenu(0).getItem(1).setText("继续");
        else
            this.getJMenuBar().getMenu(0).getItem(1).setText("暂停");
    }

    // 设置速度
    public void setSpeed() {
        setPause(true); // 暂停
        String tempString = "" + 1000 / Gaming.getPeriod(); // 获得当前速度
        tempString = JOptionPane.showInputDialog(this, "请输入速度（单位：格/s）", tempString);
        // 输入不合法内容并按下了确认键则给予提示
        while (tempString != null && !tempString.matches("[1-9]|10")) {
            JOptionPane.showMessageDialog(this, "设置失败，数值应在1-10之间");
            tempString = JOptionPane.showInputDialog(this, "请输入（单位：格/s）", tempString);
        }
        if (tempString != null)
            gaming.setPeriod(1000 / Integer.parseInt(tempString)); // 修改速度
        setPause(false); // 继续
    }

    public void setKeySensitivity() {
        setPause(true); // 暂停
        String tempString = "" + (21 - OnKeyListener.timeDelay / 10); // 获得当前按键灵敏度
        tempString = JOptionPane.showInputDialog(this, "请输入按键灵敏度", tempString);
        // 输入不合法内容并按下了确认键则给予提示
        while (tempString != null && !tempString.matches("[1-9]|1[0-6]?")) {
            JOptionPane.showMessageDialog(this, "设置失败，数值应在1-16之间");
            tempString = JOptionPane.showInputDialog(this, "请输入按键灵敏度", tempString);
        }
        if (tempString != null)
            OnKeyListener.timeDelay = (21 - Integer.parseInt(tempString)) * 10;  // 修改按键灵敏度
        setPause(false); // 继续
    }

}