import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class InfiniteTicTacToe {
    int boardWidth = 600;
    int boardHeight = 700;

    JFrame frame = new JFrame("Infinite Tic Tac Toe");
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JButton newGameButton = new JButton("<html><font color = light gray>New Game</font></html>");

    JButton[][] board = new JButton[3][3];
    Player playerX = new Player("X");
    Player playerO = new Player("O");
    Player currentPlayer = playerX;

    boolean gameOver = false;

    InfiniteTicTacToe() {
        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        textLabel.setBackground(Color.darkGray);
        textLabel.setForeground(Color.white);
        textLabel.setFont(new Font("Arial", Font.BOLD, 35));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Infinite Tic Tac Toe");
        textLabel.setOpaque(true);

        textPanel.setLayout(new BorderLayout());
        textPanel.add(textLabel, BorderLayout.NORTH);

        newGameButton.setBorderPainted(false);
        newGameButton.setOpaque(true);
        newGameButton.setFont(new Font("Arial", Font.BOLD, 25));
        newGameButton.setFocusable(false);
        newGameButton.setBackground(Color.gray);
        if (!gameOver) {
            newGameButton.setEnabled(false);
        }
        newGameButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newGame();
            }
        });
        textPanel.add(newGameButton, BorderLayout.SOUTH);

        frame.add(textPanel, BorderLayout.NORTH);

        boardPanel.setLayout(new GridLayout(3, 3, 3, 3));
        boardPanel.setBackground(Color.darkGray);
        frame.add(boardPanel);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                JButton tile = new JButton();
                board[row][col] = tile;
                boardPanel.add(tile);

                tile.setBorderPainted(false);
                tile.setOpaque(true);
                tile.setBackground(Color.lightGray);
                tile.setForeground(Color.black);
                tile.setFont(new Font("Arial", Font.BOLD, 120));
                tile.setFocusable(false);

                tile.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (gameOver) return;
                        JButton tile = (JButton) e.getSource();
                        if (tile.getText() == "") {
                            tile.setText(currentPlayer.getText());
                            currentPlayer.setTile(tile);
                            checkWinner();
                            if (!gameOver) {
                                currentPlayer = currentPlayer == playerX ? playerO : playerX;
                                highlightNextTile();
                                textLabel.setText(currentPlayer.getText() + "'s turn.");
                            }
                        }
                    }
                });
            }
        }
    }

    void checkWinner() {
        //horizontal
        for (int row = 0; row < 3; row++) {
            if (board[row][0].getText() == "") continue;

            if (board[row][0].getText() == board[row][1].getText() &&
               board[row][1].getText() == board[row][2].getText()) {
                for (int i = 0; i < 3; i++) {
                    setWinner(board[row][i]);
                }
                gameOver = true;
                return;
               }
        }

        //vertical
        for (int col = 0; col < 3; col++) {
            if (board[0][col].getText() == "") continue;

            if (board[0][col].getText() == board[1][col].getText() &&
                board[1][col].getText() == board[2][col].getText()) {
                for (int i = 0; i < 3; i++) {
                    setWinner(board[i][col]);
                }
                gameOver = true;
                return;
                }
        }

        //diagonal left
        if (board[0][0].getText() == board[1][1].getText() &&
            board[1][1].getText() == board[2][2].getText() &&
            board[0][0].getText() != "") {
            for (int i = 0; i < 3; i++) {
                setWinner(board[i][i]);
            }
            gameOver = true;
            return;
        }
        
        //diagonal right
        if (board[0][2].getText() == board[1][1].getText() &&
            board[1][1].getText() == board[2][0].getText() &&
            board[0][2].getText() != "") {
            
            setWinner(board[0][2]);
            setWinner(board[1][1]);
            setWinner(board[2][0]);
            
            gameOver = true;
            return;
        }
    }

    void setWinner(JButton tile) {
        tile.setBackground(Color.green);
        tile.setForeground(Color.black);
        textLabel.setText((currentPlayer.getText() + " is the winner!"));
        newGameButton.setEnabled(true);
    }

    void highlightNextTile() {
        if (currentPlayer == playerX) {
            playerX.getNextTile(true);
            playerO.getNextTile(false);
        }
        else {
            playerO.getNextTile(true);
            playerX.getNextTile(false);
        }
    }

    void newGame() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                board[row][col].setText("");
                board[row][col].setBackground(Color.lightGray);
                board[row][col].setForeground(Color.black);
            }
        }
        newGameButton.setEnabled(false);
        playerX.resetGame();
        playerO.resetGame();
        currentPlayer = playerX;
        gameOver = false;
        textLabel.setText("Infinite Tic Tac Toe");
    }

    public static void main(String[] args) throws Exception {
        InfiniteTicTacToe ticTacToe = new InfiniteTicTacToe();
    }
}

/*  
    Update .jar file:
    jar uf InfiniteTicTacToe.jar src/InfiniteTicTacToe.java 
    
    Run .jar file:
    java -jar InfiniteTicTacToe.jar
*/