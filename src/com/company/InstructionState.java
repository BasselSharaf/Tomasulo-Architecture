package com.company;

public class InstructionState {
    int tag, issueCycle, startCycle, endCycle;
    boolean finished; // if instruction already wrote back

    public InstructionState(int t, int a) {
        tag = t;
        issueCycle = a;
    }
    
    public void print_state() {
    	System.out.printf("Tag : %d | Issue Cycle : %d | Start Cycle : %d | End Cycle : %d | Write Back : %b%n", tag, issueCycle, startCycle, endCycle, finished);
    }
}
