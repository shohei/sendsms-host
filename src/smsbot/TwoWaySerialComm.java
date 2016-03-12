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
    Thread t1;
    Thread t2;

    void disconnect() {
        if (serialPort != null) {
            new Thread() {
                @Override
                public void run() {
                    System.out.println("interrupting threads.");
                    t1.interrupt();
                    t2.interrupt();
//                    t1.stop();
//                    t2.stop();
                    System.out.println("done. closing port");
                    serialPort.removeEventListener();
                    serialPort.close();
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

                InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();

                SerialReader serialReader = new SerialReader(in);
                SerialWriter serialWriter = new SerialWriter(out);

                t1 = new Thread(serialReader);
                t2 = new Thread(serialWriter);
                t1.start();
                t2.start();
                System.out.println("Connected to serial port.");
            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }

    public static class SerialReader implements Runnable {

        InputStream in;

        public SerialReader(InputStream in) {
            this.in = in;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                byte[] buffer = new byte[1024];
                int len = -1;
                try {
                    while ((len = this.in.read(buffer)) > -1) {
                        System.out.print(new String(buffer, 0, len));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("reader shutdown");
        }
    }

    public static class SerialWriter implements Runnable {

        OutputStream out;

        public SerialWriter(OutputStream out) {
            this.out = out;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    int c = 0;
                    while ((c = System.in.read()) > -1) {
                        this.out.write(c);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("writer shutdown");
        }
    }
}
