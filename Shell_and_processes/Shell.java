// Shell.java
// CSS 430
// Program1 Part2
// Donghee Lee
//
// This is a program that behaves similar to a shell.
//
// It reads in series of keyboard input from the user and executes them if valid.
// User can use this program to execcute programs concurrently by delimiting the
// commands with "&", or sequentially by delimiting them with ";". If the command 
// is not delimited by either of those, the program assumes that it may be ran sequentially.
//
// This program does not support pipe ("|") or redirecting("<", ">").

import java.util.*;

class Shell extends Thread {
    
    private int lineNum;
    
    // Shell Constructor
    // initializes lineNum to indicate the number of line that starts from 1.
    public Shell() {
        lineNum = 1;
    }

    // run()
    // reads in keyboard input from the user and runs the commands.
    // If the user puts ";" at the end of the command, the command is executed sequentially.
    // If the user puts "&" at the end of the command, the command is executed concurrently.
    // Shell is terminated when user types in "exit" to the console.
    public void run() {
        StringBuffer cmdLine;
        
        for (;;) {
            cmdLine = new StringBuffer();
            SysLib.cout("shell[" + lineNum + "]% ");
            SysLib.cin(cmdLine);
        
            if (cmdLine.toString().length() > 0) { // user typed in input
                
                for (String cmdsWithSemicolon : cmdLine.toString().split(";")) {
                    
                    // psList keeps track of all the threads created
                    ArrayList<Integer> psList = new ArrayList<Integer>();
                    
                    for (String cmdsWithAmp : cmdsWithSemicolon.split("&")) {
                        
                        // Shell is terminated when user types "exit" as a command
                        if(cmdsWithAmp.trim().equals("exit")) {
                            SysLib.exit();
                            return;
                        }
                        String [] commandsArgs = SysLib.stringToArgs(cmdsWithAmp);
                        if (commandsArgs.length > 0) SysLib.cout(commandsArgs[0] + "\n");
                        Integer tid = SysLib.exec(commandsArgs);
                        
                        if (tid > 0) { // if thread was created successfully
                            psList.add(tid);
                        }
                    }
                    
                    // move onto the next sequence of commands when all
                    // concurrent threads are done executing
                    while (!psList.isEmpty()) {
                        Integer exitId = SysLib.join();
                        psList.remove(exitId);
                    }
                    
                }
                lineNum++;
            }
        }
    }
}
