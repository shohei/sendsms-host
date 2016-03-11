package smsbot;


import gnu.io.CommPortIdentifier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
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

    private ObservableList<Person> parentsData;
    private TableColumn firstNameCol;
    private TableColumn lastNameCol;
    private TableColumn telephoneCol;

    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        initComboBox();
        parentsData = FXCollections.observableArrayList();

        firstNameCol = new TableColumn("First Name");
        lastNameCol = new TableColumn("Last Name");
        telephoneCol = new TableColumn("Telephone");
        phoneNumberTableView.getColumns().addAll(firstNameCol, lastNameCol, telephoneCol);
        firstNameCol.setCellValueFactory(
                new PropertyValueFactory<Person,String>("firstName")
        );
        lastNameCol.setCellValueFactory(
                new PropertyValueFactory<Person,String>("lastName")
        );
        telephoneCol.setCellValueFactory(
                new PropertyValueFactory<Person,String>("telephone")
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
            if(ext.equals("xlsx")||ext.equals("XLSX")){
                phoneNumberTableView.getItems().removeAll(phoneNumberTableView.getItems());
                readXLSX(file);
                phoneNumberTableView.setItems(parentsData);
            }else if(ext.equals("xls")||ext.equals("XLS")){
                phoneNumberTableView.getItems().removeAll(phoneNumberTableView.getItems());
                readXLS(file);
                phoneNumberTableView.setItems(parentsData);
            }
        }
    }

    public void reloadSerialPort() {
        removeComboBox();
        initComboBox();
    }

    public void removeComboBox() {
        this.serialComboBox.getItems().removeAll(this.serialComboBox.getItems());
    }

    public void parsePhoneNumber(){
        //TODO:
    }

    public void validatePhoneNumber(){
        //TODO:
    }

    public void readXLSX(File file) {
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

                    int counter=0;
                    String[] parentInfo = new String[3];
                    while (cellIterator.hasNext()) {
                        if(counter>3){
                            break;
                        }
                        Cell cell = cellIterator.next();

                        switch (cell.getCellType()) {
                            case Cell.CELL_TYPE_STRING:
                                parentInfo[counter]=cell.getStringCellValue();
                                break;
                            case Cell.CELL_TYPE_NUMERIC:
                                parentInfo[counter]=String.valueOf(cell.getNumericCellValue());
                                break;
                        }
                        counter++;
                    }
                    Person person = new Person(parentInfo[0],parentInfo[1],parentInfo[2]);
                    parentsData.addAll(person);
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

    public void readXLS(File file){
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
            for(int i = 0; i < 10 || i < rows; i++) {
                row = sheet.getRow(i);
                if(row != null) {
                    tmp = sheet.getRow(i).getPhysicalNumberOfCells();
                    if(tmp > cols) cols = tmp;
                }
            }

            for(int r = 0; r < rows; r++) {
                row = sheet.getRow(r);
                if(row != null) {
                    int counter=0;
                    String[] parentInfo = new String[3];
                    for(int c = 0; c < cols; c++) {
                        cell = row.getCell((short)c);
                        if(cell != null) {
//                            System.out.println(cell.toString());
                            parentInfo[counter]=cell.toString();
                        }
                        counter++;
                    }
                    Person person = new Person(parentInfo[0],parentInfo[1],parentInfo[2]);
                    parentsData.addAll(person);
                }
            }
        } catch(Exception ioe) {
            ioe.printStackTrace();
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
