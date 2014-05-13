/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.tracker;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author a1155997
 */
public class TrackerStillAlive extends Thread {
    private boolean hasToDie = false;
    

    @Override
    public void run() {
        try {
            MulticastSocket socket = new MulticastSocket();
            InetAddress group = InetAddress.getByName("228.5.6.7");
            
            socket.joinGroup(group);

            byte[] message = "Tracker is Ok".getBytes();
            DatagramPacket pack = new DatagramPacket(message, message.length);
            
            while (true) {
                if(hasToDie){
                    return;
                }
                socket.send(pack);

                sleep(1000);
            }
        } catch (InterruptedException intEx) {

        } catch (IOException ex) {
        }
    }

    public void setHasToDie(boolean hasToDie) {
        this.hasToDie = hasToDie;
    }

}
