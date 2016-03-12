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
public class SerialSender {
    SerialPort serialPort;
    OutputStream out;

    void disconnect(){
        try{
            out.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        serialPort.close();
    }

    void send(String msg){
        try{
            char[] charArray = msg.toCharArray();
            for(int i=0;i<charArray.length;i++){
                out.write(charArray[i]);
            }
            out.write('\r');
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void connect(String portName, int baudrate) throws Exception,PortAlreadyUsedException {
        CommPortIdentifier portIdentifier = CommPortIdentifier
                .getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
            throw new PortAlreadyUsedException();
        } else {
            int timeout = 2000;
            CommPort commPort = portIdentifier.open(this.getClass().getName(), timeout);

            if (commPort instanceof SerialPort) {
                serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(baudrate,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                out = serialPort.getOutputStream();
            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }

    class PortAlreadyUsedException extends Throwable{
    }


}
