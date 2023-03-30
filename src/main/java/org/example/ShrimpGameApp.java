package org.example;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.controllers.CreateGameScreenController;
import org.example.controllers.GameController;
import org.example.controllers.MainMenuScreenController;
import org.example.logic.Lobby;
import org.example.network.ServerConnection;

/**
 * The ShrimpGameApp class is the entry point of the application and is responsible for launching
 * the JavaFX application and
 * setting up the UI. It contains fields and methods for initializing all the screens and
 * components of the game UI, as well
 * as for accessing the primary stage, server connection, and game controller.
 */
public class ShrimpGameApp extends Application
{
    private Stage primaryStage;
    private Scene mainScene;
    private Scene mainAdminScene;
    private Scene createGameScene;
    private Scene joinGameTableScene;
    private boolean isAdmin;
    private ServerConnection serverConnection;
    private GameController gameController;
    private MainMenuScreenController mainMenuScreenController;
    private CreateGameScreenController createGameScreenController;
    private ScheduledExecutorService executor;
    private TableView<Lobby> lobbyTableView;

    @Override
    public void start(Stage stage) throws Exception
    {
        this.primaryStage = stage;
        this.lobbyTableView = new TableView<Lobby>();
        String username;
        try
        {
            String[] input = this.initServerConnection();
            username = input[0];
            this.isAdmin = Boolean.parseBoolean(input[1]);
        }
        catch (RuntimeException exception)
        {
            username = "Player";
            this.isAdmin = false;
        }
        MainMenuScreenController controller = new MainMenuScreenController(this,
                                                                           this.serverConnection,
                                                                           username);
        this.setMainScene(controller);
        this.setMainAdminScene(controller);
        this.setCreateGameScene(new CreateGameScreenController(this, this.serverConnection));
        this.setJoinGameTableScene();
        this.setScene(this.getMainScene());
    }

    /**
     * The main method launches the JavaFX application.
     *
     * @param args the commandline arguments.
     */
    public static void main(String[] args)
    {
        launch(args);
    }

    public void addIconToDialog(Dialog dialog)
    {
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(
            new Image(this.getClass().getResource("/images/shrimp_logo.png").toExternalForm()));
    }

    public boolean getIsAdmin()
    {
        return this.isAdmin;
    }

    public void setIsAdmin(boolean isAdmin)
    {
        this.isAdmin = isAdmin;
    }

    public void setScene(Scene scene)
    {
        this.primaryStage.hide();
        Image icon = new Image(
            this.getClass().getResource("/images/shrimp_logo.png").toExternalForm());
        this.primaryStage.getIcons().add(icon);
        this.primaryStage.setScene(scene);
        this.primaryStage.setTitle("Shrimp Game");
        this.primaryStage.setMinHeight(600);
        this.primaryStage.setMinWidth(700);
        this.primaryStage.show();
    }

    private void setMainScene(MainMenuScreenController controller)
    {
        VBox root = new VBox();
        root.setSpacing(20);
        root.setPadding(new Insets(150, 0, 0, 0));
        Scene mainMenu = new Scene(root, 800, 600);
        mainMenu.getStylesheets().add(
            this.getClass().getResource("/css/styles.css").toExternalForm());
        Font helvetica = Font.loadFont("file:/fonts/Helvetica.ttf", 24);

        Label welcomeLbl = new Label("Welcome to Shrimp Game " + controller.getUsername());

        Button joinGameBtn = new Button("JOIN GAME");
        joinGameBtn.setOnAction(event ->
                                {
                                    this.executor = Executors.newSingleThreadScheduledExecutor();
                                    executor.scheduleAtFixedRate(() ->
                                                                 {
                                                                     List<Lobby> lobbies =
                                                                         serverConnection.getLobbies();
                                                                     Platform.runLater(() ->
                                                                                       {
                                                                                           ObservableList<Lobby>
                                                                                               observableLobbies =
                                                                                               FXCollections.observableArrayList(
                                                                                                   lobbies);
                                                                                           lobbyTableView.setItems(
                                                                                               observableLobbies);
                                                                                       });
                                                                 }, 0, 5, TimeUnit.SECONDS);
                                    this.setScene(this.joinGameTableScene);
                                });
        joinGameBtn.setPrefWidth(320);
        joinGameBtn.setPrefHeight(80);

        Button becomeAdminBtn = new Button("BECOME ADMIN");
        becomeAdminBtn.setOnAction(event -> controller.handleBecomeAdminButton());
        becomeAdminBtn.setPrefWidth(320);
        becomeAdminBtn.setPrefHeight(80);

        Button quitBtn = new Button("QUIT");
        quitBtn.setOnAction(event -> Platform.exit());
        quitBtn.setPrefWidth(320);
        quitBtn.setPrefHeight(80);

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        VBox.setVgrow(spacer1, Priority.ALWAYS);
        VBox.setVgrow(spacer2, Priority.ALWAYS);


        root.getChildren().addAll(spacer1, welcomeLbl, joinGameBtn, becomeAdminBtn, quitBtn,
                                  spacer2);
        root.setAlignment(Pos.CENTER);

        Image backgroundImage = new Image(
            this.getClass().getResource("/images/main_menu.jpg").toExternalForm());
        BackgroundSize backgroundSize = new BackgroundSize(1.0, 1.0, true, true, false, false);
        BackgroundImage background = new BackgroundImage(backgroundImage,
                                                         BackgroundRepeat.NO_REPEAT,
                                                         BackgroundRepeat.NO_REPEAT,
                                                         BackgroundPosition.CENTER, backgroundSize);
        root.setBackground(new Background(background));
        this.mainScene = mainMenu;
    }

