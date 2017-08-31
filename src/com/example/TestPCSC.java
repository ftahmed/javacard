package com.example;

import java.util.List;
import java.util.Scanner;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import javax.xml.bind.DatatypeConverter;

import static com.example.HexUtils.hexStringToByteArray;

public class TestPCSC {

    public static void main(String[] args) throws CardException {

        TerminalFactory tf = TerminalFactory.getDefault();
        List< CardTerminal> terminals = tf.terminals().list();
        System.out.println("Available Readers:");
        System.out.println(terminals + "\n");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Which reader do you want to send your commands to? (0 or 1 or ...): ");
        String input = scanner.nextLine();
        int readerNum = Integer.parseInt(input);
        CardTerminal cardTerminal = (CardTerminal) terminals.get(readerNum);
        Card connection = cardTerminal.connect("DIRECT");
        CardChannel cardChannel = connection.getBasicChannel();

        System.out.println("Write your commands in Hex form, without '0x' or Space charaters.");
        System.out.println("\n---------------------------------------------------");
        System.out.println("Pseudo-APDU Mode:");
        System.out.println("---------------------------------------------------");
        while (true) {
            System.out.println("Pseudo-APDU command: (Enter 0 to send APDU command)");
            // e.g. FF 82 20 05 06 FF FF FF FF FF FF [compact: FF82200506FFFFFFFFFFFF
            String cmd = scanner.nextLine();
            if (cmd.equals("0")) {
                break;
            }
            System.out.println("Command  : " + cmd);
            byte[] cmdArray = hexStringToByteArray(cmd);
            byte[] resp = connection.transmitControlCommand(CONTROL_CODE(), cmdArray);
            String hex = DatatypeConverter.printHexBinary(resp);
            System.out.println("Response : " + hex + "\n");
        }

        System.out.println("\n---------------------------------------------------");
        System.out.println("APDU Mode:");
        System.out.println("---------------------------------------------------");

        while (true) {
            System.out.println("APDU command: (Enter 0 to exit)");
            String cmd = scanner.nextLine();
            if (cmd.equals("0")) {
                break;
            }
            System.out.println("Command  : " + cmd);
            byte[] cmdArray = hexStringToByteArray(cmd);
            ResponseAPDU resp = cardChannel.transmit(new CommandAPDU(cmdArray));
            byte[] respB = resp.getBytes();
            String hex = DatatypeConverter.printHexBinary(respB);
            System.out.println("Response : " + hex + "\n");
        }

        connection.disconnect(true);

    }

    public static int CONTROL_CODE() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.indexOf("windows") > -1) {
            /* Value used by both MS' CCID driver and SpringCard's CCID driver */
            return (0x31 << 16 | 3500 << 2);
        } else {
            /* Value used by PCSC-Lite */
            return 0x42000000 + 1;
        }

    }

}