package smsbot;


import gnu.io.CommPortIdentifier;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
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
import org.controlsfx.dialog.ProgressDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private TextArea messageTextArea;
    @FXML
    private TextArea templateTextArea;
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
    @FXML
    public Label connectedLabel;
    @FXML
    public Label disconnectedLabel;
    @FXML
    private RadioButton textRadioBtn;
    @FXML
    private RadioButton templateRadioBtn;

    private ObservableList<Person> personsData;
    private ObservableList<Student> studentData;
    private TableColumn firstNameCol;
    private TableColumn lastNameCol;
    private TableColumn telephoneCol;

    public static final int MAX_SMS_LENGTH = 160;
    public static TwoWaySerialComm twoWaySerialComm;

    private String[] columnNames;

    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        initComboBox();
        initRadioGroup();
        initActionListenerForTextArea();
        personsData = FXCollections.observableArrayList();
        studentData = FXCollections.observableArrayList();

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
            if (ext.equals("xlsx") || ext.equals("XLSX")) {//When new XLSX format
                phoneNumberTableView.getItems().removeAll(phoneNumberTableView.getItems());
                if (textRadioBtn.isSelected()) {
                    readXLSXForFreeText(file);
                    phoneNumberTableView.setItems(personsData);
                } else {
                    readXLSXForTemplate(file);
                    phoneNumberTableView.setItems(studentData);
                }
            } else if (ext.equals("xls") || ext.equals("XLS")) {//When old XLS format
                phoneNumberTableView.getItems().removeAll(phoneNumberTableView.getItems());
                if (textRadioBtn.isSelected()) {
                    readXLSForFreeText(file);
                    phoneNumberTableView.setItems(personsData);
                } else {
                    readXLSForTemplate(file);
                    phoneNumberTableView.setItems(studentData);
                }
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About this software");
        alert.setHeaderText(null);
        alert.setContentText("(c)2016 Shohei Aoki. All Rights Reserved.");

        alert.showAndWait();
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
        ProgressDialog pd = new ProgressDialog(service);
        pd.setContentText("Sending SMS....");
        pd.setHeaderText("Please Wait...");
        pd.initModality(Modality.WINDOW_MODAL);
        service.start();

    }

    @FXML
    public void disconnectFromSerialPort() {
//        sender.disconnect();
        if (twoWaySerialComm != null) {
            twoWaySerialComm.disconnect();
        }
        connectedLabel.setText("");
        disconnectedLabel.setText("Disconnected from device.");
    }

    @FXML
    public void sendToSerialPort() {
        String msg = "{\"tel\":\"23328503949\",\"message\":\"hello from TTI\"}";
        twoWaySerialComm.send(msg);
    }

    @FXML
    public void connectToSerialPort() {
        try {
            String serialPort = serialComboBox.getValue();
            twoWaySerialComm = new TwoWaySerialComm();
            twoWaySerialComm.connect(serialPort, 19200);
//              sender = new SerialSender();
//              sender.connect(serialPort,19200);
            connectedLabel.setText("Connected from device.");
            disconnectedLabel.setText("");
        } catch (Exception e) {
            e.printStackTrace();
//        } catch (SerialSender.PortAlreadyUsedException ex){
//            showDialogPortUsed();
        }
    }

    public void showDialogPortUsed() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText("Already connected to the serial port!\n" +
                "Disconnect and try again.");
        alert.showAndWait();
    }

    public void showAlertRemind() {
        String msg = messageTextArea.getText();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Are you sure to send SMS?");
        alert.setContentText("Following message will be sent:\n\n" + msg);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            String message = messageTextArea.getText();
            System.out.println(message);
            messageSendingDialog();
        } else {
        }
    }

    public void showAlertNoMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText("Please input message before sending SMS!");
        alert.showAndWait();
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

    public void readXLSXForFreeText(File file) {
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
                    personsData.addAll(person);
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

    public void readXLSForFreeText(File file) {
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
                    Student student = new Student(parentInfo[0], parentInfo[1], parentInfo[2]);
                    studentData.addAll(student);
                }
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    public void readXLSXForTemplate(File file) {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
            try {
                Workbook workbook = new XSSFWorkbook(inputStream);
                Sheet firstSheet = workbook.getSheetAt(0);
                Iterator<Row> iterator = firstSheet.iterator();
                //count the total length of column

                Row nextRow1 = iterator.next();
                Iterator<Cell> cellIterator1 = nextRow1.cellIterator();
                int len = 0;
                while (cellIterator1.hasNext()) {
                    len++;
                }

                columnNames = new String[len];

                int rowCounter = 0;
                while (iterator.hasNext()) {
                    Row nextRow = iterator.next();
                    Iterator<Cell> cellIterator = nextRow.cellIterator();

                    //Only for first line. Needed for parsing title header
                    if (rowCounter == 0 && isHeaderIncluded()) {
                        int counter = 0;
                        while (cellIterator.hasNext()) {
                            Cell cell = cellIterator.next();
                            switch (cell.getCellType()) {
                                case Cell.CELL_TYPE_STRING:
                                    columnNames[counter] = cell.getStringCellValue();
                                    break;
                            }
                            counter++;
                        }
                        rowCounter++;//for this, never called again in loop
                        continue;
                    }

                    int counter = 0;
                    String[] parentInfo = new String[3];
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        switch (cell.getCellType()) {
                            case Cell.CELL_TYPE_STRING:
                                parentInfo[counter] = cell.getStringCellValue();
                                break;
                            case Cell.CELL_TYPE_NUMERIC:
                                parentInfo[counter] = String.valueOf(cell.getNumericCellValue());
                                break;
                        }
                        counter++;
                    }
                    Student student = new Student(parentInfo[0], parentInfo[1], parentInfo[2]);
                    studentData.addAll(student);
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


    public void readXLSForTemplate(File file) {

    }

    public void initComboBox() {
        java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            System.out.println(portIdentifier.getName());
            serialComboBox.getItems().addAll(portIdentifier.getName());
        }
    }

    public void initRadioGroup() {
        final ToggleGroup group = new ToggleGroup();
        textRadioBtn.setToggleGroup(group);
        textRadioBtn.setSelected(true);
        templateRadioBtn.setToggleGroup(group);

    }

    @FXML
    public void enableFreeText() {
        messageTextArea.setDisable(false);
        templateTextArea.setDisable(true);
    }

    @FXML
    public void enableTemplate() {
        messageTextArea.setDisable(true);
        templateTextArea.setDisable(false);
    }

}
