import java.awt.Color;

import javax.swing.JButton;

public class Player {
    String text;
    int turnCount;
    JButton tile1;
    JButton tile2;
    JButton tile3;

    Player(String text) {
        this.text = text;
        this.turnCount = 1;
    }

    String getText() {
        return text;
    }

    int getTurnCount() {
        return turnCount;
    }

    JButton getNextTile(boolean myTurn) {
        if (myTurn) {
            if (turnCount == 1 && tile1 != null) {
                tile1.setForeground(Color.red);
                return tile1;
            }
            if (turnCount == 2 && tile2 != null) {
                tile2.setForeground(Color.red);
                return tile1;
            }
            if (turnCount == 3 && tile3 != null) {
                tile3.setForeground(Color.red);
                return tile1;
            }
            else if (tile1 != null && tile2 != null && tile3 != null) {
                tile1.setForeground(Color.black);
                tile2.setForeground(Color.black);
                tile3.setForeground(Color.black);
            }
        }
        else {
            if (tile1 != null && tile2 != null && tile3 != null) {
                tile1.setForeground(Color.black);
                tile2.setForeground(Color.black);
                tile3.setForeground(Color.black);
            }
        }

        return null;
    }

    void incrementTurns() {
        turnCount++;
        if (turnCount > 3) {
            turnCount = 1;
        }
    }

    void setTile(JButton tile) {
        if (turnCount == 1) {
            if (tile1 != null) {
                tile1.setText("");
            }
            tile1 = tile;
        }
        else if (turnCount == 2) {
            if (tile2 != null) {
                tile2.setText("");
            }
            tile2 = tile;
        }
        else {
            if (tile3 != null) {
                tile3.setText("");
            }
            tile3 = tile;
        }
        incrementTurns();
    }

    void resetGame() {
        turnCount = 1;
        tile1 = null;
        tile2 = null;
        tile3 = null;
    }
}
