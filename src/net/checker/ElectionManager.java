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
            socket = new MulticastSocket();
            socket.joinGroup(group);

            startTime = Calendar.getInstance().getTimeInMillis();

            while (!endElection()) {
                byte[] buffer = new byte[1000];
                DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                socket.receive(messageIn);
                System.out.println("Porta " + numeroPortaPasta + " recebeu: " + new String(messageIn.getData()));

                //TODO não permitir multiplos votos do mesmo peer
                //A mensagem recebida é armazenada em Voto
                String mensagem = new String(messageIn.getData());
                String[] parts = mensagem.split(";");
                int porta = Integer.valueOf(parts[0]); // processo
                int voto = Integer.valueOf(parts[1]); // porta

                if (voto == -1) {
                    //TODO set tracker

                    InetAddress address = InetAddress.getByName(parts[2]);

                    checker.getMain().setTrackerAddress(address);

                } else {
                    votacao.add(new Voto(porta, voto));
                }
            }

            checker.setWaitingMoreParticipants(false);

            // Imprime qual Processo será o Tracker
            Voto vencedor = quemVenceu();
            System.out.println("Vencedor " + vencedor.getPorta() + " - " + vencedor.getVoto());

            // Prepara o vencedor para receber lista de outros peers
            if (vencedor.getPorta() == numeroPortaPasta) { // Se este processo for o vencedor
                    Tracker trackerThread = new Tracker(checker.getMain());
                    checker.getMain().setAmITracker(true);
                    trackerThread.start();
            }

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
        Voto peerVencedor = new Voto(0, 0);
        for (int i = 0; i < votacao.size(); i++) {
            if (votacao.get(i).getVoto() > peerVencedor.getVoto()) {
                peerVencedor = votacao.get(i);
            }
        }
        return peerVencedor;
    }
}
