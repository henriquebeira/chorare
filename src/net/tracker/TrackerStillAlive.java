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

/**
 * Classe para envio de mensagens que está ativo.
 * 
 * @author a1155997
 */
public class TrackerStillAlive extends Thread {
    private boolean hasToDie = false;
    
    private InetAddress adrress;
    private int port;

    /**
     * Construtora da classe.
     * 
     * @param adrress Endereço do Tracker.
     * @param port Porta do Tracker.
     */
    public TrackerStillAlive(InetAddress adrress, int port) {
        this.adrress = adrress;
        this.port = port;
    }

    /**
     * Envio de mensagens Multicast com o endereço e porta do Tracker.
     */
    @Override
    public void run() {
        try {
            MulticastSocket socket = new MulticastSocket();
            InetAddress group = InetAddress.getByName("228.5.6.7");
            
            socket.joinGroup(group);

            byte[] message = (adrress.getHostAddress() + ";" + port).getBytes();
            DatagramPacket pack = new DatagramPacket(message, message.length);
            
            while (true) {
                if(hasToDie){
                    return;
                }
                socket.send(pack);

                sleep(1000);
            }
        } catch (InterruptedException | IOException ex) {
            
        }
    }

    /**
     * Método para finalizar o envio de mensagens.
     * @param hasToDie True caso deve parar de enviar mensagens.
     */
    public void setHasToDie(boolean hasToDie) {
        this.hasToDie = hasToDie;
    }

}
