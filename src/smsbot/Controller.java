package smsbot;


import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.labs.dialogs.MonologFXButton;
import jfxtras.labs.dialogs.MonologFXButtonBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private TextArea messageTextArea;
    @FXML
    private ComboBox<String> serialComboBox;
    @FXML
    private TableView phoneNumberTableView;
    @FXML
    private Label filePathLabel;
    @FXML
    private Label fileLoadLabel;
    @FXML
    private CheckBox withHeaderCheckbox;
    @FXML
    private Label messageLengthLabel;

    private ObservableList<Person> parentsData;
    private TableColumn firstNameCol;
    private TableColumn lastNameCol;
    private TableColumn telephoneCol;

    public static final int MAX_SMS_LENGTH = 160;
    public TwoWaySerialComm twoWaySerialComm;

    SerialSender sender;

    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        initComboBox();
        initActionListenerForTextArea();
        parentsData = FXCollections.observableArrayList();

        firstNameCol = new TableColumn("First Name");
        lastNameCol = new TableColumn("Last Name");
        telephoneCol = new TableColumn("Telephone");
        phoneNumberTableView.getColumns().addAll(firstNameCol, lastNameCol, telephoneCol);
        firstNameCol.setCellValueFactory(
                new PropertyValueFactory<Person, String>("firstName")
        );
        lastNameCol.setCellValueFactory(
                new PropertyValueFactory<Person, String>("lastName")
        );
        telephoneCol.setCellValueFactory(
                new PropertyValueFactory<Person, String>("telephone")
        );

    }

    @FXML
    public void openFileDialog() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Excel file");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel document(*.xls,*xlsx)", "*.xls", "*.xlsx"));
        File file = chooser.showOpenDialog(new Stage());
        if (file != null) {
            fileLoadLabel.setText("File location:");
            filePathLabel.setText(file.toPath().toString());
            String ext = FilenameUtils.getExtension(file.getAbsolutePath());
            if (ext.equals("xlsx") || ext.equals("XLSX")) {
                phoneNumberTableView.getItems().removeAll(phoneNumberTableView.getItems());
                readXLSX(file);
                phoneNumberTableView.setItems(parentsData);
            } else if (ext.equals("xls") || ext.equals("XLS")) {
                phoneNumberTableView.getItems().removeAll(phoneNumberTableView.getItems());
                readXLS(file);
                phoneNumberTableView.setItems(parentsData);
            }
        }
    }

    @FXML
    public void doSendSms() {
        if (isMessageWritten()) {
            showAlertRemind();
        } else {
            showAlertNoMessage();
        }
    }

    @FXML
    public void showAboutDialog() {
        Dialogs.create()
                .title("About this software")
                .masthead("SendSMS")
                .message("(c)2016 Shohei Aoki. All Rights Reserved.")
                .showInformation();
    }


    public void initActionListenerForTextArea() {
        messageLengthLabel.setText("0/" + String.valueOf(MAX_SMS_LENGTH));

        messageTextArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                if (messageTextArea.getText().length() > MAX_SMS_LENGTH) {
                    String s = messageTextArea.getText().substring(0, MAX_SMS_LENGTH);
                    messageTextArea.setText(s);
                }
                int mLength = messageTextArea.getLength();
                messageLengthLabel.setText(String.valueOf(mLength) + "/" + String.valueOf(MAX_SMS_LENGTH));
            }
        });
    }

    public void messageSendingDialog() {
        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call()
                            throws InterruptedException {
                        updateMessage("Finding friends . . .");
                        updateProgress(0, 10);
                        for (int i = 0; i < 10; i++) {
                            Thread.sleep(300);
                            updateProgress(i + 1, 10);
                            updateMessage("Found " + (i + 1) + " friends!");
                        }
                        updateMessage("Found all.");
                        return null;
                    }
                };
            }
        };

        Dialogs.create()
                .title("Progress dialog")
                .masthead("Sending SMS. Please wait until finished.")
                .showWorkerProgress(service);

        service.start();
    }

    @FXML
    public void disconnectFromSerialPort() {
//        sender.disconnect();
        twoWaySerialComm.disconnect();
    }

    @FXML
    public void sendToSerialPort(){
        String msg = "{\"tel\":\"23328503949\",\"message\":\"hello from TTI\"}";
        twoWaySerialComm.send(msg);
    }

    @FXML
    public void connectToSerialPort() {
        try {
            String serialPort = serialComboBox.getValue();
            twoWaySerialComm =  new TwoWaySerialComm();
            twoWaySerialComm.connect(serialPort, 19200);
//              sender = new SerialSender();
//              sender.connect(serialPort,19200);
        } catch (Exception e) {
            e.printStackTrace();
//        } catch (SerialSender.PortAlreadyUsedException ex){
//            showDialogPortUsed();
        }
    }

    public void showDialogPortUsed() {
        Dialogs.create()
                .title("Warning")
                .masthead(null)
                .message("Already connected to the serial port!\n" +
                        "Disconnect and try again.")
                .showInformation();
    }

    public void showAlertRemind() {
        String msg = messageTextArea.getText();
        Action response = Dialogs.create()
                .title("Confirmation ")
                .masthead("Are you sure to send SMS?")
                .message("Following message will be sent:\n\n" + msg)
                .actions(Dialog.Actions.OK, Dialog.Actions.CANCEL)
                .showConfirm();

        if (response == Dialog.Actions.OK) {
            String message = messageTextArea.getText();
            System.out.println(message);
            messageSendingDialog();
        } else {
            //do nothing
        }
    }

    public void showAlertNoMessage() {
        Dialogs.create()
                .title("Information")
                .masthead(null)
                .message("Please input message before sending SMS!")
                .showInformation();
    }

    public boolean isMessageWritten() {
        if (messageTextArea.getLength() > 0) {
            return true;
        } else {
            return false;
        }
    }

    @FXML
    public void reloadSerialPort() {
        removeComboBox();
        initComboBox();
    }

    public void removeComboBox() {
        this.serialComboBox.getItems().removeAll(this.serialComboBox.getItems());
    }

    public void parsePhoneNumber() {
        //TODO:
    }

    public void validatePhoneNumber() {
        //TODO:
    }

    public boolean isHeaderIncluded() {
        if (withHeaderCheckbox.isSelected()) {
            return true;
        } else {
            return false;
        }
    }

    public void readXLSX(File file) {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
            try {
                Workbook workbook = new XSSFWorkbook(inputStream);
                Sheet firstSheet = workbook.getSheetAt(0);
                Iterator<Row> iterator = firstSheet.iterator();

                int rowCounter = 0;
                while (iterator.hasNext()) {
                    Row nextRow = iterator.next();
                    Iterator<Cell> cellIterator = nextRow.cellIterator();

                    if (rowCounter == 0 && isHeaderIncluded()) {
                        rowCounter++;
                        continue;
                    }
                    int counter = 0;
                    String[] parentInfo = new String[3];
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        switch (cell.getCellType()) {
                            case Cell.CELL_TYPE_STRING:
                                if (counter < 3) {
                                    parentInfo[counter] = cell.getStringCellValue();
                                }
                                break;
                            case Cell.CELL_TYPE_NUMERIC:
                                if (counter < 3) {
                                    parentInfo[counter] = String.valueOf(cell.getNumericCellValue());
                                }
                                break;
                        }
                        counter++;
                    }
                    Person person = new Person(parentInfo[0], parentInfo[1], parentInfo[2]);
                    parentsData.addAll(person);
                    rowCounter++;
                }

                workbook.close();
                inputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (
                FileNotFoundException ex
                )

        {
            ex.printStackTrace();
        }

    }

    public void readXLS(File file) {
        try {
            POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(file));
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row;
            HSSFCell cell;

            int rows; // No of rows
            rows = sheet.getPhysicalNumberOfRows();

            int cols = 0; // No of columns
            int tmp = 0;

            // This trick ensures that we get the data properly even if it doesn't start from first few rows
            for (int i = 0; i < 10 || i < rows; i++) {
                row = sheet.getRow(i);
                if (row != null) {
                    tmp = sheet.getRow(i).getPhysicalNumberOfCells();
                    if (tmp > cols) cols = tmp;
                }
            }

            for (int r = 0; r < rows; r++) {
                row = sheet.getRow(r);
                if (row != null) {
                    int counter = 0;
                    String[] parentInfo = new String[3];
                    if (r == 0 && isHeaderIncluded()) {
                        continue;
                    }
                    for (int c = 0; c < cols; c++) {
                        cell = row.getCell((short) c);
                        if (cell != null) {
//                            System.out.println(cell.toString());
                            if (counter < 3) {
                                parentInfo[counter] = cell.toString();
                            }
                        }
                        counter++;
                    }
                    Person person = new Person(parentInfo[0], parentInfo[1], parentInfo[2]);
                    parentsData.addAll(person);
                }
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    public void initComboBox() {
        java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            System.out.println(portIdentifier.getName());
            serialComboBox.getItems().addAll(portIdentifier.getName());
        }
    }


}
