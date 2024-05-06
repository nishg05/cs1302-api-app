package cs1302.api;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import javafx.scene.control.TextField;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.net.http.HttpClient;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.net.URLEncoder;
import java.io.InputStream;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.Priority;
import javafx.concurrent.Task;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;
import java.util.Map;
import javafx.scene.layout.BorderPane;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.layout.TilePane;
import javafx.scene.control.ButtonType;
import java.util.Random;
import java.lang.Thread;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * REPLACE WITH NON-SHOUTING DESCRIPTION OF YOUR APP.
 */
public class ApiApp extends Application {

    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    Stage stage;
    Scene scene;
    VBox root;
    HBox hbox;
    Button getButton;
    Button getFactButton;
    Label messageBar;
    HBox mainContent;
    ScheduledExecutorService executorService;
    boolean currentlyFetching;
    List<String> imageUrls;
    TilePane tilePane;
    String apiUrl;
    TextArea factTextField;
    String countryName;

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        root = new VBox();
        hbox = new HBox();
        getButton = new Button("Get Random Cat image");
        getFactButton = new Button("Country Stats:");
        getFactButton.setVisible(false);
        mainContent = new HBox();
        currentlyFetching = false;
        executorService = Executors.newSingleThreadScheduledExecutor();
        imageUrls = new ArrayList<>();
        factTextField = new TextArea();
        factTextField.setEditable(false);
        factTextField.setVisible(false);
        factTextField.setPrefWidth(400);
        factTextField.setPrefHeight(300);
        factTextField.setWrapText(true);
    } // ApiApp

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {

        this.stage = stage;

        hbox.setSpacing(7);
        hbox.getChildren().addAll(getButton, getFactButton);

        mainContent.setMinSize(400, 400);
        mainContent.setStyle("-fx-border-color: black");

        root.getChildren().addAll(hbox, mainContent, factTextField);

        getButton.setOnAction(event -> {
            try {
                getButton.setDisable(true);
                fetchImages();
                getFactButton.setVisible(true);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR,
                                        "Exception: " + e.toString(), ButtonType.OK);
                alert.showAndWait();
                getButton.setDisable(false);
            }
        });

        getFactButton.setOnAction(event -> {
            try {
                getFactButton.setDisable(true);
                getCountryStats(countryName);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR,
                                        "Exception: " + e.toString(), ButtonType.OK);
                alert.showAndWait();
                getFactButton.setDisable(false);
            }
        });

        scene = new Scene(root);

        stage.setTitle("Random Cats and Countries App");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();
        Platform.runLater(() -> this.stage.setResizable(false));
    } // start

    private void fetchImages() throws IOException, InterruptedException {

        getButton.setDisable(true);

        apiUrl = "https://api.thecatapi.com/v1/images/search?has_breeds=true&api_key"
            + "=live_X18ejBHTLe9S88dvleYxns8qIsbXUjt47035C1D3dhAEIzZV6eORIllDxPee7knQ";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request,
                                                         BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException(response.toString());
        }

        System.out.println(response);
        String bodyResponse = response.body();
        System.out.println(bodyResponse);

        int breedIndex = bodyResponse.indexOf("\"origin\":\"");
        System.out.println("breedIndex: " + breedIndex);
        if (breedIndex != -1) {
            int countryIndex = breedIndex + "\"origin\":\"".length();
            int endIndex = bodyResponse.indexOf("\"", countryIndex);
            if (endIndex != -1) {
                String countrySubstring = bodyResponse.substring(countryIndex, endIndex);
                System.out.println("Country Name: " + countrySubstring);
                countryName = countrySubstring;
            } else {
                System.out.println("Country name not found.");
            }
        } else {
            System.out.println("Country name not found.");
        }


        int index = bodyResponse.indexOf("\"url\":\"");
        if (index != -1) {
            int endIndex = bodyResponse.indexOf("\"", index + 7);
            String imageUrl = bodyResponse.substring(index + 7, endIndex);
            System.out.println(imageUrl);

            ImageView imageView = new ImageView(new Image(imageUrl));
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(400);
            imageView.setFitHeight(400);

            mainContent.getChildren().clear();
            mainContent.getChildren().add(imageView);
        }

        getButton.setDisable(false);
    }

    private void getCountryStats(String country) throws IOException, InterruptedException {

        if (country.contains(" ")) {
            country = country.replace(" ", "%20");
        }

        String factUrl = "https://restcountries.com/v3.1/name/" + country;
        getFactButton.setDisable(true);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(factUrl))
            .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request,
                                                         BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException(response.toString());
        }

        String responseBody = response.body();
        String capital = extractValue(responseBody, "capital");
        String region = extractValue(responseBody, "region");
        String subregion = extractValue(responseBody, "subregion");
        String languages = extractLanguages(responseBody);

        String countryInfo = "Capital: " + capital + "\n"
            + "Region: " + region + "\n"
            + "Subregion: " + subregion + "\n"
            + "Languages: " + languages;
        factTextField.setText(countryInfo);
        factTextField.setVisible(true);

        getFactButton.setDisable(false);

    }

    public String extractValue(String responseBody, String key) {
        int startIndex = responseBody.indexOf("\"" + key + "\":");
        if (startIndex == -1) {
            return "N/A";
        }
        startIndex = responseBody.indexOf("\"", startIndex + key.length() + 4) + 1;
        int endIndex = responseBody.indexOf("\"", startIndex);
        return responseBody.substring(startIndex, endIndex);
    }

    public String extractLanguages(String responseBody) {
        int startIndex = responseBody.indexOf("\"languages\":");
        if (startIndex == -1) {
            return "N/A";
        }
        startIndex = responseBody.indexOf("{", startIndex) + 1;
        int endIndex = responseBody.indexOf("}", startIndex);
        String languagesObj = responseBody.substring(startIndex, endIndex);
        StringBuilder languages = new StringBuilder();
        int langIndex = languagesObj.indexOf("\"", startIndex);
        while (langIndex != -1) {
            int langEndIndex = languagesObj.indexOf("\"", langIndex + 1);
            languages.append(languagesObj, langIndex + 1, langEndIndex).append(", ");
            langIndex = languagesObj.indexOf("\"", langEndIndex + 1);
        }
        if (languages.length() > 2) {
            languages.delete(languages.length() - 2, languages.length());
        }
        return languages.toString();
    }
} // ApiApp