    public Scene getMainScene()
    {
        Scene mainScene = null;
        if (this.isAdmin)
        {
            mainScene = this.mainAdminScene;
        }
        else
        {
            mainScene = this.mainScene;
        }
        return mainScene;
    }

    public void setMainAdminScene(MainMenuScreenController controller)
    {
        VBox root = new VBox();
        root.setSpacing(20);
        root.setPadding(new Insets(150, 0, 0, 0));
        Scene mainAdminScene = new Scene(root, 800, 600);
        mainAdminScene.getStylesheets().add(
            this.getClass().getResource("/css/styles.css").toExternalForm());
        Font helvetica = Font.loadFont("file:/fonts/Helvetica.ttf", 24);

        Label welcomeLbl = new Label("What would you like to do " + controller.getUsername() + "?");

        Button createGameBtn = new Button("CREATE GAME");
        createGameBtn.setOnAction(event -> this.setScene(this.createGameScene));
        createGameBtn.setPrefWidth(320);
        createGameBtn.setPrefHeight(80);

        Button joinGameBtn = new Button("JOIN GAME");
        joinGameBtn.setOnAction(event ->
                                {
                                    this.executor = Executors.newSingleThreadScheduledExecutor();
                                    executor.scheduleAtFixedRate(() ->
                                                                 {
                                                                     List<Lobby> lobbies =
                                                                         serverConnection.getLobbies();
                                                                     Platform.runLater(() ->
                                                                                       {
                                                                                           ObservableList<Lobby>
                                                                                               observableLobbies =
                                                                                               FXCollections.observableArrayList(
                                                                                                   lobbies);
                                                                                           lobbyTableView.setItems(
                                                                                               observableLobbies);
                                                                                       });
                                                                 }, 0, 5, TimeUnit.SECONDS);
                                    this.setScene(this.joinGameTableScene);
                                });
        joinGameBtn.setPrefWidth(320);
        joinGameBtn.setPrefHeight(80);

        Button quitBtn = new Button("QUIT");
        quitBtn.setOnAction(event -> Platform.exit());
        quitBtn.setPrefWidth(320);
        quitBtn.setPrefHeight(80);

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        VBox.setVgrow(spacer1, Priority.ALWAYS);
        VBox.setVgrow(spacer2, Priority.ALWAYS);


        root.getChildren().addAll(spacer1, welcomeLbl, createGameBtn, joinGameBtn, quitBtn,
                                  spacer2);
        root.setAlignment(Pos.CENTER);

        Image backgroundImage = new Image(
            this.getClass().getResource("/images/main_menu.jpg").toExternalForm());
        BackgroundSize backgroundSize = new BackgroundSize(1.0, 1.0, true, true, false, false);
        BackgroundImage background = new BackgroundImage(backgroundImage,
                                                         BackgroundRepeat.NO_REPEAT,
                                                         BackgroundRepeat.NO_REPEAT,
                                                         BackgroundPosition.CENTER, backgroundSize);
        root.setBackground(new Background(background));
        this.mainAdminScene = mainAdminScene;
    }

