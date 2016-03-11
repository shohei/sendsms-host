package smsbot;


import gnu.io.CommPortIdentifier;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

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


    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        initComboBox();
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
