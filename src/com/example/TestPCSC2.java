package com.example;

import java.nio.ByteBuffer;
import java.util.List;
import javax.smartcardio.*;

import static com.example.HexUtils.bytesToHex;

public class TestPCSC2 {
    public static final int NE_ISO_7816_CASE_1 = 0x00;
    public static final int NE_ISO_7816_CASE_2 = 0x100; // 256

    public static void main(String[] args) {
        try {
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();

            int index = 0; // Use the first terminal by default
            if (args.length > 0)
                index = Integer.parseInt(args[0]);
            CardTerminal terminal = terminals.get(index);

            // Display the list of terminals
            System.out.println("Terminals: ");
            for (int i=0; i<terminals.size(); i++) {
                String selected = (i == index) ? "*" : " ";
                System.out.println(String.format("%s %d: %s", selected, i, terminals.get(i)));
            }

            // Connect with the card
            Card card = terminal.connect("*");
//            Card card = terminal.conn)ect("DIRECT");
            System.out.println("Card ATR: " + bytesToHex(card.getATR().getBytes()));
            CardChannel channel = card.getBasicChannel();

            byte[] baReadUID = new byte[5];

            // get UID
            baReadUID = new byte[]{(byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x00}; //FF CA 00 00 00
            System.out.println("UID: " + SendCommand(baReadUID, channel));

            // get ATS
            baReadUID = new byte[]{(byte) 0xFF, (byte) 0xCA, (byte) 0x01, (byte) 0x00, (byte) 0x00}; //FF CA 00 00 00
            System.out.println("ATS: " + SendCommand(baReadUID, channel));

            /*
            // Send Select Applet command
            byte[] aid = {(byte)0xA0, 0x00, 0x00, 0x00, 0x62, 0x03, 0x01, 0x0C, 0x06, 0x01};
            ResponseAPDU answer = channel.transmit(new CommandAPDU(0x00, 0xA4, 0x04, 0x00, aid));
            System.out.println("answer: " + answer.toString());
            */

            // Send test command
//            ResponseAPDU answer = channel.transmit(new CommandAPDU(baReadUID));
            // NOTE: for the ne/Le field 256 is encoded as 0x00
            ResponseAPDU answer = channel.transmit(new CommandAPDU(0xFF, 0xCA, 0x00, 0x00, NE_ISO_7816_CASE_2));
            System.out.println("answer: " + answer.toString());
            byte r[] = answer.getData();
            String data = "";
            for (int i = 0; i<r.length; i++) {
                data += String.format("%02X", r[i]);
            }
            System.out.println("data: " + data);

            // Disconnect the card
            card.disconnect(false);
        } catch(Exception e) {
            System.out.println("Ouch: " + e.toString());
        }
    }

    public static String SendCommand(byte[] cmd, CardChannel channel)
    {
        String response = "";
        byte[] baResp = new byte[258];

        ByteBuffer bufCmd = ByteBuffer.wrap(cmd);
        ByteBuffer bufResp = ByteBuffer.wrap(baResp);

        int output = 0;

        try{
            output = channel.transmit(bufCmd, bufResp);
        }
        catch(CardException ex){
            ex.printStackTrace();
        }

        for (int i = 0; i < output; i++) {
            response += String.format("%02X", baResp[i]);
        }
        return response;
    }

}
