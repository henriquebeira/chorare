/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.checker;

import chorare_pacote.EnvioMulticast;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.start.Main;
import net.tracker.Tracker;

/**
 * Thread responsável por iniciar a eleição. Fica enviando sua mensagem até que
 * lhe digam que a eleição foi concluída.
 *
 * @author Henrique
 */
public class StatusChecker extends Thread {

    private static final int tempoSleep = 1000; // Tempo de espera entre envios de mensagem.

    private Main main;

    private boolean waitingMoreparticipants = true;
    private boolean stillAlive = true;

    public StatusChecker(Main main) {
        this.main = main;
    }

    @Override
    public void run() {
        while (stillAlive) {
            if (main.getTrackerAddress() == null) { // Se não existe um Tracker ativo
                sendVote(); // Inicia votação
            } else { // Se já houver um Tracker
                listenTracker(); // Fica ouvindo se ele continua ativo.
            }
        }
    }

    private void sendVote() {
        new ElectionManager(this).start(); // Inicializa o gerenciador de votação

        MulticastSocket s = null;
        try {
            InetAddress group = InetAddress.getByName("228.5.6.7");
            s = new MulticastSocket();

            int porta = s.getLocalPort();

            s.joinGroup(group);
            long voto = (long) (0 + Math.random() * 10000); // Sorteia um voto
            byte[] mensagem = (porta + ";" + voto + ";").getBytes();

            // Envia mensagens...
            while (waitingMoreparticipants) { // Envia o voto até a eleição acabar
                DatagramPacket messageOut = new DatagramPacket(mensagem, mensagem.length, group, 6789);
                s.send(messageOut);
                
                Thread.sleep(tempoSleep);
            }

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } catch (InterruptedException ex) {
            Logger.getLogger(EnvioMulticast.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (s != null) {
                s.close();
            }
        }
    }

    private void listenTracker() {
        MulticastSocket s = null;
        try {
            InetAddress group = InetAddress.getByName("228.5.6.7");
            s = new MulticastSocket();
            s.setSoTimeout(tempoSleep * 2);

            int porta = s.getLocalPort();

            s.joinGroup(group);
            byte[] mensagem;

            // Recebe mensagens...
            while (true) {
                mensagem = new byte[Tracker.TrackerOk.getBytes().length]; // Limpa buffer
                DatagramPacket messageIn = new DatagramPacket(mensagem, mensagem.length);
                s.receive(messageIn); // Recebe a mensagem

                String message = new String(messageIn.getData());
                
                if (!message.equals(Tracker.TrackerOk)) { // Se a mensagem não estiver correta
                    main.setTrackerAddress(null); // Marca o tracker como inexistente
                    
                    main.setAmITracker(false);
                    
                    break; // Encerra método
                }
            }

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (s != null) {
                s.close();
            }
        }
    }

    public void setWaitingMoreParticipants(boolean wait) {
        waitingMoreparticipants = wait;
    }

    public void setStillAlive(boolean stillAlive) {
        this.stillAlive = stillAlive;
    }

    public Main getMain() {
        return main;
    }

}
