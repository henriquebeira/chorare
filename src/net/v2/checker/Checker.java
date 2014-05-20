/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.v2.checker;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Calendar;
import java.util.HashMap;
import net.v2.start.Main;
import net.v2.tracker.Tracker;

/**
 * Classe para controle da eleição.
 * 
 * @author Henrique
 */
public class Checker extends Thread {

    private Main main;
    private MulticastSocket mSocket;

    /**
     * Construtora da classe.
     * 
     * @param main Classe principal de um processo.
     */    
    public Checker(Main main) {
        this.main = main;

        start();
    }

    /**
     * Método para a inicialização de um grupo Multicast.
     */    
    @Override
    public void run() {
        try {
            mSocket = new MulticastSocket(6789);

            mSocket.joinGroup(main.getMulticastAddress());


            loop();
        } catch (IOException ex) {
        }

    }

    /**
     * Método para a realização de uma eleição, na ausência de um Tracker já definido.
     */    
    private void loop() {
        while (!interrupted()) {
            if (main.getTrackerAddress() != null) {
                listenTracker();
            } else {
//                System.out.println("No tracker ...");
                startElection();
            }
        }
    }

    /**
     * Método para receber o endereço de um Tracker estabelecido. 
     */    
    private void listenTracker() {
        try {
            mSocket.setSoTimeout(2000);

            byte[] buff = new byte[256];
            DatagramPacket pack = new DatagramPacket(buff, buff.length);

            mSocket.receive(pack);

            String[] data = new String(pack.getData()).split(":");

            if (data.length == 4) {
                InetAddress receivedAdd = InetAddress.getByName(data[1]);
                int receivedPort = Integer.parseInt(data[2]);

                if (main.getTrackerAddress().getHostAddress().equals(receivedAdd.getHostAddress())) {
                    if (receivedPort == main.getTrackerPort()) {
                    }
                }
            }


        } catch (SocketTimeoutException ex) {

            main.setTrackerPort(-1);
            main.setTrackerAddress(null);
            
        } catch (SocketException ex) {
            System.err.println("Checker - socket: " + ex.getMessage());
        } catch (IOException ex) {
            System.err.println("Checker - IO: " + ex.getMessage());
        }

//        System.out.println("Tracker failed !");

    }

    /**
     * Método para realizar uma eleição.
     */    
    private void startElection() {
        try {
            SendVote sender = new SendVote(main);
            sender.start();

            HashMap<String, Integer> votesMap = new HashMap<>();

            String winningNick = "";
            Integer winningVote = -1;

            mSocket.setSoTimeout(1000);

            DatagramPacket pack;

            runningTimeStart = Calendar.getInstance().getTimeInMillis();

            while (!endElection(votesMap)) {
                pack = new DatagramPacket(new byte[512], 512);

                mSocket.receive(pack);

                String[] data = new String(pack.getData()).split(":");

                if (data.length == 3) {
                    String nick = data[0];
                    int vote = Integer.parseInt(data[1]);

                    System.out.println(nick + " - " + vote);

                    if (!votesMap.containsKey(nick)) {
                        votesMap.put(nick, vote);

                        System.out.println("Nick added:" + nick);

                        if (vote > winningVote) {
                            winningVote = vote;
                            winningNick = nick;
                        } else {
                            if (vote == winningVote) {
                                if (!nick.equals(winningNick)) {
                                    if (nick.compareTo(winningNick) < 0) {
                                        winningNick = nick;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (data.length == 4) {

                        if (data[0].equals("I`mTracker")) {

                            main.setTrackerPort(Integer.parseInt(data[2]));
                            main.setTrackerAddress(InetAddress.getByName(data[1]));

                            sender.interrupt();

                            return;
                        }
                    }
                }
            }

            sender.interrupt();

            if (main.getNickName().equals(winningNick)) {
                main.setAuxTracker(new Tracker(main));
                System.out.println("Hey I'm the tracker!!");
            }

        } catch (SocketException ex) {
            System.err.println("Election - Soket: " + ex.getMessage());
        } catch (IOException ex) {
            System.err.println("Election - IO: " + ex.getMessage());
        }
    }
    private long runningTimeStart;

    /**
     * Método para terminar uma eleição.
     *
     * @param votesMap Mapeamento nickname - voto.
     * @return True de tiver mais de 3 votos, ou mais de 1 voto passados 10
     * segundos.
     */
    private boolean endElection(HashMap<String, Integer> votesMap) {
        if (votesMap.size() > 3) {
            return true;
        }

        if (votesMap.size() > 1 && (Calendar.getInstance().getTimeInMillis() - runningTimeStart > 10000)) {
            return true;
        }

        return false;
    }
}

/**
 * Classe para envio do voto.
 * 
 * @author Henrique
 */
class SendVote extends Thread {

    String nick;
    Main main;

    /**
     * Construtora da classe.
     * 
     * @param main Classe principal de um processo.
     */    
    public SendVote(Main main) {
        this.nick = main.getNickName();
        this.main = main;
    }

    /**
     * Método para envio dos votos.
     */    
    @Override
    public void run() {
        try {
            MulticastSocket sock = new MulticastSocket(6789);
            sock.joinGroup(main.getMulticastAddress());

            int vote = (int) (Math.random() * 10000);

            byte[] buff = (nick + ":" + vote + ":").getBytes();

            DatagramPacket pack = new DatagramPacket(buff, buff.length, main.getMulticastAddress(), 6789);


            try {
                while (!isInterrupted()) {
                    sock.send(pack);

                    sleep(500);

                }
            } catch (InterruptedException ex) {
            }
        } catch (IOException ex) {
            System.err.println("SendVote - failed to send vote. " + ex.getMessage());
        }
    }
}