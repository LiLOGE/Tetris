package pers.le.tetris;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OnClickListener implements ActionListener {

    private int id; // 菜单项的ID，用以区分是哪个菜单项上的点击事件
    private View view;

    public OnClickListener(View view, int id) {
        super();
        this.id = id;
        this.view = view;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 以ID区分不同的菜单项，实现不同的功能
        switch (id) {
            case ID.New_Game_Item:
                // 初始化游戏资源，重新开始游戏
                Gaming.mainGaming.init();
                if (Gaming.secondaryGaming != null)
                    Gaming.secondaryGaming.init();
                // 取消暂停
                view.setPause(false);
                break;
            case ID.Pause_Item:
                view.setPause(!Gaming.getPause());
                break;
            case ID.Speed_Item:
                view.setSpeed();
                break;
            case ID.Key_Sensitivity_Item:
                view.setKeySensitivity();
                break;
            case ID.Exit_Item:
                // 弹出是否确认退出的对话框
                if (JOptionPane.showConfirmDialog(view, "确定要退出吗？", "退出", 0) == JOptionPane.YES_OPTION)
                    System.exit(0);
                break;
            case ID.Single_Mode_Item:
                view.closeSecondaryView();
                break;
            case ID.Double_Mode_Item:
                view.newSecondaryView();
                break;
        }
    }

}