/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import net.client.Client;
import net.start.Main;
import org.jdesktop.swingx.JXBusyLabel;

/**
 * Interface Gráfica do Sistema.
 * @author a1155997
 */
public class GUI extends JFrame{
    private boolean hasTracker;
    
    private Main main;
    private Client clientThread;
    
    private JPanel await;
    private JPanel client;
    
    private byte mode;
    
    private static final Byte MODE_AWAITING = 0;
    private static final Byte MODE_OK = 1;

    /**
     * Construtora da classe.
     * @param main Main de um Processo.
     */
    public GUI(Main main) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(500, 400));
        this.main = main;
        this.setLayout(new BorderLayout());
        
        mode = -1;
        
        hasTracker = false;
        
        await = new JPanel(new BorderLayout());
        
        JXBusyLabel busy = new JXBusyLabel(new Dimension(150, 150));
        
        busy.setBusy(true);
        busy.setDelay(30);
        busy.setHorizontalAlignment(JXBusyLabel.CENTER);
        
        busy.getBusyPainter().setPoints(100);
        busy.getBusyPainter().setTrailLength(10);
        
        await.add(busy);
        
        client = new JPanel();
        
        updateState();
        
        this.setVisible(true);
    }

    /**
     * Método para verificar se há um Tracker.
     * @param hasTracker True se há um Tracker.
     */
    public void setHasTracker(boolean hasTracker) {
        this.hasTracker = hasTracker;
        
        updateState();
    }
    
    /**
     * Método para adicionar um Painel caso não exista um Tracker.
     */
    private void updateState(){
        if(hasTracker){
            trackerOK();
        }else{
            awaitTracker();
        }
    }
    
    /**
     * Método para mostrar um Painel de espera ao Tracker.
     */
    private void awaitTracker(){
        if(mode != MODE_AWAITING && !hasTracker){
            mode = MODE_AWAITING;
            
            this.remove(client);
            this.add(await);
        }
    }
    
    /**
     * Método para mostrar o Painel de um Processo.
     */
    private void trackerOK(){
        if(mode != MODE_OK && hasTracker){
            mode = MODE_OK;
            
            this.remove(await);
            this.add(client);
        }
    }

    /**
     * Método ?
     * @return 
     */
    public Client getClientThread() {
        return clientThread;
    }

    /**
     * Método ?
     * @param clientThread 
     */
    public void setClientThread(Client clientThread) {
        this.clientThread = clientThread;
    }

    /**
     * Método ?
     * @param data 
     */
    public void searchDone(String[][] data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
