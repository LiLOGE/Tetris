package pers.le.tetris;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class OnKeyListener implements KeyListener {

    // 无论是哪个Gaming对象作为事件源都不影响这些静态变量的值，且各按键用到的资源都不同，因此不用考虑线程之间的同步问题
    public static boolean keysState[] = new boolean[8]; // 记录键盘按键的状态，true-被按着，false-没被按着
    private static Thread threads[] = new Thread[8]; // 一个按键对应一个线程。线程用于处理按键被按着的响应事件
    public static int timeDelay = 150; // 线程每次执行按键被按着的响应事件的间隔时间。= (21 - 灵敏度) * 10

    @Override
    public void keyPressed(KeyEvent e) {
        // 暂停了啥也不干
        if (!Gaming.getPause()) {
            if (!Gaming.mainGaming.getGameOver()) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        handlePressed(0, () -> Gaming.mainGaming.rotateCurrentBlock());
                        break;
                    case KeyEvent.VK_DOWN:
                        handlePressed(1, () -> Gaming.mainGaming.moveDown());
                        break;
                    case KeyEvent.VK_LEFT:
                        handlePressed(2, () -> Gaming.mainGaming.moveLeft());
                        break;
                    case KeyEvent.VK_RIGHT:
                        handlePressed(3, () -> Gaming.mainGaming.moveRight());
                        break;
                }
            }
            // 双人模式（secondaryGaming != null）才需要响应WASD四个按键的事件
            if (Gaming.secondaryGaming != null && !Gaming.secondaryGaming.getGameOver()) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        handlePressed(4, () -> Gaming.secondaryGaming.rotateCurrentBlock());
                        break;
                    case KeyEvent.VK_S:
                        handlePressed(5, () -> Gaming.secondaryGaming.moveDown());
                        break;
                    case KeyEvent.VK_A:
                        handlePressed(6, () -> Gaming.secondaryGaming.moveLeft());
                        break;
                    case KeyEvent.VK_D:
                        handlePressed(7, () -> Gaming.secondaryGaming.moveRight());
                        break;
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                handleReleased(0);
                break;
            case KeyEvent.VK_DOWN:
                handleReleased(1);
                break;
            case KeyEvent.VK_LEFT:
                handleReleased(2);
                break;
            case KeyEvent.VK_RIGHT:
                handleReleased(3);
                break;
            case KeyEvent.VK_W:
                handleReleased(4);
                break;
            case KeyEvent.VK_S:
                handleReleased(5);
                break;
            case KeyEvent.VK_A:
                handleReleased(6);
                break;
            case KeyEvent.VK_D:
                handleReleased(7);
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }


    // tag-第几个按键，runnable-该按键对应的动作
    private void handlePressed(int tag, Runnable runnable) {
        if (!keysState[tag]) {
            keysState[tag] = true;
            threads[tag] = new Thread(() -> {
                while (keysState[tag]) {
                    runnable.run(); // 执行按键对应的操作
                    try {
                        Thread.sleep(timeDelay);
                    } catch (InterruptedException e) {
                        break; // 被打断了就退出循环，结束线程
                    }
                }
            });
            threads[tag].start();
        }
    }

    // tag-第几个按键
    private void handleReleased(int tag) {
        keysState[tag] = false;
        if (threads[tag] != null && threads[tag].isAlive())
            threads[tag].interrupt(); // 若目标线程在运行则将其打断
    }

}