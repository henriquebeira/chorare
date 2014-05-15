/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.start;

import java.util.Scanner;
import net.checker.StatusChecker;
import net.gui.GUI;

/**
 *
 * @author Henrique
 */
public class Main {
    private String nickName;
    private StatusChecker checker;
    private GUI gui;

    public Main(String nickName) {
        this.nickName = nickName;
    }
//    private 
    public static void main(String ... args){
        Main thisM;
        
        if(args.length > 0){
        
         thisM = new Main(args[0]);
        }else{
            String nick;
            
            Scanner s = new Scanner(System.in);
            
            nick = s.next();
            
            thisM = new Main(nick);
        }
        
        thisM.checker = new StatusChecker(thisM);
        thisM.checker.start();
        
        thisM.gui = new GUI(thisM);
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
}
