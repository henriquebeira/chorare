/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.checker;

import chorare_pacote.Voto;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.client.Client;
import net.tracker.Tracker;

/**
 * Classe para o gerenciamento de uma eleição.
 * 
 * @author Henriques
 */
public class ElectionManager extends Thread {

    private final ArrayList<Voto> votacao;
    private StatusChecker checker;
    private int numeroPortaPasta;
    private Long startTime;
    private String caminhoDoDiretorio;

    /**
     * Construtora da classe.
     * 
     * @param check Recebe o StatusChecker de um Processo.
     */
    public ElectionManager(StatusChecker check) {
        this.votacao = new ArrayList<>();
        checker = check;
    }

    /**
     * Método para indicar o fim de uma eleição.
     * 
     * @return True se há 4 ou mais Processos para a eleição, ou após 10 segundos com 2 Processos.
     */
    private boolean endElection() {
        if (votacao.size() >= 4) {
            return true;
        }

        if (Calendar.getInstance().getTimeInMillis() - startTime > 10000) {
            if (votacao.size() > 1) {
                return true;
            }
        }

        return false;
    }

    /**
     * Método que realiza a eleição, recebendo os votos, definindo o Tracker e os demais Processos (Clientes).
     * TODO?
     */
    @Override
    public void run() {
        MulticastSocket socket = null;

        try {
            InetAddress group = InetAddress.getByName("228.5.6.7");
            socket = new MulticastSocket(6789);
            socket.joinGroup(group);

            startTime = Calendar.getInstance().getTimeInMillis();

            while (!endElection()) {
                byte[] buffer = new byte[1000];
                DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                socket.receive(messageIn);
//                System.out.println("Porta " + numeroPortaPasta + " recebeu: " + new String(messageIn.getData()));

                //TODO não permitir multiplos votos do mesmo peer
                //A mensagem recebida é armazenada em Voto
                String mensagem = new String(messageIn.getData());
                String[] parts = mensagem.split(";");
                String nick = parts[0]; // processo
                int voto = Integer.valueOf(parts[1]); // porta

//                System.out.println("Recebeu voto de: " + nick + "  com o valor: " + voto);

                if (voto == -1) {
                    //TODO set tracker

                    InetAddress address = InetAddress.getByName(parts[2]);

                    checker.getMain().setTrackerAddress(address);

                } else {
                    boolean alreadyIn = false;
                    for (Voto v : votacao) {
                        if (v.getNick().equals(nick)) {
                            alreadyIn = true;
                        }
                    }
                    if (!alreadyIn) {
                        votacao.add(new Voto(nick, voto));
                    }
                }
            }

            checker.setWaitingMoreParticipants(false);

            // Imprime qual Processo será o Tracker
            Voto vencedor = quemVenceu();
            System.out.println("Vencedor " + vencedor.getNick() + " - " + vencedor.getVoto());

            InetAddress addres = InetAddress.getByName("228.5.6.7");
            MulticastSocket msocket = new MulticastSocket(6790);

            msocket.joinGroup(addres);

            // Prepara o vencedor para receber lista de outros peers
            if (vencedor.getNick().equals(checker.getMain().getNickName())) { // Se este processo for o vencedor
                // Inicializa o tracker
                //Thread thread_recebe_lista = new Thread(new TCP_Recebe_Lista(caminhoDoDiretorio, numeroPortaPasta));
                //thread_recebe_lista.start();
                try {
                    Tracker trackerThread = new Tracker(checker.getMain());
                    checker.getMain().setAmITracker(true);
                    checker.getMain().setTrackerPort(trackerThread.getListenSocket().getLocalPort());

                    String partMessage = "" + trackerThread.getListenSocket().getInetAddress().getAddress()[0] +"." + trackerThread.getListenSocket().getInetAddress().getAddress()[1] +"."+ trackerThread.getListenSocket().getInetAddress().getAddress()[2] +"." + trackerThread.getListenSocket().getInetAddress().getAddress()[3];
                    
                    byte[] message = (partMessage + ";" + trackerThread.getListenSocket().getLocalPort() + ";" + trackerThread.getListPort() + ";").getBytes();
                    
                    System.out.println("Enviando: " + new String(message));
                    
                    DatagramPacket pack = new DatagramPacket(message, message.length, addres, 6790);
                    try {
                        sleep(3000);
                    } catch (InterruptedException ex) {
                    }
                    msocket.send(pack);

                    trackerThread.start();
                } catch (IOException ex) {
                }
            } else {


                DatagramPacket pack = new DatagramPacket(new byte[1000], 1000);

                msocket.receive(pack);

                String[] data = new String(pack.getData()).split(";");
                
                System.out.println("Client receive: " + data[0] + "  -  " + data[1]);
                
//                String[] ip = data[0].split(".");
                
//                InetAddress trackerAddress = InetAddress.getByAddress(new byte[]{Byte.parseByte(ip[0]),Byte.parseByte(ip[1]),Byte.parseByte(ip[2]),Byte.parseByte(ip[3])});
                int port = Integer.parseInt(data[1]);
                
//                checker.getMain().setTrackerAddress(InetAddress.getLocalHost());
                checker.getMain().setTrackerPort(port);
                checker.getMain().setListPort(port);
            }
            
            checker.getMain().setTrackerAddress(InetAddress.getLocalHost());
                    

            Client clientThread = new Client(checker.getMain());
            clientThread.start();

            checker.getMain().getGui().setHasTracker(true);
            checker.getMain().getGui().setClientThread(clientThread);

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    /**
     * Método para definir o vencedor da eleição.
     * 
     * @return Processo vencedor, que tem o maior voto.
     */
    public Voto quemVenceu() {
        Voto peerVencedor = new Voto("", 0);
        for (int i = 0; i < votacao.size(); i++) {
            if (votacao.get(i).getVoto() > peerVencedor.getVoto()) {
                peerVencedor = votacao.get(i);
            }
        }
        return peerVencedor;
    }
}
