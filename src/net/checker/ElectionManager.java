/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.checker;

import chorare_pacote.RecebeMulticast;
import chorare_pacote.TCP_Client_Busca;
import chorare_pacote.TCP_Envia_Lista;
import chorare_pacote.TCP_Recebe_Lista;
import chorare_pacote.TCP_Server_Busca;
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

/**
 *
 * @author Henriques
 */
public class ElectionManager extends Thread {

    private final ArrayList<Voto> votacao;
    private StatusChecker checker;

    private Long startTime;

    public ElectionManager(StatusChecker check) {
        this.votacao = new ArrayList<>();
        checker = check;
    }

    private boolean endElection() {
        if (votacao.size() >= 4) {
//            if (Calendar.getInstance().getTimeInMillis() - startTime > 5000) {
            return true;
//            }
        }

        if (Calendar.getInstance().getTimeInMillis() - startTime > 10000) {
            if (votacao.size() > 1) {
                return true;
            }
        }

        return false;
    }

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
                    
                } else {
                    votacao.add(new Voto(porta, voto));
                }
            }

            checker.setWaitingMoreParticipants(false);

            // Eleição do tracker
            Voto vencedor = quemVenceu();
            System.out.println("Vencedor " + vencedor.getPorta() + " - " + vencedor.getVoto());

            // Prepara o vencedor para receber lista de outros peers
            if (vencedor.getPorta() == numeroPortaPasta) { // Se este processo for o vencedor
                // Inicializa o tracker
                Thread thread_recebe_lista = new Thread(new TCP_Recebe_Lista(caminhoDoDiretorio, numeroPortaPasta));
                thread_recebe_lista.start();
            }

            //Inicializa o Client
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