    public void setCreateGameScene(CreateGameScreenController controller)
    {
        VBox root = new VBox();
        root.setSpacing(20);
        root.setPadding(new Insets(50, 0, 50, 0));
        Scene createGameScene = new Scene(root, 800, 600);
        createGameScene.getStylesheets().add(
            this.getClass().getResource("/css/styles.css").toExternalForm());
        Font helvetica = Font.loadFont("file:/fonts/Helvetica.ttf", 24);

        Label headingLbl = new Label("Create New Game");
        headingLbl.setFont(helvetica);
        headingLbl.getStyleClass().add("heading");

        TextField gameLobbyNameField = new TextField();
        gameLobbyNameField.setPromptText("Game lobby name");
        gameLobbyNameField.setPrefWidth(320);
        gameLobbyNameField.setMaxWidth(320);

        TextField maxPlayersField = new TextField();
        maxPlayersField.setPromptText("Max players");
        maxPlayersField.setPrefWidth(320);
        maxPlayersField.setMaxWidth(320);

        TextField numRoundsField = new TextField();
        numRoundsField.setPromptText("Number of rounds");
        numRoundsField.setPrefWidth(320);
        numRoundsField.setMaxWidth(320);

        TextField roundTimeField = new TextField();
        roundTimeField.setPromptText("Round time (in seconds)");
        roundTimeField.setPrefWidth(320);
        roundTimeField.setMaxWidth(320);

        TextField minShrimpField = new TextField();
        minShrimpField.setPromptText("Minimum amount of shrimp kilograms to catch");
        minShrimpField.setPrefWidth(320);
        minShrimpField.setMaxWidth(320);

        TextField maxShrimpField = new TextField();
        maxShrimpField.setPromptText("Maximum amount of shrimp kilograms to catch");
        maxShrimpField.setPrefWidth(320);
        maxShrimpField.setMaxWidth(320);

        Label errorLbl = new Label();
        errorLbl.setTextFill(Color.RED);
        errorLbl.setVisible(false);

        Button createBtn = new Button("Create Game");
        createBtn.setPrefWidth(320);
        createBtn.setPrefHeight(80);
        createBtn.setOnAction(
            event -> controller.handleCreateGameButton(gameLobbyNameField, maxPlayersField,
                                                       numRoundsField, roundTimeField,
                                                       minShrimpField, maxShrimpField, errorLbl));
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(320);
        cancelBtn.setPrefHeight(80);
        cancelBtn.setOnAction(event -> this.setScene(this.getMainScene()));

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        VBox.setVgrow(spacer1, Priority.ALWAYS);
        VBox.setVgrow(spacer2, Priority.ALWAYS);

        root.getChildren().addAll(spacer1, headingLbl, gameLobbyNameField, maxPlayersField,
                                  numRoundsField, roundTimeField, minShrimpField, maxShrimpField,
                                  errorLbl, createBtn, cancelBtn, spacer2);
        root.setAlignment(Pos.CENTER);

        Image backgroundImage = new Image(
            this.getClass().getResource("/images/create_game.jpg").toExternalForm());
        BackgroundSize backgroundSize = new BackgroundSize(1.0, 1.0, true, true, false, false);
        BackgroundImage background = new BackgroundImage(backgroundImage,
                                                         BackgroundRepeat.NO_REPEAT,
                                                         BackgroundRepeat.NO_REPEAT,
                                                         BackgroundPosition.CENTER, backgroundSize);
        root.setBackground(new Background(background));
        this.createGameScene = createGameScene;
    }

    public Scene getJoinGameTableScene()
    {
        return this.joinGameTableScene;
    }

