package org.beryx.fxgl.puzzle;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.EntityBuilder;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.IrremovableComponent;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.texture.Texture;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.Map;

public class SlidingPuzzle extends GameApplication {
    private static final int TILE_SIZE = 150;
    private static final int BOARD_X = 50;
    private static final int BOARD_Y = 50;
    private static final int BOARD_THICK = 10;
    private static final int BOARD_SIZE = 2 * BOARD_THICK + 4 * TILE_SIZE;
    private static final int TOTAL_WIDTH = 2 * BOARD_X + BOARD_SIZE + 160;
    private static final int TOTAL_HEIGHT = 2 * BOARD_Y + BOARD_SIZE;

    private final Entity[] tiles = new Entity[16];
    private final int[] tileNumberAtPos = new int[16];
    private int holePosition;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(TOTAL_WIDTH);
        settings.setHeight(TOTAL_HEIGHT);
        settings.setTitle(System.getProperty("fxgl.sliding.puzzle.title", "Sliding Puzzle"));
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
        return new EntityBuilder()
                .at(BOARD_X, BOARD_Y)
                .view(new Rectangle(BOARD_SIZE, BOARD_SIZE, Color.LIGHTGRAY))
                .buildAndAttach();
    }


    private Entity createTile(int number) {
        Node node;
        if(number == 0) {
            node = FXGL.getAssetLoader().loadTexture("tile0.png");
        } else {
            var tile = FXGL.getAssetLoader().loadTexture("tile.png");
            Text numberText = FXGL.getUIFactoryService().newText("" + number, Color.DARKGREEN, 64);
            numberText.setTranslateX((TILE_SIZE - 40 - 40 * (number / 10)) / 2);
            numberText.setTranslateY((TILE_SIZE + 50) / 2);
            node = new Group(tile, numberText);
            node.setOnMouseClicked(ev -> {
                if((holePosition % 4 != 0) && (tileNumberAtPos[holePosition - 1] == number)) moveRight();
                else if((holePosition % 4 != 3) && (tileNumberAtPos[holePosition + 1] == number)) moveLeft();
                else if((holePosition / 4 != 0) && (tileNumberAtPos[holePosition - 4] == number)) moveDown();
                else if((holePosition / 4 != 3) && (tileNumberAtPos[holePosition + 4] == number)) moveUp();
            });
        }

        return new EntityBuilder()
                .view(node)
                .buildAndAttach();
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("moves", 0);

        Rectangle bg = new Rectangle(TOTAL_WIDTH, TOTAL_HEIGHT,
                new LinearGradient(TOTAL_WIDTH / 2, 0, TOTAL_HEIGHT / 2, TOTAL_HEIGHT,
                        false, CycleMethod.NO_CYCLE,
                        new Stop(0.2, Color.STEELBLUE), new Stop(0.8, Color.ROYALBLUE)));
        new EntityBuilder()
                .view(bg)
                .with(new IrremovableComponent())
                .buildAndAttach();

    }

    @Override
    protected void initUI() {
        Text movesText = createMovesText();
        Texture shuffleButton = createShuffleButton();
        FXGL.getGameScene().addUINodes(movesText, shuffleButton);
    }

    private Text createMovesText() {
        Text movesText = FXGL.getUIFactoryService().newText("", Color.WHITE, 24);
        movesText.setTranslateX(BOARD_X + BOARD_SIZE + 40);
        movesText.setTranslateY(BOARD_Y + 120);
        movesText.textProperty().bind(
                new ReadOnlyStringWrapper("Moves: ").concat(
                    FXGL.getWorldProperties().intProperty("moves").asString()
                ));
        return movesText;
    }

    private Texture createShuffleButton() {
        var shuffleButtonReleased = FXGL.getAssetLoader().loadTexture("shuffle1.png");
        var shuffleButtonPressed = FXGL.getAssetLoader().loadTexture("shuffle2.png");
        var shuffleButton = shuffleButtonReleased.copy();
        shuffleButton.setTranslateX(BOARD_X + BOARD_SIZE + 20);
        shuffleButton.setTranslateY(BOARD_Y + 230);
        shuffleButton.setOnMouseClicked(ev -> shuffle());
        shuffleButton.setOnMouseEntered(ev -> shuffleButton.set(shuffleButtonPressed));
        shuffleButton.setOnMouseExited(ev -> shuffleButton.set(shuffleButtonReleased));
        return shuffleButton;
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

    private boolean shuffling;
    private void shuffle() {
        shuffling = true;
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
        FXGL.getWorldProperties().setValue("moves", 0);
        shuffling = false;
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
        FXGL.getWorldProperties().increment("moves", +1);
        if(!shuffling && isSolved()) {
            FXGL.getDialogService().showConfirmationBox("Congratulations!\nPlay again?", yes -> {
                if (yes) {
                    shuffle();
                }  else {
                    Platform.exit();
                    System.exit(0);
                }
            });
        }
        return true;
    }

    private void setTilePosition(int tileNumber, int pos) {
        tileNumberAtPos[pos] = tileNumber;
        tiles[tileNumber].setPosition(BOARD_X + BOARD_THICK + TILE_SIZE * (pos % 4), BOARD_Y + BOARD_THICK + TILE_SIZE * (pos / 4));
    }

    private boolean isSolved() {
        for(int i=0; i<16; i++) {
            if(tileNumberAtPos[(i + 15) % 16] != i) return false;
        }
        return true;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
