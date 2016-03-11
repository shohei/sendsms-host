package smsbot;


import gnu.io.CommPortIdentifier;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
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
    public void openFileDialog() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Excel file");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel document(*.xls,*xlsx)", "*.xls", "*.xlsx"));
        File file = chooser.showOpenDialog(new Stage());
        if (file != null) {
            fileLoadLabel.setText("File location:");
            filePathLabel.setText(file.toPath().toString());
            readExcel(file);
        }
    }

    public void reloadSerialPort() {
        removeComboBox();
        initComboBox();
    }

    public void removeComboBox() {
        this.serialComboBox.getItems().removeAll(this.serialComboBox.getItems());
    }

    public void readExcel(File file) {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
            try {
                Workbook workbook = new XSSFWorkbook(inputStream);
                Sheet firstSheet = workbook.getSheetAt(0);
                Iterator<Row> iterator = firstSheet.iterator();

                while (iterator.hasNext()) {
                    Row nextRow = iterator.next();
                    Iterator<Cell> cellIterator = nextRow.cellIterator();

                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();

                        switch (cell.getCellType()) {
                            case Cell.CELL_TYPE_STRING:
                                System.out.print(cell.getStringCellValue());
                                break;
                            case Cell.CELL_TYPE_BOOLEAN:
                                System.out.print(cell.getBooleanCellValue());
                                break;
                            case Cell.CELL_TYPE_NUMERIC:
                                System.out.print(cell.getNumericCellValue());
                                break;
                        }
                        System.out.print(" - ");
                    }
                    System.out.println();
                }

                workbook.close();
                inputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

    }


    public void initComboBox() {
        java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            System.out.println(portIdentifier.getName());
            this.serialComboBox.getItems().addAll(portIdentifier.getName());
        }
    }


}
