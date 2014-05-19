/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.v2.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import net.v2.start.Main;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTable;

/**
 *
 * @author User
 */
public class GUI extends JFrame implements WindowListener{

    private Main main;
    private JPanel waiting;
    private JPanel running;
    public final static Byte MODE_WAIT = 1;
    public final static Byte MODE_RUN = 2;
    private Byte mode;
    private JTabbedPane tabPanel;
    private JTabbedPane inerTabPanel;
    private JTextField searchArea;
    
    private JXTable table;

    public GUI(Main main) {
        setLayout(new BorderLayout());
        setTitle(main.getNickName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);

        this.main = main;

        waiting = new JPanel(new BorderLayout());

        JXBusyLabel busy = new JXBusyLabel(new Dimension(150, 150));

        busy.setBusy(true);
        busy.setDelay(30);
        busy.setHorizontalAlignment(JXBusyLabel.CENTER);

        busy.getBusyPainter().setPoints(100);
        busy.getBusyPainter().setTrailLength(10);

        waiting.add(busy);

        running = new JPanel(new BorderLayout());
        JPanel searchPanel = new JPanel(new BorderLayout());
        
        searchArea = new JTextField();
        searchArea.setMinimumSize(new Dimension(150, (int)searchArea.getPreferredSize().getHeight()));
        
        JButton searchButton = new JButton("Buscar");
        searchButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent me) {
                if(!searchArea.getText().equals("")){
                    doSearch(searchArea.getText());
                }
            }
        });
        
        searchPanel.add(searchArea);
        searchPanel.add(searchButton, BorderLayout.EAST);
        
        running.add(searchPanel, BorderLayout.NORTH);
        
        tabPanel = new JTabbedPane();
        
        inerTabPanel = new JTabbedPane(JTabbedPane.LEFT);
        
        
        
        running.add(tabPanel);

        //TEst
        running.setBackground(Color.yellow);
        
        changeModeAwaiting();

        this.setVisible(true);
    }

    public void updateState(Byte mode) {
        if (mode == MODE_RUN) {
            changeModeRunning();
            return;
        }
        
        if (mode == MODE_WAIT) {
            changeModeAwaiting();
        }
    }

    private void changeModeAwaiting() {
        if (mode != MODE_WAIT) {
            this.remove(running);

            this.add(waiting);

            revalidate();
            repaint();
        }
    }

    private void changeModeRunning() {
        if (mode != MODE_RUN) {
            this.remove(waiting);

            this.add(running);

            revalidate();
            repaint();
        }
    }

    public Byte getMode() {
        return mode;
    }

    public void setMode(Byte mode) {
        updateState(mode);
    }
    
    private void doSearch(String search){
        main.getClient().search(search);
    }
    
    public void receiveSearchResponse(Object[][] data){
        
    }
    
    private void makeDownload(InetAddress peer, Integer peerPort, String file){
        main.getClient().requestFileFromPeer(peer, peerPort, file);
    }
    
    public void warnCompletedDownload(String file, String peerNick, File filePath){
        String[] buttons = new String[]{"Abrir o arquivo","Não abrir"};
        
        Integer response =  JOptionPane.showOptionDialog(this, "O download do arquivo " + file + " foi concluído.\n"
                + "O arquivo foi transferido pelo peer: " + peerNick + ".\n Deseja abrir o arquivo?", file + " concluído.", 
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, buttons, buttons[0]);
        
        if(response == 0){
            try {
                Desktop.getDesktop().open(filePath);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Não foi possível abrir o arquiv!", "Erro", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    @Override
    public void windowOpened(WindowEvent we) {
    }

    @Override
    public void windowClosing(WindowEvent we) {
        main.killThreads();
    }

    @Override
    public void windowClosed(WindowEvent we) {
    }

    @Override
    public void windowIconified(WindowEvent we) {
    }

    @Override
    public void windowDeiconified(WindowEvent we) {
    }

    @Override
    public void windowActivated(WindowEvent we) {
    }

    @Override
    public void windowDeactivated(WindowEvent we) {
    }
}
