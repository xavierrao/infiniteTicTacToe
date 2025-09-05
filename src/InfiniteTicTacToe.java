import java.awt.*;
import javax.swing.*;
import java.net.*;
import java.io.*;

public class InfiniteTicTacToe {
    int boardWidth = 600;
    int boardHeight = 700;

    JFrame frame = new JFrame("Infinite Tic Tac Toe");
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JButton newGameButton = new JButton("New Game");

    JButton[][] board = new JButton[3][3];
    Player playerX = new Player("X");
    Player playerO = new Player("O");
    Player currentPlayer;
    boolean isHost;
    boolean myTurn;
    boolean gameOver = false;

    ServerSocket serverSocket;
    Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;

    InfiniteTicTacToe(boolean isHost, String hostIP, int port) {
        this.isHost = isHost;
        this.currentPlayer = isHost ? playerX : playerO; // Host is X, client is O
        this.myTurn = isHost; // Host starts as X

        // Frame setup
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
        textLabel.setText(isHost ? "Waiting for opponent..." : "Connecting to host...");
        textLabel.setOpaque(true);

        textPanel.setLayout(new BorderLayout());
        textPanel.add(textLabel, BorderLayout.NORTH);

        newGameButton.setBorderPainted(false);
        newGameButton.setOpaque(true);
        newGameButton.setFont(new Font("Arial", Font.BOLD, 25));
        newGameButton.setFocusable(false);
        newGameButton.setBackground(Color.gray);
        newGameButton.setEnabled(false);
        newGameButton.addActionListener(e -> newGame());
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

                final int r = row, c = col;
                tile.addActionListener(e -> handleTileClick(r, c));
            }
        }

        // Initialize networking in a separate thread
        new Thread(() -> setupNetworking(isHost, hostIP, port)).start();
    }

    void setupNetworking(boolean isHost, String hostIP, int port) {
        try {
            if (isHost) {
                serverSocket = new ServerSocket(port);
                SwingUtilities.invokeLater(() -> textLabel.setText("Waiting for opponent..."));
                socket = serverSocket.accept();
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                SwingUtilities.invokeLater(() -> textLabel.setText("Opponent connected! X's turn."));
            } else {
                socket = new Socket(hostIP, port);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                SwingUtilities.invokeLater(() -> textLabel.setText("Connected to host! Waiting for X's turn..."));
            }
            new Thread(this::listenForOpponentMoves).start();
        } catch (IOException e) {
            e.printStackTrace();
            cleanupAndShowOptionDialog("Connection error: " + e.getMessage());
        }
    }

    void handleTileClick(int row, int col) {
        if (gameOver || !myTurn || board[row][col].getText() != "") {
            return;
        }
        if (!isHost && !currentPlayer.getText().equals("O")) {
            System.err.println("Client: Incorrect currentPlayer, resetting to O");
            currentPlayer = playerO;
        } else if (isHost && !currentPlayer.getText().equals("X")) {
            System.err.println("Host: Incorrect currentPlayer, resetting to X");
            currentPlayer = playerX;
        }
        JButton tile = board[row][col];
        tile.setText(currentPlayer.getText());
        currentPlayer.setTile(tile);
        myTurn = false;
        textLabel.setText("Waiting for opponent's move...");
        try {
            out.writeObject(new Move(row, col, currentPlayer.getText()));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            cleanupAndShowOptionDialog("Error sending move: " + e.getMessage());
            return;
        }
        checkWinner();
        if (!gameOver) {
            currentPlayer = (currentPlayer == playerX) ? playerO : playerX;
            highlightNextTile();
        }
    }

    void listenForOpponentMoves() {
        try {
            while (true) {
                Move move = (Move) in.readObject();
                if (move.player.equals("NEW_GAME")) {
                    System.out.println("Received NEW_GAME signal");
                    SwingUtilities.invokeLater(this::resetGame);
                    continue;
                }
                if (gameOver) {
                    continue;
                }
                String expectedOpponentSymbol = isHost ? "O" : "X";
                if (!move.player.equals(expectedOpponentSymbol)) {
                    System.err.println("Received invalid player symbol: " + move.player);
                    continue;
                }
                SwingUtilities.invokeLater(() -> {
                    board[move.row][move.col].setText(move.player);
                    (move.player.equals("X") ? playerX : playerO).setTile(board[move.row][move.col]);
                    checkWinner();
                    if (!gameOver) {
                        currentPlayer = isHost ? playerX : playerO;
                        myTurn = true;
                        textLabel.setText(currentPlayer.getText() + "'s turn.");
                        highlightNextTile();
                    }
                });
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            cleanupAndShowOptionDialog("Connection lost: " + e.getMessage());
        }
    }

    void checkWinner() {
        // Horizontal
        for (int row = 0; row < 3; row++) {
            if (board[row][0].getText().isEmpty())
                continue;
            if (board[row][0].getText().equals(board[row][1].getText()) &&
                    board[row][1].getText().equals(board[row][2].getText())) {
                for (int i = 0; i < 3; i++)
                    setWinner(board[row][i]);
                gameOver = true;
                return;
            }
        }

        // Vertical
        for (int col = 0; col < 3; col++) {
            if (board[0][col].getText().isEmpty())
                continue;
            if (board[0][col].getText().equals(board[1][col].getText()) &&
                    board[1][col].getText().equals(board[2][col].getText())) {
                for (int i = 0; i < 3; i++)
                    setWinner(board[i][col]);
                gameOver = true;
                return;
            }
        }

        // Diagonal left
        if (!board[0][0].getText().isEmpty() &&
                board[0][0].getText().equals(board[1][1].getText()) &&
                board[1][1].getText().equals(board[2][2].getText())) {
            for (int i = 0; i < 3; i++)
                setWinner(board[i][i]);
            gameOver = true;
            return;
        }

        // Diagonal right
        if (!board[0][2].getText().isEmpty() &&
                board[0][2].getText().equals(board[1][1].getText()) &&
                board[1][1].getText().equals(board[2][0].getText())) {
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
        textLabel.setText(currentPlayer.getText() + " is the winner!");
        newGameButton.setEnabled(true);
    }

    void highlightNextTile() {
        if (currentPlayer == playerX) {
            playerX.getNextTile(myTurn && currentPlayer == playerX);
            playerO.getNextTile(myTurn && currentPlayer == playerO);
        } else {
            playerO.getNextTile(myTurn && currentPlayer == playerO);
            playerX.getNextTile(myTurn && currentPlayer == playerX);
        }
    }

    void newGame() {
        try {
            out.writeObject(new Move(-1, -1, "NEW_GAME"));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        resetGame();
    }

    void resetGame() {
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
        currentPlayer = isHost ? playerX : playerO;
        myTurn = isHost;
        gameOver = false;
        textLabel.setText(isHost ? "X's turn." : "Waiting for X's turn...");
        highlightNextTile();
        boardPanel.revalidate(); // Ensure UI updates
        boardPanel.repaint();
        frame.revalidate();
        frame.repaint();
    }

    void cleanupAndShowOptionDialog(String message) {
        SwingUtilities.invokeLater(() -> {
            // Update UI to show disconnection message
            textLabel.setText(message);
            // Clean up networking resources
            try {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
                if (socket != null)
                    socket.close();
                if (isHost && serverSocket != null)
                    serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Close the current game window
            frame.dispose();
            // Show the option dialog
            showOptionDialog();
        });
    }

    void showOptionDialog() {
        String[] options = { "Host Game", "Join Game" };
        int choice = JOptionPane.showOptionDialog(null, "Host or Join Game?", "Game Mode",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            // Run as host
            new InfiniteTicTacToe(true, null, 12345);
        } else if (choice == 1) {
            // Run as client
            String hostIP = JOptionPane.showInputDialog("Enter host IP address:");
            new InfiniteTicTacToe(false, hostIP, 12345);
        }
    }

    public static void main(String[] args) {
        // Prompt user to choose host or join
        String[] options = { "Host Game", "Join Game" };
        int choice = JOptionPane.showOptionDialog(null, "Host or Join Game?", "Game Mode",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            // Run as host
            new InfiniteTicTacToe(true, null, 12345);
        } else {
            // Run as client
            String hostIP = JOptionPane.showInputDialog("Enter host IP address:");
            new InfiniteTicTacToe(false, hostIP, 12345);
        }
    }
}

// Class to represent a move
class Move implements Serializable {
    int row, col;
    String player;

    Move(int row, int col, String player) {
        this.row = row;
        this.col = col;
        this.player = player;
    }
}