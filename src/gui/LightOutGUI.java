package gui;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.LightsOutModel;
import model.Observer;

import java.io.File;

/**
 * A GUI interface for Lights Out
 */
public class LightOutGUI extends Application implements Observer<LightsOutModel, String> {
    /** The model for Lights Out **/
    private LightsOutModel model;
    /** The text-field of the GUI on top of the stage**/
    TextField textField;
    /** The grid pane of the GUI at the center of the stage **/
    GridPane gridPane;

    /**
     * Creates the GUI for the puzzle with all the buttons being set
     * to a specific action. The stage's title is named Lights Out and
     * a BorderPane is created. The top of the borderPane is a textField
     * which displays the amount of moves and specific messages. The center
     * of the borderPane is a 5 x 5 grid of buttons that is used for the puzzle.
     * The button of the borderPane is a flowPane with more buttons that
     * allow the user to create a new game, load a game, or get a hint.
     *
     * @param stage the primary stage for this application, onto which
     * the application scene can be set.
     * Applications may create other stages, if needed, but they will not be
     * primary stages.
     * @throws Exception
     */
    @Override
    public void start(Stage stage) throws Exception {
        stage.show();
        stage.setTitle("Lights Out");

        BorderPane borderPane = new BorderPane();

        textField = new TextField("Moves: 0 Message: Start a game first.");
        textField.setEditable(false);
        borderPane.setTop(textField);

        gridPane = new GridPane();
        gridPane.setMaxHeight(500);
        gridPane.setMaxWidth(500);
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                Button button = new Button();
                button.setStyle("-fx-border-color: GRAY; -fx-border-width: 3px;");
                button.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
                button.setMinHeight(100);
                button.setMinWidth(100);
                gridPane.add(button, row, col);
                int finalRow = row;
                int finalCol = col;
                button.setOnAction(event -> {
                    model.toggleTile(finalRow, finalCol);
                });
            }
        }
        borderPane.setCenter(gridPane);

        GridPane gridPaneBot = new GridPane();
        Button newGame = new Button("New Game");
        newGame.setMinWidth(75);
        gridPaneBot.add(newGame, 0, 0);
        Button loadGame = new Button("Load Game");
        loadGame.setMinWidth(75);
        gridPaneBot.add(loadGame, 1, 0);
        Button hint = new Button("Hint");
        hint.setMinWidth(75);
        gridPaneBot.add(hint, 2, 0);
        FlowPane flowPane = new FlowPane(gridPaneBot);
        flowPane.setAlignment(Pos.CENTER);
        borderPane.setBottom(flowPane);

        stage.setScene(new Scene(borderPane));

        newGame.setOnAction((event -> {
            model.generateRandomBoard();
        }));

        loadGame.setOnAction((event -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Load a game board.");
                File selectedFile = fileChooser.showOpenDialog(stage);
                fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")+"/boards"));
                fileChooser.getExtensionFilters().addAll( new FileChooser.ExtensionFilter("Text Files", "*.lob"));
                model.loadBoardFromFile(selectedFile);
            }
            catch (NullPointerException e) {
                textField.setText("Moves: " + model.getMoves() + " Message: Select a game to load.");
            }
        }));

        hint.setOnAction((event -> {
            try {
                model.getHint();
            }
            catch (IndexOutOfBoundsException e) {
                textField.setText("Moves: " + model.getMoves() + " Message: Start a new game.");
            }
        }));
    }

    /**
     * Uses two for loops to search through each button on the grid.
     * It sees whether it's supposed to be BLACK or WHITE by using the isOn
     * method on the specific tile of the model. It will set the button to
     * the background color accordingly based on the returned value.
     */
    public void displayBoard() {
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                Button button = (Button) gridPane.getChildren().get(row * 5 + col);
                button.setStyle("-fx-border-color: GRAY; -fx-border-width: 3px;");
                if (!model.getTile(row, col).isOn()) {
                    button.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
                } else {
                    button.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
                }
            }
        }
    }

    /**
     * Initializes the model and adds this as an observer.
     *
     * @throws Exception
     */
    @Override
    public void init() throws Exception {
        System.out.println("init: Initialize and connect to model!");
        this.model = new LightsOutModel();
        this.model.addObserver(this);
    }

    /**
     * Updates the message in the textField with the correct number of moves
     * and a specific message. It also displays the updated board. The hint
     * button will update the button so the border is highlighted yellow.
     *
     * @param model the object that wishes to inform this object
     *                about something that has happened.
     * @param msg optional data the server.model can send to the observer
     *
     */
    @Override
    public void update(LightsOutModel model, String msg) {
        if (msg.equals(LightsOutModel.LOADED)){ // game is loaded successfully
            textField.setText("Moves: " + model.getMoves() + " Message: Game Loaded");
            displayBoard();
            return;
        } else if (msg.equals(LightsOutModel.LOAD_FAILED)){ //Game failed to load
            textField.setText("Moves: " + model.getMoves() + " Message: Error Loading Game");
            return;
        } else if (msg.startsWith(LightsOutModel.HINT_PREFIX)) { //Model is reporting a  hint
            textField.setText("Moves: " + model.getMoves() + " Message: " + msg);
            String[] ints = msg.substring(6).split(", ");
            int index = Integer.parseInt(ints[0]) * 5 + Integer.parseInt(ints[1]);
            Button button = (Button) gridPane.getChildren().get(index);
            button.setStyle("-fx-border-color: YELLOW; -fx-border-width: 6px;");;
            return;
        }

        if (model.gameOver()) {
            displayBoard();
            textField.setText("Moves: " + model.getMoves() + " Message: You win. Good for you.");
            return;
        }
        displayBoard(); // renders the board
        textField.setText("Moves: " + model.getMoves() + " Message: Move: " + msg);
    }

    /**
     * Launches the application.
     *
     * @param args argument commands
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
}
