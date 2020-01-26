//  CSS430 HW1 PT1
//  Donghee Lee
//
//  This program takes one argument as a string when executed on shell.
//  This displays the number of processes of that given input name on the
//  system where this program is invoked.

#include <stdio.h>
#include <stdlib.h>
#include <sys/wait.h>
#include <unistd.h>
#include <iostream>

using namespace std;

int main(int argc, char* argv[]) {
    
    enum {READ, WRITE};
    pid_t returnPid0, returnPid1, returnPid2;
    int pipeFD1[2];
    int pipeFD2[2];
    
    returnPid0 = fork();
    
    if (returnPid0 < 0) {
        cerr << "Fork error in parents" << endl;
        exit(-1);
        
    } else if (returnPid0 == 0) {  // self is child
        
        // pipe between the child and the grand child
        int rc1 = pipe(pipeFD1);
        returnPid1 = fork();
        
        if (rc1 < 0) {
            cerr << "Pipe error in a child" << endl;
            exit(-1);
        }
        
        if(returnPid1 < 0) {
            cerr << "Fork error in a child" << endl;
            exit(-1);
            
        } else if (returnPid1 == 0) { // self is grand child
            // pipe between the grand child and the great grand child
            int rc2 = pipe(pipeFD2);
            pid_t returnPid2 = fork();
            
            if (rc2 < 0) {
                cerr << "Pipe error in a grand chilld" << endl;
                exit(-1);
            }
            
            if (returnPid2 < 0) { // fork error
                cerr << "Fork error in a grand child" << endl;
                exit(-1);
                
            } else if (returnPid2 == 0) { // self is great grand child
                dup2(pipeFD2[WRITE], 1); // STD output is written to pipeFD2

                close(pipeFD2[READ]);
                
                execlp("ps", "ps", "-A", 0);
                
            } else {
                wait(NULL);     // grand child waits for the great grand child
                
                dup2(pipeFD2[READ], 0); // STD input is read from pipeFD2
                dup2(pipeFD1[WRITE], 1);// STD output is written to pipeFD1
                
                close(pipeFD1[READ]);
                close(pipeFD2[WRITE]);
                
                execlp("grep", "grep", argv[1], 0);
            }
        } else {
            wait(NULL);         // child waits for the grand child
            
            dup2(pipeFD1[READ], 0); // STD input is read from pipeFD1
            
            close(pipeFD1[WRITE]);
            
            execlp("wc", "wc", "-l", 0);
        }
        
    } else {
        wait(NULL);             // parent waits for the child
    }
    
}
