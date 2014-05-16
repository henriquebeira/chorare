/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import net.client.Client;
import net.start.Main;
import org.jdesktop.swingx.JXBusyLabel;

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
    private JPanel statusPanel;
    private JTabbedPane tabPanel;
    
    private byte mode;
    
    private static final Byte MODE_AWAITING = 0;
    private static final Byte MODE_OK = 1;

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
        
        client = new JPanel(new BorderLayout());
        
        JPanel northP = new JPanel(new FlowLayout());
        JPanel centerP = new JPanel(new BorderLayout());
        
        tabPanel = new JTabbedPane(JTabbedPane.TOP);
        
        statusPanel = new JPanel();
        statusPanel.setBackground(Color.yellow);
        
        tabPanel.addTab("Atividade", statusPanel);
        
        client.add(northP, BorderLayout.NORTH);
        client.add(tabPanel);
        
        updateState();
        
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

    public void searchDone(String[][] data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
