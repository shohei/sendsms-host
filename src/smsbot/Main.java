package smsbot;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Enumeration;
import java.util.HashSet;

public class Main extends Application {
    Controller c;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Pane pane = (Pane) loader.load();
        c = loader.<Controller>getController();
        primaryStage.setTitle("SendSMS");
        primaryStage.setScene(new Scene(root, 1000, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop(){
        //In case serial port not disconnected
        if(Controller.twoWaySerialComm!=null){
            c.disconnectFromSerialPort();
            Platform.exit();
            System.exit(0);
        }
    }

}
