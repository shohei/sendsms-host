package smsbot;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;


public class Main extends Application {
    Controller c;
    String firstPath;

    @Override
    public void start(Stage primaryStage) throws Exception {

        if (OSUtils.isWindows()) {
            extractJarFromResource();
        }

        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Pane pane = (Pane) loader.load();
        c = loader.<Controller>getController();
        primaryStage.setTitle("SendSMS");
        primaryStage.setScene(new Scene(root, 1000, 600));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        //In case serial port not disconnected
        if (Controller.twoWaySerialComm != null) {
            c.disconnectFromSerialPort();
            //Unload .dll before deleting
            //I gave up to delete dll. To do this, defining another class loader is required.
//            if (OSUtils.isWindows()) {
//                deleteTemporalDll();
//            }
            Platform.exit();
            System.exit(0);
        }else{
            //Same thing above
//            if (OSUtils.isWindows()) {
//                deleteTemporalDll();
//            }
        }
    }

//    private static void loadLib32bitFromJar() {
//        System.load("fileName");//loading goes here
//    }
//
//    private static void loadLib64bitFromJar() {
//        System.load("fileName");//loading goes here
//    }

    public void expandJarAndLoadDll(String jarPath) {
        try {
            java.util.jar.JarFile jar = new java.util.jar.JarFile(jarPath);
//            java.util.jar.JarFile jar = getClass().
//                    getClassLoader().getResource("smsbot/lib/rxtx-native-windows.jar");
            java.util.Enumeration enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
                String destDirName = System.getProperty("java.io.tmpdir") + "SendSMS";
                File destDir = new File(destDirName);
                if (!destDir.exists()) {
                    destDir.mkdir();
                }
                System.out.println("Expand .dll to temp dir: " + destDirName);
                java.io.File f = new java.io.File(destDirName + java.io.File.separator + file.getName());
                if (file.isDirectory()) { // if its a directory, create it
                    f.mkdir();
                    continue;
                }
                java.io.InputStream is = jar.getInputStream(file); // get the input stream
                java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
                while (is.available() > 0) {  // write contents of 'is' to 'fos'
                    fos.write(is.read());
                }
                fos.close();
                is.close();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String tmpdir = System.getProperty("java.io.tmpdir");

        String srcFile;
        if (OSUtils.is32bit()) {
            //load .dll for 32bit
            srcFile = tmpdir + "SendSMS" + java.io.File.separator + "rxtxSerial.dll";
            System.out.println("32bit OS detected.");
        } else {
            //load .dll for 64bit
            srcFile = tmpdir + "SendSMS" + java.io.File.separator + "rxtxSerial64.dll";
            System.out.println("64bit OS detected.");
        }

        //move .dll to java.library.path
        String destFile = System.getProperty("user.dir") + File.separator + "rxtxSerial.dll";
        try {
            if(!new File(destFile).exists()){
                System.out.println("Copying rxtxSerial.dll to user.dir");
                System.out.println("Destination: "+destFile);
                copyFile(new File(srcFile),new File(destFile));
            }
            System.loadLibrary("rxtxSerial");
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public void extractJarFromResource() {
        File file = null;
        String resource = "/smsbot/resources/rxtx-native-windows.jar";
        URL res = getClass().getResource(resource);
        if (res.toString().startsWith("jar:")) {
            try {
                InputStream input = getClass().getResourceAsStream(resource);
                file = File.createTempFile("rxtxSerial", ".jar");
                String tmpFilePath = file.getPath();
                System.out.println("temp file location: " + tmpFilePath);
                OutputStream out = new FileOutputStream(file);
                int read;
                byte[] bytes = new byte[1024];

                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                file.deleteOnExit();

                expandJarAndLoadDll(tmpFilePath);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            //this will probably work in your IDE, but not from a JAR
            file = new File(res.getFile());
        }

        if (file != null && !file.exists()) {
            throw new RuntimeException("Error: File " + file + " not found!");
        }
    }

    public void deleteTemporalDll(){
        String destFile = System.getProperty("user.dir") + File.separator + "rxtxSerial.dll";
        File dllFile = new File(destFile);
        try {
            dllFile.delete();
            if(dllFile.delete()){
                System.out.println(dllFile.getName() + " is deleted!");
            }else{
                System.out.println("Delete operation is failed.");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

//    public void listFiles() {
//        File folder = new File("/");
//        File[] listOfFiles = folder.listFiles();
//
//        for (int i = 0; i < listOfFiles.length; i++) {
//            if (listOfFiles[i].isFile()) {
//                System.out.println("File " + listOfFiles[i].getName());
//            } else if (listOfFiles[i].isDirectory()) {
//                System.out.println("Directory " + listOfFiles[i].getName());
//            }
//        }
//    }

//    public void addLibraryPath(){
//        String tmpdir = System.getProperty("java.io.tmpdir");
////        String srcFile = tmpdir + "SendSMS" + java.io.File.separator + "rxtxSerial64.dll";
////        String destFile = tmpdir + "rxtxSerial.dll";
//        System.setProperty( "java.library.path", "tmpdir" );
//        try {
//            Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
//            fieldSysPath.setAccessible( true );
//            fieldSysPath.set( null, null );
//        } catch(IllegalAccessException e){
//            e.printStackTrace();
//        } catch(NoSuchFieldException e){
//            e.printStackTrace();
//        }
//    }

}