    public void setJoinGameTableScene()
    {
        VBox root = new VBox();
        root.setSpacing(20);
        root.setPadding(new Insets(50, 0, 50, 0));
        Scene lobbyListScene = new Scene(root, 800, 600);
        lobbyListScene.getStylesheets().add(
            this.getClass().getResource("/css/styles.css").toExternalForm());
        Font helvetica = Font.loadFont("file:/fonts/Helvetica.ttf", 24);

        Label headingLbl = new Label("Lobby List");
        headingLbl.setFont(helvetica);
        headingLbl.getStyleClass().add("heading");

        TableView<Lobby> lobbyTableView = this.lobbyTableView;

        TableColumn<Lobby, String> lobbyNameCol = new TableColumn<>("Lobby Name");
        lobbyNameCol.setCellValueFactory(new PropertyValueFactory<>("lobbyName"));
        lobbyNameCol.setResizable(false);
        lobbyNameCol.setReorderable(false);
        lobbyNameCol.prefWidthProperty().bind(lobbyTableView.widthProperty().multiply(0.5));

        TableColumn<Lobby, Integer> playerCountCol = new TableColumn<>("Players");
        playerCountCol.setCellValueFactory(new PropertyValueFactory<>("numPlayers"));
        playerCountCol.setResizable(false);
        playerCountCol.setReorderable(false);
        playerCountCol.prefWidthProperty().bind(lobbyTableView.widthProperty().multiply(0.5));

        lobbyTableView.getColumns().addAll(lobbyNameCol, playerCountCol);
        lobbyNameCol.setCellFactory(tc -> new TableCell<>()
        {
            @Override
            protected void updateItem(String item, boolean empty)
            {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-alignment: CENTER;");
            }
        });

        playerCountCol.setCellFactory(tc -> new TableCell<>()
        {
            @Override
            protected void updateItem(Integer item, boolean empty)
            {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : Integer.toString(item));
                setStyle("-fx-alignment: CENTER;");
            }
        });

        Button joinBtn = new Button("JOIN");
        joinBtn.setPrefWidth(320);
        joinBtn.setPrefHeight(80);
        joinBtn.setOnAction(event ->
                            {
//                                // Get the selected lobby from the table view and join it
//                                Lobby selectedLobby =
//                                    lobbyTableView.getSelectionModel().getSelectedItem();
//                                if (selectedLobby != null)
//                                {
//                                    Alert successDialog = new Alert(Alert.AlertType.INFORMATION);
//                                    successDialog.setTitle("Success");
//                                    successDialog.setHeaderText(null);
//                                    successDialog.setContentText("Joined the lobby successfully!");
//                                    this.addIconToDialog(successDialog);
//                                    successDialog.showAndWait();
//                                }
                            });

        Button returnBtn = new Button("RETURN");
        returnBtn.setPrefWidth(320);
        returnBtn.setPrefHeight(80);
        returnBtn.setOnAction(event ->
                              {
                                  this.executor.shutdown();
                                  this.setScene(this.getMainScene());
                              });

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        VBox.setVgrow(spacer1, Priority.ALWAYS);
        VBox.setVgrow(spacer2, Priority.ALWAYS);

        root.getChildren().addAll(spacer1, headingLbl, lobbyTableView, joinBtn, returnBtn, spacer2);
        root.setAlignment(Pos.CENTER);

        // Set the background image for the lobby list screen
        Image backgroundImage = new Image(
            this.getClass().getResource("/images/create_game.jpg").toExternalForm());
        BackgroundSize backgroundSize = new BackgroundSize(1.0, 1.0, true, true, false, false);
        BackgroundImage background = new BackgroundImage(backgroundImage,
                                                         BackgroundRepeat.NO_REPEAT,
                                                         BackgroundRepeat.NO_REPEAT,
                                                         BackgroundPosition.CENTER, backgroundSize);
        root.setBackground(new Background(background));


        this.joinGameTableScene = lobbyListScene;
    }

    public Scene getCreateGameScene()
    {
        return this.createGameScene;
    }

    /**
     * Initializes the connection to the server, sends a request for the associated username and
     * returns it.
     *
     * @return the username associated with the client
     * @throws RuntimeException if there is a failure to initialize the server connection or to
     *                          send the username request to the server
     */
    private String[] initServerConnection()
    {
        String[] input;
        this.serverConnection = new ServerConnection("spill.datakomm.work", 8080);
        try
        {
            this.serverConnection.connect();
            input = this.serverConnection.sendUsernameRequest();
        }
        catch (RuntimeException exception)
        {
            throw new RuntimeException("Failed to initialize the server connection.");
        }
        return input;
    }

    /**
     * Initializes the game controller and sets up the protocol for sending and receiving
     * messages between the client and server.
     */
    private void initGameController()
    {
        gameController = new GameController(serverConnection);
    }

    /**
     * Returns the server connection object.
     *
     * @return the server connection object
     */
    public ServerConnection getServerConnection()
    {
        return serverConnection;
    }

    /**
     * Returns the game controller object.
     *
     * @return the game controller object
     */
    public GameController getGameController()
    {
        return gameController;
    }
}
