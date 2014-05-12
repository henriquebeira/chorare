/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chorare_pacote;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe para o envio de mensagens Multicast.
 * 
 * @author Henrique
 */
public class EnvioMulticast implements Runnable {

    private final int tempoEspera, portaPasta;

    /**
     * Construtora da classe.
     * 
     * @param tempo Intervalo de tempo entre os envios de mensagens.
     * @param porta Identificação do Processo.
     */
    EnvioMulticast(int tempo, int porta) {
        this.tempoEspera = tempo;
        this.portaPasta = porta;
    }

    /**
     * Envio de mensagens Multicast, contendo a identificação do Processo e o voto da eleição para Tracker.
     */
    @Override
    public void run() {
        // args give message contents and destination multicast group (e.g. "228.5.6.7")
        MulticastSocket s = null;
        try {
            InetAddress group = InetAddress.getByName("228.5.6.7");
            s = new MulticastSocket(6789);
            s.joinGroup(group);
            long voto = (long) (0 + Math.random() * 10000);
            byte[] mensagem = (portaPasta + ";" + voto + ";").getBytes();

            // Envia mensagens...
            for (int i = 0; i < 4; i++) {		
                DatagramPacket messageOut = new DatagramPacket(mensagem, mensagem.length, group, 6789);
                s.send(messageOut);
                Thread.sleep(tempoEspera);
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

}
