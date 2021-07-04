package tictactoe;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.*;

public class GameClient extends JFrame {

    private final String[][] board = {{"1", "2", "3"}, {"4", "5", "6"}, {"7", "8", "9"}};
    private final String[][] WaysToWin = {{"1", "2", "3"}, {"4", "5", "6"}, {"7", "8", "9"}, {"1", "4", "7"}, {"2", "5", "8"}, {"3", "6", "9"}, {"1", "5", "9"}, {"3", "5", "7"}};
    private final JLabel[][] cells = new JLabel[3][3];
    private static final int PORT = 8189;
    private Connection connection;
    private String symToTurn;
    private int tie = 9;
    private boolean turn = false;
    private final ArrayList<String> prValue = new ArrayList<>(Collections.singletonList(" "));

    private GameClient() {
        super("Tic-tac-toe");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setVisible(true);
        createGameUITable();
        try {
            String IP = InetAddress.getLocalHost().getHostAddress();
            connection = new Connection(this, IP, PORT);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(GameClient.this, "Connection Failed");
        }
    }

    public void createGameUITable() {
        setSize(400, 400);
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(3, 3));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final int number = i * 3 + j + 1;
                JLabel p = new JLabel();
                cells[i][j] = p;
                p.setHorizontalAlignment(SwingConstants.CENTER);
                p.setVerticalAlignment(SwingConstants.CENTER);
                p.setFont(new Font(Font.SERIF, Font.PLAIN, 35));
                p.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                northPanel.add(p);
                add(northPanel, BorderLayout.CENTER);
                p.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (symToTurn == null) {
                            JOptionPane.showMessageDialog(GameClient.this, "Connection to server");
                        } else {
                            if (turn) {
                                makeTurn(number, symToTurn);
                            } else {
                                JOptionPane.showMessageDialog(GameClient.this, "Waiting player 2");
                            }
                        }
                    }
                });
            }
        }
        printGameTable();
    }

    public void makeTurn(int number, String ch) {
        int i = (number - 1) / 3;
        int j = number - i * 3 - 1;
        if (!board[i][j].equals("X") && !board[i][j].equals("O")) {
            board[i][j] = ch;
            String turnInput = String.valueOf(number);
            if (!prValue.contains((symToTurn + turnInput)))
                prValue.add(symToTurn + turnInput);
            turn = false;
            printGameTable();
            connection.sendString(turnInput);
            checkWinner(turnInput, symToTurn);
        } else {
            JOptionPane.showMessageDialog(GameClient.this, "Slot already taken; re-enter slot number");
        }

    }

    private void checkWinner(String num, String sym) {
        tie--;
        for (int i = 0; i < WaysToWin.length; i++) {
            for (int j = 0; j < 3; j++) {
                if (WaysToWin[i][j].equals(num)) {
                    WaysToWin[i][j] = sym;
                }
            }
        }
        String[] arr1 = {"X", "X", "X"}, arr2 = {"O", "O", "O"};
        for (String[] s : WaysToWin) {
            if (Arrays.equals(s, arr1)) {
                JOptionPane.showMessageDialog(GameClient.this, "X Won");
                System.exit(0);
            }
            if (Arrays.equals(s, arr2)) {
                JOptionPane.showMessageDialog(GameClient.this, "O Won");
                System.exit(0);
            }
        }
        if (tie == 0) {
            JOptionPane.showMessageDialog(GameClient.this, "It's Tie");
            System.exit(0);
        }
    }

    public void printGameTable() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j].equals("O") || board[i][j].equals("X")) {
                    cells[i][j].setText(String.valueOf(board[i][j]));
                }
            }
        }
    }

    private void setLabel(String conn) {
        JLabel jLabel = new JLabel();
        jLabel.setFont(new Font("Verdana", Font.PLAIN, 20));
        jLabel.setText("<html>You symbol to turn: " + symToTurn + "<br>You connection on server: " + conn + "</html>");
        add(jLabel, BorderLayout.NORTH);
        setSize(400, 450);
    }


    public void onReceive(String value) {
        try {
            if ("O".equals(String.valueOf(value.charAt(1)))) {
                symToTurn = "O";
                setLabel(String.valueOf(value.charAt(2)));
                turn = true;
            }
            if ("X".equals(String.valueOf(value.charAt(1)))) {
                symToTurn = "X";
                setLabel(String.valueOf(value.charAt(2)));
            } else {
                if (!prValue.contains(value)) {
                    prValue.add(value);
                    int num = Integer.parseInt(String.valueOf(value.charAt(1)));
                    int i = (num - 1) / 3;
                    int j = num - i * 3 - 1;
                    board[i][j] = String.valueOf(value.charAt(0));
                    printGameTable();
                    checkWinner(String.valueOf(num), String.valueOf(value.charAt(0)));
                    turn = true;
                }
            }

        } catch (Exception ignored) {
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameClient::new);
        SwingUtilities.invokeLater(GameClient::new);
    }
}