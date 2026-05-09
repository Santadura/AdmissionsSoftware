package com.tuyensinh;

import javax.swing.SwingUtilities;
import com.tuyensinh.ui.login.LoginFrame;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}