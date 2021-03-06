/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package chorare_prototipo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe que basicamente recebe as mensagens multicast.
 * Também realiza a eleição do Tracker, prepara o Tracker para receber a lista de arquivos dos peers.
 * E, enfim, prepara o Tracker para receber requisições.
 * 
 * @author Henrique
 */
public class RecebeMulticast implements Runnable{

    private final int numeroPortaPasta, numeroDeProcessos;
    private final ArrayList<Voto> votacao;
    private final String caminhoDoDiretorio;
    private final Janela janela;
    private boolean repetido = false;

    /**
     * Construtora da classe, preparando a eleição posteriormente feita.
     * 
     * @param numPorta Identificação do Processo, que também é a identificação da pasta.
     * @param caminhoDaPasta Caminho raíz de todos os Processos.
     */
    RecebeMulticast(int numPorta, String caminhoDaPasta, Janela janela, int numProcessos) {
        this.votacao = new ArrayList<>();
        numeroPortaPasta = numPorta;
        this.caminhoDoDiretorio = caminhoDaPasta;
        this.janela = janela;
        numeroDeProcessos = numProcessos;
        janela.setTitle(String.valueOf(numPorta));
        janela.bloqueioBotaoBusca(false);
    }
    
    /**
     * Recebimento de mensagens multicast, e armazenando-as para a eleição do Tracker.
     * Após eleito, o Tracker recebe mensagens sobre quais arquivos cada peer possui.
     * Inicialização do Tracker para receber requisições dos peers.
     * 
     */
    @Override
    public void run() {
        MulticastSocket socket = null;
        try {
            InetAddress group = InetAddress.getByName("228.5.6.7");
            socket = new MulticastSocket(6789);
            socket.joinGroup(group);
            while (votacao.size() < numeroDeProcessos) {
                byte[] buffer = new byte[1000];
                DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                socket.receive(messageIn);
                janela.setjLog("Porta "+numeroPortaPasta+" recebeu: " + new String(messageIn.getData()));
                
                //A mensagem recebida é armazenada em Voto
                String mensagem = new String(messageIn.getData());
                String[] parts = mensagem.split(";");
                int porta = Integer.valueOf(parts[0]); // processo
                int voto = Integer.valueOf(parts[1]); // porta
                
                //É o 1º voto? adiciona...
                if(votacao.isEmpty() && numeroPortaPasta == porta){
                    votacao.add(new Voto(porta, voto));
                }
                
                //Verifica se o processo da mensagem recebida já está na votação
                for (int i = 0; i < votacao.size(); i++) {
                    if (votacao.get(i).getPorta() == porta) {
                        repetido = true;
                        break;
                    }
                }
                
                //Adiciona o voto do processo caso não esteja na votação
                if (repetido == false){
                    votacao.add(new Voto(porta, voto));
                } else {
                    repetido = false;
                }
            }
            
            // Eleição do tracker
            Voto vencedor = quemVenceu();
            janela.setjLog("Vencedor "+vencedor.getPorta()+" - "+vencedor.getVoto());
            
            // Prepara o vencedor para receber lista de outros peers
            if (vencedor.getPorta() == numeroPortaPasta){
                janela.setTitle(janela.getTitle() + " - Tracker");
                Thread thread_recebe_lista = new Thread(new TCP_Recebe_Lista(caminhoDoDiretorio, numeroPortaPasta));
                thread_recebe_lista.start();
            }
            
            // Todos os peers enviam seus arquivos para o tracker
            Thread.sleep(10000);
            Thread thread4 = new Thread(new TCP_Envia_Lista(caminhoDoDiretorio, vencedor.getPorta(), numeroPortaPasta, janela));
            thread4.start();
            
            // Preparar tracker para responder requisições de quem tem um determinado arquivo
            if (vencedor.getPorta() == numeroPortaPasta) {
                Thread thread5 = new Thread(new TCP_Server_Busca(caminhoDoDiretorio, numeroPortaPasta, janela));
                thread5.start();
            }
            
            // Preparar peers para realizar requisições
            Thread.sleep(5000);
            Thread thread5 = new Thread(new TCP_Client_Busca(caminhoDoDiretorio, vencedor.getPorta(), numeroPortaPasta, janela));
            thread5.start();
            
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } catch (InterruptedException ex) {
            Logger.getLogger(RecebeMulticast.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
    
    /**
     * Método para processar a eleição para Tracker.
     * 
     * @return Retorna quem vencer a eleição, i.e. qual Processo tem o voto de maior número.
     */
    // Retorna o vencedor da eleição para Tracker
    public Voto quemVenceu (){
        Voto peerVencedor = new Voto(0, 0);
        for(int i=0 ; i<votacao.size() ; i++){
            if(votacao.get(i).getVoto() > peerVencedor.getVoto()){
                peerVencedor = votacao.get(i);
            }
        }
        return peerVencedor;
    }
    
}

