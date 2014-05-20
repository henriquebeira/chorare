/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.v2.start;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import net.v2.gui.GUI;
import net.v2.checker.Checker;
import net.v2.client.Client;
import net.v2.tracker.Tracker;

/**
 * Classe principal de um processo.
 * 
 * @author Henriques
 */
public class Main {

    public static final String SEARCH_UTF = "SEARCH";
    public static final String LIST_UTF = "LIST";
    private String nickName;
    private final File filesFolder;
    private final File configFolder;
    private final File trackerFolder;
    private InetAddress trackerAddress;
    private int trackerPort;
    private InetAddress multicastAddress;
    private InetAddress thisAddress;
    private int clientPort;
    private final GUI gui;
    private final Checker checker;
    private final Client client;
    private Tracker auxTracker;

     /**
     * Criação dos diretórios "controle" e "track".
     * Inicialização do controle de eleição, GUI, e do client do processo.
     * 
     * @param nickName Nome do diretório do processo
     * @throws UnknownHostException Endereço inexistente.
     */
    private Main(String nickName) throws UnknownHostException {
        this.nickName = nickName;

        filesFolder = new File(nickName);
        configFolder = new File(filesFolder.getPath() + File.separator + "controle");
        trackerFolder = new File(configFolder.getPath() + File.separator + "track");

        if (!filesFolder.exists()) {
            filesFolder.mkdir();
        }

        if (!configFolder.exists()) {
            configFolder.mkdir();
        }

        if (!trackerFolder.exists()) {
            trackerFolder.mkdir();
        }

        for (File f : configFolder.listFiles()) {
            if (!f.getName().equals("track")) {
                f.delete();
            }
        }

        for (File f : trackerFolder.listFiles()) {
            f.delete();
        }

        thisAddress = InetAddress.getLocalHost();

        multicastAddress = InetAddress.getByName("228.5.6.7");

        gui = new GUI(this);
        checker = new Checker(this);
        client = new Client(this);
    }

    public String getNickName() {
        return nickName;
    }

    public File getFilesFolder() {
        return filesFolder;
    }

    public File getConfigFolder() {
        return configFolder;
    }

    public File getTrackerFolder() {
        return trackerFolder;
    }

    public InetAddress getMulticastAddress() {
        return multicastAddress;
    }

    public InetAddress getThisAddress() {
        return thisAddress;
    }

    public InetAddress getTrackerAddress() {
        return trackerAddress;
    }

    /**
     * Método para indicar o endereço do Tracker.
     * 
     * @param trackerAddress Endereço do Tracker.
     */
    public void setTrackerAddress(InetAddress trackerAddress) {
        this.trackerAddress = trackerAddress;

        gui.setMode((trackerAddress == null ? GUI.MODE_WAIT : GUI.MODE_RUN));

        if (trackerAddress == null) {
            if (auxTracker != null) {
                auxTracker.interrupt();
                auxTracker = null;
            }
        } else {
            client.sendFileList();
        }
    }

    public int getTrackerPort() {
        return trackerPort;
    }

    public void setTrackerPort(int trackerPort) {
        this.trackerPort = trackerPort;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public Client getClient() {
        return client;
    }

    public GUI getGui() {
        return gui;
    }

    /**
     * Inicialização do processo.
     * Recebimento do nickname.
     * 
     * @param args 
     */
    public static void main(String... args) {
        Main thisM;

        try {
            if (args.length > 0) {

                thisM = new Main(args[0]);
            } else {
                String nick;

                Scanner s = new Scanner(System.in);

                nick = s.next();

                thisM = new Main(nick);
            }
        } catch (UnknownHostException ex) {
            System.err.println("Falha no Main - " + ex.getMessage());
        }
    }

    public void setAuxTracker(Tracker auxTracker) {
        this.auxTracker = auxTracker;
    }

    public void killThreads() {
        client.interrupt();
        checker.interrupt();
        if (auxTracker != null) {
            auxTracker.interrupt();
        }
    }
}
