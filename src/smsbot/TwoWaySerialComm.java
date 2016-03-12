package smsbot;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by shohei on 3/12/16.
 */
public class TwoWaySerialComm {

    SerialPort serialPort;
    InputStream in;
    OutputStream out;
    SerialReader serialReader;
    SerialWriter serialWriter;
//    Thread t1;
//    Thread t2;

    void send() {
        try {
            out.write('a');
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    void disconnect() {
//        if (serialPort != null) {
//            new Thread() {
//                @Override
//                public void run() {
//                    System.out.println("try disconnection.");
//                    System.out.println("Close I/O stream.");
//                    try{
//                        in.close();
//                        out.close();
//                    } catch(Exception e){
//                        e.printStackTrace();
//                    }
//                    System.out.println("Shutdown Reader/Writer.");
//                    serialReader.shutdown = true;
//                    serialWriter.shutdown = true;
//                    System.out.println("Closing port.");
//                    serialPort.removeEventListener();
//                    serialPort.close();
//                    System.out.println("disconnected.");
//                }
//            }.start();
//        }
//    }

    void disconnect() {
        if (serialPort != null) {
            System.out.println("try disconnection.");
            System.out.println("Shutdown Reader/Writer.");
            serialReader.shutdown();
            serialWriter.shutdown();
            System.out.println("Closing port.");
            serialPort.removeEventListener();
            serialPort.close();
            System.out.println("disconnected.");
        }
    }

    void connect(String portName, int baudrate) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier
                .getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        } else {
            int timeout = 2000;
            CommPort commPort = portIdentifier.open(this.getClass().getName(), timeout);

            if (commPort instanceof SerialPort) {
                serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(baudrate,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                in = serialPort.getInputStream();
                out = serialPort.getOutputStream();

                serialReader = new SerialReader(in);
                serialWriter = new SerialWriter(out);

                (new Thread(serialReader)).start();
                (new Thread(serialWriter)).start();
                System.out.println("Connected to serial port.");
            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }

    public static class SerialReader implements Runnable {

        InputStream in;
        private volatile boolean shutdown = false;

        public SerialReader(InputStream in) {
            this.in = in;
        }

        public void shutdown(){
            shutdown = true;
        }

        @Override
        public void run() {
            while (!shutdown) {
                byte[] buffer = new byte[1024];
                int len = -1;
                try {
                    while (((len = this.in.read(buffer)) > -1) && !shutdown) {
                        System.out.print(new String(buffer, 0, len));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("out while reader");
        }
    }

    public static class SerialWriter implements Runnable {

        OutputStream out;
        private volatile boolean shutdown = false;

        public SerialWriter(OutputStream out) {
            this.out = out;
        }

        public void shutdown() {
            shutdown = true;
        }

        @Override
        public void run() {
            while (!shutdown) {
                try {
                    int c = 0;
                    while (((c = System.in.read()) > -1) && !shutdown) {
                        this.out.write(c);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("out while writer");
        }
    }
}
