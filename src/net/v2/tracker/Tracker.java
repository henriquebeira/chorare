/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.v2.tracker;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.v2.start.Main;

/**
 *
 * @author User
 */
public class Tracker extends Thread {

    private Main main;
    private ServerSocket serverSocket;
    private MulticastSocket multiSocket;
    private Repeater repeater;

    public Tracker(Main main) {
        System.out.println("Starting Tracker");
        try {
            this.main = main;

            serverSocket = new ServerSocket(0, 0, InetAddress.getLocalHost());

            multiSocket = new MulticastSocket(6789);
            multiSocket.joinGroup(main.getMulticastAddress());

            repeater = new Repeater("I`mTracker:" + serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort(),
                    multiSocket, main.getMulticastAddress());

            start();
        } catch (IOException ex) {
            System.err.println("Tracker - Falha ao iniciar Tracker: " + ex.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (!interrupted()) {
                Socket input = serverSocket.accept();

                DataInputStream dIn = new DataInputStream(input.getInputStream());

                String request = dIn.readUTF();

                if (request.equals(Main.SEARCH_UTF)) {
                    new AnswerSearch(main, input, dIn);
                }

                if (request.equals(Main.LIST_UTF)) {
                    new ReceiveList(main, input, dIn);
                }
            }
        } catch (IOException ex) {
            System.err.println("Tracker#run - IO: " + ex.getMessage());
        }

        repeater.interrupt();
    }
}

class Repeater extends Thread {

    String message;
    MulticastSocket socket;
    InetAddress sendTo;

    public Repeater(String message, MulticastSocket socket, InetAddress sendTo) {
        this.message = message;
        this.socket = socket;
        this.sendTo = sendTo;

        start();
    }

    @Override
    public void run() {
        if (socket != null) {
            if (socket.isBound() && !socket.isClosed()) {
                byte[] data = (message + ":").getBytes();

                DatagramPacket pack = new DatagramPacket(data, data.length, sendTo, 6789);

                while (!isInterrupted()) {
                    try {
                        socket.send(pack);
                    } catch (IOException ex) {
                        System.err.println("Tracker:Repeater - Can not send message: " + ex.getMessage());
                    }

                    try {
                        sleep(500);
                    } catch (InterruptedException ex) {
                    }
                }
            } else {
                System.out.println(" no ...");
            }
        }
    }
}