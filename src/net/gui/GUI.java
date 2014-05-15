/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.gui;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import net.client.Client;
import net.start.Main;

/**
 *
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

    public GUI(Main main) {
        this.main = main;
        this.setLayout(new BorderLayout());
        
        hasTracker = false;
        
        awaitTracker();
        
        await = new JPanel();
        
        client = new JPanel();
        
        this.setVisible(true);
    }

    public void setHasTracker(boolean hasTracker) {
        this.hasTracker = hasTracker;
        
        updateState();
    }
    
    private void updateState(){
        if(hasTracker){
            trackerOK();
        }else{
            awaitTracker();
        }
    }
    
    private void awaitTracker(){
        if(mode != MODE_AWAITING && !hasTracker){
            mode = MODE_AWAITING;
            
            this.remove(client);
            this.add(await);
        }
    }
    
    private void trackerOK(){
        if(mode != MODE_OK && hasTracker){
            mode = MODE_OK;
            
            this.remove(await);
            this.add(client);
        }
    }

    public Client getClientThread() {
        return clientThread;
    }

    public void setClientThread(Client clientThread) {
        this.clientThread = clientThread;
    }
}