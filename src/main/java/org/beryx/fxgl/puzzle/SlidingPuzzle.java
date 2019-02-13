package org.beryx.fxgl.puzzle;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.EntityBuilder;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class SlidingPuzzle extends GameApplication {
    private static final int TILE_SIZE = 150;
    private static final int BOARD_X = 50;
    private static final int BOARD_Y = 50;
    private static final int BOARD_THICK = 10;

    private final Entity[] tiles = new Entity[16];
    private final int[] tileNumberAtPos = new int[16];
    private int holePosition;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(200 + 4 * TILE_SIZE);
        settings.setHeight(200 + 4 * TILE_SIZE);
        settings.setTitle("Sliding Puzzle");
        settings.setVersion("1.0");
    }

    @Override
    protected void initGame() {
        createBoard();
        for(int i=0; i<16; i++) {
            tiles[i] = createTile(i);
        }
        shuffle();
    }

    private Entity createBoard() {
        int boardSize = 2 * BOARD_THICK + 4 * TILE_SIZE;
        return new EntityBuilder()
                .at(BOARD_X, BOARD_Y)
                .view(new Rectangle(boardSize, boardSize, Color.LIGHTGRAY))
                .buildAndAttach();
    }


    private Entity createTile(int number) {
        Node node;
        if(number == 0) {
            node = FXGL.getAssetLoader().loadTexture("tile0.png");
        } else {
            var tile = FXGL.getAssetLoader().loadTexture("tile.png");
            Text numberText = FXGL.getUIFactory().newText("" + number, Color.WHITE, 64);
            numberText.setTranslateX((TILE_SIZE - 40 - 40 * (number / 10)) / 2);
            numberText.setTranslateY((TILE_SIZE + 50) / 2);
            node = new Group(tile, numberText);
        }
        return new EntityBuilder()
                .view(node)
                .buildAndAttach();
    }

    @Override
    protected void initInput() {
        Input input = FXGL.getInput();

        input.addAction(new UserAction("Move Right") {
            @Override
            protected void onActionBegin() {
                moveRight();
            }
        }, KeyCode.RIGHT);

        input.addAction(new UserAction("Move Left") {
            @Override
            protected void onActionBegin() {
                moveLeft();
            }
        }, KeyCode.LEFT);

        input.addAction(new UserAction("Move Up") {
            @Override
            protected void onActionBegin() {
                moveUp();
            }
        }, KeyCode.UP);

        input.addAction(new UserAction("Move Down") {
            @Override
            protected void onActionBegin() {
                moveDown();
            }
        }, KeyCode.DOWN);
    }

    private void reset() {
        for(int i=0; i<16; i++) {
            setTilePosition(i, (i + 15) % 16);
        }
        holePosition = 15;
    }

    private void shuffle() {
        reset();
        int moves = FXGL.random(1800, 2000);
        for(int i=0; i<moves; i++) {
            int k = FXGL.random(0, 3);
            switch (k) {
                case 0: moveUp(); break;
                case 1: moveDown(); break;
                case 2: moveLeft(); break;
                case 3: moveRight(); break;
            }
        }
    }

    private boolean moveUp() {
        if(holePosition / 4 == 3) return false;
        return swap(holePosition + 4);
    }

    private boolean moveDown() {
        if(holePosition / 4 == 0) return false;
        return swap(holePosition - 4);
    }

    private boolean moveLeft() {
        if(holePosition % 4 == 3) return false;
        return swap(holePosition + 1);
    }

    private boolean moveRight() {
        if(holePosition % 4 == 0) return false;
        return swap(holePosition - 1);
    }

    private boolean swap(int pos) {
        setTilePosition(tileNumberAtPos[pos], holePosition);
        setTilePosition(0, pos);
        holePosition = pos;
        return true;
    }

    private void setTilePosition(int tileNumber, int pos) {
        tileNumberAtPos[pos] = tileNumber;
        tiles[tileNumber].setPosition(BOARD_X + BOARD_THICK + TILE_SIZE * (pos % 4), BOARD_Y + BOARD_THICK + TILE_SIZE * (pos / 4));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
