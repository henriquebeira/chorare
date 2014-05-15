/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.tracker;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.start.Main;

/**
 * Classe que o Tracker usa para iniciar a conexão que receberá as requisições
 * de busca dos peers.
 *
 * @author Henriques
 */
public class Tracker extends Thread {

    private Main main;

    private final GeradorAssinatura gerarParChaves;
    int numeroPortaPasta;
    String caminhoDaPasta;

    private final TrackerStillAlive stillAlive;
    private final TrackerListReceiver receiveList;

    private ServerSocket listenSocket;

    public static final String TrackerOk = "TrackerIsOK!!";

    /**
     * Construtora da classe. Geração do par de chaves privada e pública.
     *
     * @param caminhoDaPasta
     * @param porta
     */
    public Tracker(Main main) throws IOException {
        this.main = main;
        listenSocket = new ServerSocket();
        caminhoDaPasta = main.getDefaultDiretory().getPath() + File.separator + main.getNickName();

        gerarParChaves = new GeradorAssinatura(caminhoDaPasta);

        stillAlive = new TrackerStillAlive(listenSocket.getInetAddress(), listenSocket.getLocalPort());
        stillAlive.start();

        receiveList = new TrackerListReceiver(caminhoDaPasta);
        receiveList.start();

    }

    /**
     * Porta final 2 do Tracker é utilizada para receber requisições de buscas
     * no tracker, e.g. 8012 (quando peer vencedor for 8010).
     *
     */
    @Override
    public void run() {
        try {
            gerarParChaves.gerar();
            while (true) {
                Socket clientSocket = listenSocket.accept();
                SearchRequest c = new SearchRequest(clientSocket, caminhoDaPasta);
            }
        } catch (IOException e) {
        }

        kill();
    }

    private void kill() {
        stillAlive.setHasToDie(true);
        receiveList.setHasToDie(true);
    }

    public ServerSocket getListenSocket() {
        return listenSocket;
    }
}
