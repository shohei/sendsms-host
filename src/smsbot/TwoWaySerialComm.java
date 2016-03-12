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

    void send(String msg) {
        try {
            char[] charArray = msg.toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                out.write(charArray[i]);
            }
            out.write('\r');
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void disconnect() {
        if (serialPort != null) {
            new Thread() {
                @Override
                public void run() {
                    System.out.println("try disconnection.");
                    System.out.println("Close I/O stream.");
                    try {
                        in.close();
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("Shutdown Reader/Writer.");
                    serialReader.shutdown();
                    serialWriter.shutdown();
                    System.out.write('z');//hack for stop thread
                    System.out.println();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    serialPort.removeEventListener();
                    serialPort.close();
                    System.out.println("Closing port.");
                    System.out.println("disconnected.");
                }
            }.start();
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

                serialPort.enableReceiveTimeout(1000);

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

        public void shutdown() {
            shutdown = true;
        }

        @Override
        public void run() {
            while (!shutdown) {
                byte[] buffer = new byte[1024];
                int len = -1;
                try {
                    while ((len = this.in.read(buffer)) > -1) {
                        System.out.print(new String(buffer, 0, len));
                        if (shutdown) {
                            System.out.println("out while reader");
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
                    while (((c = System.in.read()) > -1)) {
                        this.out.write(c);
                        if (shutdown) {
                            System.out.println("out while writer");
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
