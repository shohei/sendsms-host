package smsbot;


import gnu.io.CommPortIdentifier;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable{
    @FXML
    private TextArea messageTextArea;
    @FXML
    private ComboBox<String> serialComboBox;
    @FXML
    private TableView<List<String>> phoneNumberTableView;
    @FXML
    private Button serialConnectBtn;
    @FXML
    private Button loadExcelBtn;
    @FXML
    private Button sendSmsBtn;
    @FXML
    private Label filePathLabel;
    @FXML
    private Label fileLoadLabel;
    @FXML
    private Button reloadPortBtn;


    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        initComboBox();
    }

    @FXML
    public void openFileDialog(){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Excel file");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel document(*.xls,*xlsx)", "*.xls","*.xlsx"));
        File file = chooser.showOpenDialog(new Stage());
        if(file!=null){
            fileLoadLabel.setText("File location:");
            filePathLabel.setText(file.toPath().toString());
        }
    }

    public void reloadSerialPort(){
        removeComboBox();
        initComboBox();
    }

    public void removeComboBox(){
        this.serialComboBox.getItems().removeAll(this.serialComboBox.getItems());
    }

    public void initComboBox(){
        java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while ( portEnum.hasMoreElements() )
        {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            System.out.println(portIdentifier.getName());
            this.serialComboBox.getItems().addAll(portIdentifier.getName());
        }
    }


}
