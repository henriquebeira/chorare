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

/**
 * Thread responsável por iniciar a eleição.
 * Fica enviando sua mensagem até que lhe digam que a eleição foi concluída.
 * @author Henrique
 */
public class StatusChecker extends Thread{
    private static final int tempoSleep = 1000; // Tempo de espera entre envios de mensagem.
    
    private Main main;
    
    private boolean waitingMoreparticipants = true;

    public StatusChecker(Main main) {
        this.main = main;
    }
    
    @Override
    public void run() {
        new ElectionManager(this).start();
        
        MulticastSocket s = null;
        try {
            InetAddress group = InetAddress.getByName("228.5.6.7");
            s = new MulticastSocket();
            
            int porta = s.getLocalPort();
            
            s.joinGroup(group);
            long voto = (long) (0 + Math.random() * 10000);
            byte[] mensagem = (porta + ";" + voto + ";").getBytes();

            // Envia mensagens...
            while(waitingMoreparticipants) {
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
    
    public void setWaitingMoreParticipants(boolean wait){
        waitingMoreparticipants = wait;
    }

    public Main getMain() {
        return main;
    }
    
    
}
