/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.start;

import java.io.File;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Scanner;
import net.checker.StatusChecker;
import net.client.Client;
import net.gui.GUI;

/**
 * Classe de inicialização do sistema.
 *
 * @author Henrique
 */
public class Main {

    private String nickName;
    private StatusChecker checker;
    private GUI gui;
    private Client client;
    private InetAddress trackerAddress;
    private boolean amITracker = false;
    private int trackerPort;

    private final File defaultDiretory;
    private int listPort;

    /**
     * Construtora da classe.
     *
     * @param nickName Nome inserido pelo usuário e que será o nome do diretório
     * do mesmo.
     */
    public Main(String nickName) {
        this.nickName = nickName;

        defaultDiretory = new File(File.separator + nickName);
    }

    /**
     * Método recebe o nome do usuário, inicializa a votação e a interface
     * gráfica.
     *
     * @param args Nome do usuário inserido no sistema.
     */
    public static void main(String... args) {
        Main thisM;

        if (args.length > 0) { // Se recebeu string de parâmetro

            thisM = new Main(args[0]); // A string será o nickName
        } else { // Se não recebeu nada
            String nick;

            Scanner s = new Scanner(System.in); // Le uma string do console

            nick = s.next();

            thisM = new Main(nick); // A string será o nickName
        }

        thisM.checker = new StatusChecker(thisM); // Inicia a Votação/Escolha Tracker
        thisM.checker.start();

        thisM.gui = new GUI(thisM); // Inicializa a GUI.
    }

    /**
     * Método para retornar o nome de usuário.
     * @return Nome de usuário.
     */
    public String getNickName() {
        return nickName;
    }

    /**
     * Método para retornar o objeto que checa se o Tracker está ativo.
     * @return Objeto StatusChecker.
     */
    public StatusChecker getChecker() {
        return checker;
    }

    /**
     * Método para retornar o objeto da interface gráfica.
     * @return Objeto GUI.
     */
    public GUI getGui() {
        return gui;
    }

    /**
     * Mátodo para retornar o endereço do Tracker.
     * @return Endereço do Tracker.
     */
    public InetAddress getTrackerAddress() {
        return trackerAddress;
    }

    public void setTrackerAddress(InetAddress trackerAddres) {
        this.trackerAddress = trackerAddres;
    }

    /**
     * Método para retornar o caminho raíz do diretório.
     * @return Caminho do diretório.
     */
    public File getDefaultDiretory() {
        return defaultDiretory;
    }

    public boolean isAmITracker() {
        return amITracker;
    }

    public void setAmITracker(boolean amITracker) {
        this.amITracker = amITracker;
    }

    public int getTrackerPort() {
        return trackerPort;
    }

    public void setTrackerPort(int trackerPort) {
        this.trackerPort = trackerPort;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setListPort(int port) {
        this.listPort = port;
    }

    public int getListPort() {
        return listPort;
    }
}
