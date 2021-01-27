package com.company;
import java.util.Queue;
import java.util.LinkedList;

public class InstructionQueue {
    int size;
    Queue<String> instructions;

    public InstructionQueue(int s) {
        size = s;
        instructions = new LinkedList<String>();
    }

    public void addInstruction(String instruction) {
        if(instructions.size() < size)
            instructions.add(instruction);
    }

    public String peek() {
        return instructions.peek();
    }

    public String poll() {
        return instructions.poll();
    }
    
    public void print_queue() {
    	String x = "";
    	for(String s : instructions)
    		x = s + "\n" + x;
    	System.out.println("Instruction Queue : \n" + x);
    }
    
    public int getSize() {
    	return instructions.size();
    }
}
