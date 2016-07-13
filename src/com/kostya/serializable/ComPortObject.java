package com.kostya.serializable;

/**
 * on 11.07.2016.
 */

import com.felhr.usbserial.UsbSerialInterface;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Класс для настроек COM порта (USB).
 */
public class ComPortObject implements Serializable{
    //private final SystemTable systemTable;
    int speed;
    int dataBits;
    int stopBits;
    int parity;
    int flowControl;

    public static final HashMap<String, Integer> usbProperties = new LinkedHashMap<>();
    static {
        usbProperties.put("5", UsbSerialInterface.DATA_BITS_5);
        usbProperties.put("6", UsbSerialInterface.DATA_BITS_6);
        usbProperties.put("7", UsbSerialInterface.DATA_BITS_7 );
        usbProperties.put("8", UsbSerialInterface.DATA_BITS_8);

        usbProperties.put("1", UsbSerialInterface.STOP_BITS_1);
        usbProperties.put("2", UsbSerialInterface.STOP_BITS_2);
        usbProperties.put("1.5", UsbSerialInterface.STOP_BITS_15);

        usbProperties.put("even", UsbSerialInterface.PARITY_EVEN);
        usbProperties.put("mark", UsbSerialInterface.PARITY_MARK);
        usbProperties.put("none", UsbSerialInterface.PARITY_NONE);
        usbProperties.put("odd", UsbSerialInterface.PARITY_ODD);
        usbProperties.put("space", UsbSerialInterface.PARITY_SPACE);

        usbProperties.put("DSR_DTR", UsbSerialInterface.FLOW_CONTROL_DSR_DTR);
        usbProperties.put("OFF", UsbSerialInterface.FLOW_CONTROL_OFF);
        usbProperties.put("RTS_CTS", UsbSerialInterface.FLOW_CONTROL_RTS_CTS);
        usbProperties.put("XON_XOFF", UsbSerialInterface.FLOW_CONTROL_XON_XOFF);
    }

    /*public ComPortObject(Context context){
        systemTable = new SystemTable(context);
        speed = Integer.valueOf(systemTable.getProperty(SystemTable.Name.SPEED_PORT, "9600"));
        dataBits = usbProperties.get(systemTable.getProperty(SystemTable.Name.FRAME_PORT, "8"));
        stopBits = usbProperties.get(systemTable.getProperty(SystemTable.Name.STOP_BIT, "1"));
        parity = usbProperties.get(systemTable.getProperty(SystemTable.Name.PARITY_BIT, "none"));
        flowControl = usbProperties.get(systemTable.getProperty(SystemTable.Name.FLOW_CONTROL, "OFF"));
    }*/

    public int getSpeed() {return speed;}
    public int getDataBits() {return dataBits;}
    public int getStopBits() {return stopBits;}
    public int getParity() {return parity;}
    public int getFlowControl() {return flowControl;}

    public void setSpeed(int speed) {this.speed = speed;}
    public void setDataBits(int dataBits) {this.dataBits = dataBits;}
    public void setStopBits(int stopBits) {this.stopBits = stopBits;}
    public void setParity(int parity) {this.parity = parity;}
    public void setFlowControl(int flowControl) {this.flowControl = flowControl;}
}