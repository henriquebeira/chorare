/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.v2.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import net.v2.start.Main;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTable;

/**
 *
 * @author User
 */
public class GUI extends JFrame implements WindowListener {

    private final Main main;
    private JPanel waiting;
    private JPanel running;
    public final static Byte MODE_WAIT = 1;
    public final static Byte MODE_RUN = 2;
    private Byte mode;
    private JTabbedPane tabPanel;
    private JTabbedPane inerTabPanel;
    private JTextField searchArea;
    private JXTable table;

    public GUI(Main mainR) {
        setLayout(new BorderLayout());
        setTitle(mainR.getNickName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);

        this.main = mainR;

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
        searchArea.setMinimumSize(new Dimension(150, (int) searchArea.getPreferredSize().getHeight()));

        JButton searchButton = new JButton("Buscar");
        searchButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent me) {
                if (!searchArea.getText().equals("")) {
                    doSearch(searchArea.getText());
                }
            }
        });

        searchPanel.add(searchArea);
        searchPanel.add(searchButton, BorderLayout.EAST);

        running.add(searchPanel, BorderLayout.NORTH);

        tabPanel = new JTabbedPane();

        inerTabPanel = new JTabbedPane(JTabbedPane.LEFT);

        JPanel generalP = new JPanel(new BorderLayout());

        table = new JXTable(new Object[0][1], new String[]{"Nome Arquivo"});
        table.setEditable(false);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    me.consume();

                    Component comp = (Component) me.getSource();

                    while (!(comp instanceof JXTable) && comp != null) {
                        comp = comp.getParent();
                    }

                    if (comp != null) {
                        JXTable table = (JXTable) comp;

                        int selectedRow = table.getSelectedRow();
                        try {
                            Desktop.getDesktop().open(new File(main.getFilesFolder() + File.separator + table.getValueAt(selectedRow, 0)));
                        } catch (IOException ex) {
                        }
                    }
                }
            }
        });

        updateTable();

        generalP.add(
                new JScrollPane(table));

        inerTabPanel.addTab(
                "Arquivos", generalP);

        tabPanel.addTab(
                "Local", inerTabPanel);

        running.add(tabPanel);

//        //TEst
//        running.setBackground(Color.yellow);
        changeModeAwaiting();

        this.setVisible(
                true);
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

    private void doSearch(String search) {
        main.getClient().search(search);
    }

    public void receiveSearchResponse(Object[][] data, String searched) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel NorthPanel = new JPanel(new BorderLayout());
        NorthPanel.add(new JLabel("Buscando por: " + searched));

        JButton button = new JButton(UIManager.getIcon("OptionPane.errorIcon"));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent me) {
                Container remove = ((JButton) me.getSource()).getParent().getParent();

                tabPanel.remove(remove);
            }
        });

        NorthPanel.add(button, BorderLayout.EAST);

        panel.add(NorthPanel, BorderLayout.NORTH);

        if (data.length == 0) {
            JLabel lab = new JLabel("Nenhum resultado encontrado");
            lab.setHorizontalAlignment(JLabel.CENTER);
            lab.setVerticalAlignment(JLabel.CENTER);

            panel.add(lab);
        } else {
            JXTable table = new JXTable(data, new String[]{"Nome Arquivo", "NickName", "IP do Peer", "Peer Port"});
            table.setColumnControlVisible(true);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setEditable(false);

            JScrollPane sP = new JScrollPane(table);

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent me) {
                    if (me.getClickCount() == 2) {
                        me.consume();

                        Component comp = (Component) me.getSource();

                        while (!(comp instanceof JXTable) && comp != null) {
                            comp = comp.getParent();
                        }

                        if (comp != null) {
                            JXTable table = (JXTable) comp;

                            int selectedRow = table.getSelectedRow();

                            makeDownload(table.getValueAt(selectedRow, 1).toString(),
                                    table.getValueAt(selectedRow, 2).toString(),
                                    (Integer) table.getValueAt(selectedRow, 3),
                                    table.getValueAt(selectedRow, 0).toString());
                        }
                    }
                }
            });

            panel.add(sP);
        }

        tabPanel.addTab(searched, panel);
    }

    private void makeDownload(String peerNick, String peerIP, Integer peerPort, String file) {
        main.getClient().requestFileFromPeer(peerNick, peerIP, peerPort, file);
    }

    public void warnCompletedDownload(String file, String peerNick, File filePath) {
        String[] buttons = new String[]{"Abrir o arquivo", "Não abrir"};

        Integer response = JOptionPane.showOptionDialog(this, "O download do arquivo " + file + " foi concluído.\n"
                + "O arquivo foi transferido pelo peer: " + peerNick + ".\n Deseja abrir o arquivo?", file + " concluído.",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, buttons, buttons[0]);

        if (response == 0) {
            try {
                Desktop.getDesktop().open(filePath);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Não foi possível abrir o arquiv!", "Erro", JOptionPane.WARNING_MESSAGE);
            }
        }

        updateTable();
    }

    private void updateTable() {
        File folder = main.getFilesFolder();

        ArrayList<String> files = new ArrayList<>();

        for (File f : folder.listFiles()) {
            if (!f.getName().equals("controle")) {
                files.add(f.getName());
            }
        }

        Object[][] filesD = new Object[files.size()][1];

        int k = 0;
        for (String s : files) {
            filesD[k++][0] = s;
        }

        table.setModel(new DefaultTableModel(filesD, new String[]{"Nome Arquivo"}));

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
