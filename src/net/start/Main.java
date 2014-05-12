/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.start;

import net.checker.StatusChecker;

/**
 *
 * @author Henrique
 */
public class Main {
    private StatusChecker checker;
//    private 
    public static void main(String ... args){
        Main thisM = new Main();
        thisM.checker = new StatusChecker();
        thisM.checker.start();
    }
}
