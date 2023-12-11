package cs1302.api;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.URI;
import java.io.IOException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Background;

/**
 * REPLACE WITH NON-SHOUTING DESCRIPTION OF YOUR APP.
 */
public class ApiApp extends Application {
    Stage stage;
    Scene scene;
    VBox root;
    Location actualLoc;
    String city;
    String country;
    String apiNinjaKey = "81do8p6+64uY48JjmFbJJg==neKPH77W7cTW6v4h";
    String owKey = "5013ce8d785fbfb5d8517c046aeeefa6";
    HBox topBar;
    Button unitedStates;
    Button worldWide;
    Label cityLabel;
    Label countryLabel;
    Label stateLabel;
    TextField cityField;
    TextField countryField;
    Button search;
    HBox locationBox;
    Label locationLabel;
    HBox middleBox;
    Image weatherImage;
    ImageView weatherImageView;
    Label defaultLabel;
    OpenWeatherResponse opr;
    Label mainTemp;
    HBox bottomBox;
    Label weatherMain;
    Label tempMax;
    Label tempMin;
    Label feels;

    /** HTTP client. */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();


    /** Google {@code Gson} object for parsing JSON-formatted strings. */
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();

     /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        root = new VBox();
        topBar = new HBox(15);
        unitedStates = new Button("US");
        worldWide = new Button("World");
        cityLabel = new Label("City:");
        countryLabel = new Label("Country:");
        stateLabel = new Label("State:");
        cityField = new TextField("City name");
        countryField = new TextField("Country name(optional)");
        search = new Button("Search");
        locationBox = new HBox();
        locationLabel = new Label("Please Enter city name and country/state name and press Search");
        middleBox = new HBox(10);
        weatherImage = null;
        weatherImageView = new ImageView(weatherImage);
        mainTemp = new Label("- F");
        bottomBox = new HBox(10);
        weatherMain = new Label("Weather: ");
        tempMax = new Label("Maximum temperature: ");
        tempMin = new Label("Minimum temperature: ");
        feels = new Label("Feels like: ");
    } // ApiApp


    /** {@inheritDoc} */
    @Override
    public void init() {
        worldWide.setDisable(true);
        topBar.setPrefWidth(700);
        cityLabel.setPadding(new Insets(4, 0, 0, 0));
        countryLabel.setPadding(new Insets(4, 0, 0, 0));
        locationBox.setAlignment(Pos.CENTER);
        locationBox.setPrefHeight(50);
        locationLabel.setFont(new Font(20));
        mainTemp.setFont(new Font(50));
        middleBox.setPrefHeight(175);
        middleBox.setAlignment(Pos.CENTER);
        middleBox.setStyle("-fx-background-color:AZURE");
        bottomBox.setAlignment(Pos.CENTER);

        // setup scene
        root.getChildren().addAll(topBar, locationBox, middleBox, bottomBox);
        topBar.getChildren().addAll(
            unitedStates, worldWide, cityLabel, cityField, countryLabel, countryField, search);
        locationBox.getChildren().addAll(locationLabel);
        middleBox.getChildren().addAll(mainTemp, weatherImageView);
        bottomBox.getChildren().addAll(weatherMain, tempMax, tempMin, feels);

        EventHandler<ActionEvent> searchClicked = (ActionEvent e) -> {
            try {
                this.searchWeather();
            } catch (IllegalStateException iae) {
                this.locationError();
            }
        };

        unitedStates.setOnAction(event -> this.changeToUs());
        worldWide.setOnAction(event -> this.changeToWorld());
        search.setOnAction(searchClicked);
    }

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        scene = new Scene(root);
        // setup stage
        stage.setTitle("WeatherApp!");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();
        Platform.runLater(() -> this.stage.setResizable(false));
    } // start

    /** getLocation.
     *@param city
     *@param country
     */
    public void getLocation(String city, String country) {
        if (city.contains(" ")) {
            city = city.replace(" ", "_");
        }
        if (country.contains(" ")) {
            country = country.replace(" ",  "_");
        }
        Location[] loc = null;
        URI soc = null;

        if (unitedStates.isDisabled()) {
            soc = URI.create(
                "https://api.api-ninjas.com/v1/geocoding?city=" + city + "&country=US"
                + "&state=" + country);
        } else {
            soc = URI.create(
                "https://api.api-ninjas.com/v1/geocoding?city=" + city + "&country=" + country);
        }

        HttpRequest request = HttpRequest.newBuilder().uri(soc).
            header("X-Api-key", apiNinjaKey).build();
        try {
            HttpResponse<String> response = HTTP_CLIENT.<String>send(
                request, BodyHandlers.ofString());
            String responseBody = response.body();

            loc = GSON.<Location[]>fromJson(responseBody, Location[].class);
        } catch (IOException | InterruptedException e) {
            System.out.println("Problem occured accessing the server");
        }
        if (loc.length == 0) {
            throw new IllegalStateException("no city exist");
        }
        this.actualLoc = loc[0];

        for (Location l : loc) {
            if (l.name.equals(city)) {
                this.actualLoc = l;
            }
        }

        this.getWeather();
    }

    /** getWeather.
    */
    public void getWeather() {
        OpenWeatherResponse opr = null;
        URI soc = URI.create(
            "https://api.openweathermap.org/data/2.5/weather?lat=" + actualLoc.latitude
            + "&lon=" + actualLoc.longitude + "&units=imperial" + "&appid=" + owKey);
        HttpRequest request = HttpRequest.newBuilder().uri(soc).build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.<String>send(
                request, BodyHandlers.ofString());
            String responseBody = response.body();

            opr = GSON.<OpenWeatherResponse>fromJson(responseBody, OpenWeatherResponse.class);
        } catch (IOException | InterruptedException e) {
            System.out.println("Problem occured accessing the server");
        }
        this.opr = opr;
    }

    /**changeToUs.
     */
    public void changeToUs() {
        unitedStates.setDisable(true);
        worldWide.setDisable(false);
        countryLabel.setText("State:");
        countryField.setText("State name(optional)");
    }

    /**changeToWorld.
     */
    public void changeToWorld() {
        worldWide.setDisable(true);
        unitedStates.setDisable(false);
        countryLabel.setText("Country:");
        countryField.setText("Country name(optional)");
    }

    /**searchWeather.
     */
    public void searchWeather() {
        this.city = cityField.getText();
        this.country = countryField.getText();
        this.getLocation(city, country);
        if (worldWide.isDisabled()) {
            this.locationLabel.setText(
                "Current Weather at: " + actualLoc.name + ", " + actualLoc.country);
        } else {
            this.locationLabel.setText(
                "Current Weather at: " + actualLoc.name + ", " + actualLoc.state);
        }
        this.weatherImageView.setImage( new Image (
            "https://openweathermap.org/img/wn/" + opr.weather[0].icon
            + "@2x.png", 150, 150, true, true));
        this.mainTemp.setText(opr.main.temp + "F");
        this.weatherMain.setText("Weather: " + opr.weather[0].main);
        this.tempMax.setText("Maximum temperature: " + opr.main.tempMax + " F");
        this.tempMin.setText("Minimum temperature: " + opr.main.tempMin + " F");
        this.feels.setText("Feels like: " + opr.main.feelsLike + " F");
    }

    /**locationError.
     */
    public void locationError() {
        TextArea text = new TextArea("Please enter a valid location");

        Runnable task = () -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.getDialogPane().setContent(text);
            alert.setResizable(false);
            alert.showAndWait();
        };
        Platform.runLater(task);
    }
} // ApiApp
