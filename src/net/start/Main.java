/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.start;

import java.io.File;
import java.net.InetAddress;
import java.util.Scanner;
import net.checker.StatusChecker;
import net.client.Client;
import net.gui.GUI;

/**
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

    public Main(String nickName) {
        this.nickName = nickName;
        
        defaultDiretory = new File(File.separator + nickName);
    }
    
    public static void main(String ... args){
        Main thisM;
        
        if(args.length > 0){ // Se recebeu string de parâmetro
        
         thisM = new Main(args[0]); // A string será o nickName
        }else{ // Se não recebeu nada
            String nick;
            
            Scanner s = new Scanner(System.in); // Le uma string do console
            
            nick = s.next(); 
            
            thisM = new Main(nick); // A string será o nickName
        }
        
        thisM.checker = new StatusChecker(thisM); // Inicia a Votação/Escolha Tracker
        thisM.checker.start();
        
        thisM.gui = new GUI(thisM); // Inicializa a GUI.
    }

    public String getNickName() {
        return nickName;
    }

    public StatusChecker getChecker() {
        return checker;
    }

    public GUI getGui() {
        return gui;
    }

    public InetAddress getTrackerAddress() {
        return trackerAddress;
    }

    public void setTrackerAddress(InetAddress trackerAddres) {
        this.trackerAddress = trackerAddres;
    }

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
}
