package mps;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mps.logic.ExpressionResult;
import mps.logic.ExpressionService;

public class MainApp extends Application {

    private final ExpressionService expressionService = new ExpressionService();

    @Override
    public void start(Stage primaryStage) {
        Scene[] startSceneHolder = new Scene[1];
        Scene mainScene = new Scene(buildMainView(primaryStage, () -> primaryStage.setScene(startSceneHolder[0])), 820, 600);
        Scene startScene = new Scene(buildStartView(primaryStage, mainScene), 820, 600);
        startSceneHolder[0] = startScene;

        primaryStage.setTitle("Mathematical Problem Solver");
        primaryStage.setScene(startScene);
        primaryStage.show();
    }

    private BorderPane buildMainView(Stage primaryStage, Runnable backAction) {
        TextArea inputArea = new TextArea();
        inputArea.setPromptText("Enter infix expression, e.g., (3+4)*2^2");
        inputArea.setPrefRowCount(2);
        inputArea.setStyle("-fx-control-inner-background: #1f2937; -fx-text-fill: #f9fafb; -fx-prompt-text-fill: #e5e7eb; -fx-highlight-fill: #3b82f6; -fx-highlight-text-fill: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #334155;");

        Label postfixLabel = new Label("Postfix:");
        Label resultLabel = new Label("Result:");
        postfixLabel.setStyle("-fx-text-fill: #f9fafb; -fx-font-weight: bold;");
        resultLabel.setStyle("-fx-text-fill: #f9fafb; -fx-font-weight: bold;");

        TextArea stepsArea = new TextArea();
        stepsArea.setEditable(false);
        stepsArea.setWrapText(true);
        stepsArea.setPrefRowCount(14);
        stepsArea.setStyle("-fx-control-inner-background: #111827; -fx-text-fill: #e5e7eb; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #334155;");

        TextArea errorArea = new TextArea();
        errorArea.setEditable(false);
        errorArea.setWrapText(true);
        errorArea.setPrefRowCount(4);
        errorArea.setStyle("-fx-control-inner-background: #111827; -fx-text-fill: #fca5a5; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #334155;");

        Button solveButton = new Button("Solve");
        Button clearButton = new Button("Clear");
        solveButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 8 18;");
        clearButton.setStyle("-fx-background-color: #1f2937; -fx-text-fill: #e5e7eb; -fx-background-radius: 12; -fx-border-color: #334155; -fx-border-radius: 12; -fx-padding: 8 18;");

        solveButton.setOnAction(event -> {
            String expression = inputArea.getText().trim();
            ExpressionResult result = expressionService.process(expression);

            postfixLabel.setText("Postfix: " + (result.getPostfix() == null ? "" : result.getPostfix()));
            resultLabel.setText("Result: " + (result.getResult() == null ? "" : result.getResult()));
            stepsArea.setText(result.getStepsText() == null ? "" : result.getStepsText());
            errorArea.setText(result.getErrorText() == null ? "" : result.getErrorText());
        });

        clearButton.setOnAction(event -> {
            inputArea.clear();
            postfixLabel.setText("Postfix:");
            resultLabel.setText("Result:");
            stepsArea.clear();
            errorArea.clear();
        });

        HBox buttons = new HBox(10, solveButton, clearButton);

        Label infixLabel = new Label("Infix Expression:");
        infixLabel.setStyle("-fx-text-fill: #f9fafb; -fx-font-weight: bold;");

        Button backButton = new Button("←");
        backButton.setStyle("-fx-background-color: #1f2937; -fx-text-fill: #e5e7eb; -fx-background-radius: 12; -fx-border-color: #334155; -fx-border-radius: 12; -fx-padding: 6 12;");
        backButton.setOnAction(event -> backAction.run());

        HBox headerRow = new HBox(10, backButton, infixLabel);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox topBox = new VBox(8,
            headerRow,
                inputArea,
                buttons,
                postfixLabel,
                resultLabel);
        topBox.setPadding(new Insets(10));
        topBox.setStyle("-fx-background-color: transparent; -fx-text-fill: #f9fafb;");

        Label stepsTitle = new Label("Step-by-Step Stack Operations:");
        stepsTitle.setStyle("-fx-text-fill: #f9fafb; -fx-font-weight: bold;");
        Label errorsTitle = new Label("Errors / Suggestions:");
        errorsTitle.setStyle("-fx-text-fill: #f9fafb; -fx-font-weight: bold;");

        VBox centerBox = new VBox(8,
            stepsTitle,
                stepsArea,
            errorsTitle,
                errorArea);
        centerBox.setPadding(new Insets(10));
        centerBox.setStyle("-fx-background-color: transparent; -fx-text-fill: #f9fafb;");

        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setCenter(centerBox);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #0f172a, #111827);");

        return root;
    }

    private BorderPane buildStartView(Stage primaryStage, Scene mainScene) {
        Label title = new Label("Mathematical Problem Solver");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #f9fafb;");

        Button startButton = new Button("Start");
        startButton.setPrefWidth(140);
        startButton.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 14; -fx-padding: 10 26;");
        startButton.setOnAction(event -> primaryStage.setScene(mainScene));

        VBox content = new VBox(18, title, startButton);
        content.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setCenter(content);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #0f172a, #1e293b);");
        return root;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
