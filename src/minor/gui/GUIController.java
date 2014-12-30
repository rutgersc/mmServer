package minor.gui;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import minor.game.GameSessionsServer;

/**
 *
 * @author Callan
 */
public class GUIController extends Thread {
    
    private MainPanel gui = null;
    
    private boolean shouldUpdate = true;
    
    private int threadCount;
    private String serverStatus = "Idle";
    
    
    
    public void GUIController() {
        
    }
    
    public void initGUI() {
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                gui = new MainPanel();
                gui.setVisible(true);
            }
        });
    }
    
    // TODO manier coderen om via executor service een get serverstatus te laten
    // werken op de EDT. Tijdelijke oplossing set gewoon de server status elke
    // seconde.
    public void startUpdatingGUI() {
        shouldUpdate = true;
        final Runnable r;
        r = new Runnable() {
            public void run() {
                
                try {
                    // Gui elements that only have to be set once.

                    setAdress(GameSessionsServer.getIp());
                    setPort(GameSessionsServer.getPort());
                    
                } catch (Exception ex) {
                    Logger.getLogger(GUIController.class.getName()).log(Level.SEVERE, null, ex);
                }
                

                while(shouldUpdate)
                {
                    if(threadCount != Thread.activeCount()) {
                        threadCount = Thread.activeCount();
                        setThreadCount(threadCount);
                        System.out.println("GUICONTROLLER: Threadcount updated.");
                    }
                    setServerStatus(serverStatus);
                    if(GameSessionsServer.getUserList() != null) {
                        setConnected(GameSessionsServer.getUserList());
                    }
                    
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GUIController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    //echo("GUI update called");
                }
                
            }
        };
        new Thread(r).start();
    }
    
    public void stopUpdatingGUI() {
        shouldUpdate = false;
    }
    
    
    public void echo(final String string) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                gui.addServerLogLine(string);
            }
        });
    }
    
    public void setThreadCount(final int i) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                gui.setThreadCount(i);
            }
        });
    }
    
    public void updateServerStatus(String s) {
        serverStatus = s;
    }
    public void setServerStatus(final String s) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                gui.setServerStatus(s);
            }
        });
    }
    
    public void setAdress(final String s) {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                gui.setAddress(s);
            }
        });  
    }
    
    public void setPort(final int i) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                gui.setPort(i);
            }
        }); 
    }
    
    public void setConnected(final ArrayList<String> aL) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if(!aL.isEmpty()) {
                    gui.setConnected(aL);
                }
            }
        }); 
    }
}
