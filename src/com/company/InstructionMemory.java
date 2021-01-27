package com.company;

import java.util.ArrayList;

public class InstructionMemory {
	ArrayList<String> instructions;
	public InstructionMemory() {
		instructions = new ArrayList<>();
	}
	
	public void fill_mem() {

	
		
		
		instructions.add("L.D F1,7(R3)");
		instructions.add("S.D F1,1(R6)");
		instructions.add("ADD.D F4,F5,F6");
		
		
		/*	
		instructions.add("ADD.D F0,F8,F2");
		instructions.add("MUL.D F3,F0,F2");
		instructions.add("ADD.D F4,F5,F1");
		*/
//		instructions.add("S.D F1,0(R1)");
//		instructions.add("S.D F2,0(R1)");
		
		

	}
	
	public String getInstruction(int idx) {
		return instructions.get(idx);
	}
	
	public int getSize() {
		return instructions.size();
	}
	
}
