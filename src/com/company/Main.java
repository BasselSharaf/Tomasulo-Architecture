package com.company;

import java.util.HashMap;
import java.util.Map;

public class Main {
    static int clockCycle, PC; // current clock cycle
    static InstructionQueue instructionQueue = new InstructionQueue(4);
    static RegisterFile registerFile = new RegisterFile(16);
    static Stations addStations = new Stations(2, 1, "Add");
    static Stations mulStations = new Stations(2, 3, "Multiply");
    static Stations loadStations = new Stations(2, 5, "Load");
    static Stations storeStations = new Stations(2, 7, "Store");
    static Memory memory = new Memory(64);
    static InstructionMemory instructionMemory = new InstructionMemory();
    static HashMap<Integer, InstructionState> issuedInstructions = new HashMap<>();// issued instructions state
    static int WBTag;

    public static void main(String[] args) {
    	instructionMemory.fill_mem();
    	registerFile.fillRegs();
        while(true) {
            clockCycle++;
            System.out.printf("Cycle : %d%n%n", clockCycle);
            addToQueue();
            instructionQueue.print_queue();
            execute();
            WBTag = writeBack();
            if(instructionQueue.getSize() != 0)
            	issue();
            memory.print_mem();
            registerFile.print_f();
            addStations.print_stations();
            System.out.println();
            mulStations.print_stations();
            System.out.println();            
            loadStations.print_stations();
            System.out.println();
            storeStations.print_stations();
            System.out.println();
            print_states();
            System.out.println("----------------------------------------------------------");
            if(isExecutionFinished())
            	break;
        }
    }

    // indicates that an instruction started execution
    public static void startExecution(int tag, int duration) {
        InstructionState s = issuedInstructions.get(tag);
        s.startCycle = clockCycle;
        s.endCycle = clockCycle + duration - 1;
        issuedInstructions.put(tag, s);
    }

    // checks if the memory is free
    public static boolean isMemFree() {
        for(int i = 5; i <= 8; i++) {
            InstructionState state = issuedInstructions.get(i);
            if(state == null)
            	continue;
            if(state.endCycle >= clockCycle)
                return false;
        }
        return true;
    }

    // executes all instructions which have no data dependency
    public static void execute() {
        for(Map.Entry<Integer, InstructionState> state : issuedInstructions.entrySet()) {
            if(state.getValue().finished || state.getValue().startCycle != 0)
                continue;
            int tag = state.getValue().tag;
            if(tag < 3) {
                if (addStations.get(getIdx(tag)).Qj == 0 && addStations.get(getIdx(tag)).Qk == 0)
                    startExecution(tag, 2);
            }
            else if(tag < 5) {
                if (mulStations.get(getIdx(tag)).Qj == 0 && mulStations.get(getIdx(tag)).Qk == 0)
                    startExecution(tag, 4);
            }
            else if(tag < 7) {
                if(isMemFree())
                    startExecution(tag, 2);
            }
            else {
                if(isMemFree() && storeStations.get(getIdx(tag)).Qj == 0)
                    startExecution(tag, 2);
            }
        }
    }

    // publishes the result of an instruction to the CDB
    public static int writeBack() {
    	writeBackStore(7);
    	writeBackStore(8);
        for(Map.Entry<Integer, InstructionState> state : issuedInstructions.entrySet()) {
            if(state.getValue().finished || state.getValue().startCycle == 0 || clockCycle <= state.getValue().endCycle)
                continue;
            state.getValue().finished = true;
            int tag = state.getValue().tag;
            freeStation(tag);
            if(tag < 7) {
                double result = getOperation(tag);
                // update register file
                for(int i = 0; i < registerFile.size; i++) {
                    if(registerFile.Qi[i] == tag)
                        registerFile.writeReg(i, result);
                }
                // update reservation stations
                for(int i = 0; i < addStations.size; i++) {
                    if(addStations.get(i).Qj == tag) {
                        addStations.get(i).Qj = 0;
                        addStations.get(i).Vj = result;
                    }
                    if(addStations.get(i).Qk == tag) {
                        addStations.get(i).Qk = 0;
                        addStations.get(i).Vk = result;
                    }
                }
                for(int i = 0; i < mulStations.size; i++) {
                    if(mulStations.get(i).Qj == tag) {
                        mulStations.get(i).Qj = 0;
                        mulStations.get(i).Vj = result;
                    }
                    if(mulStations.get(i).Qk == tag) {
                        mulStations.get(i).Qk = 0;
                        mulStations.get(i).Vk = result;
                    }
                }
                for(int i = 0; i < storeStations.size; i++) {
                    if(storeStations.get(i).Qj == tag) {
                        storeStations.get(i).Qj = 0;
                        storeStations.get(i).Vj = result;
                    }
                }
            }
            else { // updating issuedInstructions for store
                memory.writeMem(storeStations.get(getIdx(tag)).A, storeStations.get(getIdx(tag)).Vj);
            }
             
            return tag;
        }
        return -1;
    }
    public static void writeBackStore(int tag) {
    	if(issuedInstructions.get(tag)!=null && issuedInstructions.get(tag).endCycle == clockCycle ) {
            memory.writeMem(storeStations.get(getIdx(tag)).A, storeStations.get(getIdx(tag)).Vj);
            issuedInstructions.get(tag).finished = true;
            freeStation(tag);
    	}
    }


    public static double getOperation(int tag) {
        if(tag < 3) {
        	if(addStations.get(getIdx(tag)).op == 1)
        		return addStations.get(getIdx(tag)).Vj + addStations.get(getIdx(tag)).Vk;
        	return  addStations.get(getIdx(tag)).Vj - addStations.get(getIdx(tag)).Vk;
        }
        if(tag < 5) {
        	if( mulStations.get(getIdx(tag)).op == 2)
        		return mulStations.get(getIdx(tag)).Vj * mulStations.get(getIdx(tag)).Vk;
        	return mulStations.get(getIdx(tag)).Vj / mulStations.get(getIdx(tag)).Vk;
        }
        // this is for load operation
        return memory.readMem(loadStations.get(getIdx(tag)).A);
    }
    
    // issues instructions every cycle
    public static void issue() {
        String instruction = instructionQueue.peek();
        int[] decodedIns = decodeInstruction(instruction);
        double[] registerValues = null;
        int tag = isStationFree(decodedIns[0]);
        int address = decodedIns[2] + registerFile.get_i(decodedIns[3]);
        if(tag == -1 || isNameDependent(address, decodedIns[0]) || tag == WBTag)
        	return;
    	issuedInstructions.put(tag, new InstructionState(tag, clockCycle));
        switch(decodedIns[0]) {
        case 1 :
        case 5 :
        	registerValues = getRegisterValues(decodedIns[2], decodedIns[3]);
        	addStations.get(getIdx(tag)).setAddMul(decodedIns[0], registerValues[0], registerValues[1], (int)registerValues[2], (int)registerValues[3]);
        	registerFile.Qi[decodedIns[1]] = tag;
        	break;
        case 2 :
        case 6 :
        	registerValues = getRegisterValues(decodedIns[2], decodedIns[3]);
        	mulStations.get(getIdx(tag)).setAddMul(decodedIns[0], registerValues[0], registerValues[1], (int)registerValues[2], (int)registerValues[3]);
        	registerFile.Qi[decodedIns[1]] = tag;
        	break;
        case 3 :
        	loadStations.get(getIdx(tag)).setLoad(decodedIns[0], decodedIns[3], decodedIns[2], address);
        	registerFile.Qi[decodedIns[1]] = tag;
        	break;
        case 4 :
        	registerValues = getRegisterValues(decodedIns[1], decodedIns[3]);
        	storeStations.get(getIdx(tag)).setStore(decodedIns[0], registerValues[0], (int)registerValues[2], address);
        }
        instructionQueue.poll();
    }
    public static int[] decodeInstruction(String instruction) {
        String[] operands = instruction.split("[ ,()]");
        int op = 0;
        switch (operands[0]) {
            case "ADD.D" : op = 1;break;
            case "MUL.D" : op = 2;break;
            case "L.D" : op = 3;break;
            case "S.D" : op = 4;break;
            case "SUB.D" : op = 5;break;
            case "DIV.D" : op = 6;break;
        }
        int des = Integer.parseInt(operands[1].substring(1));
        int src1 = 0;
        if(op == 1 || op == 2 || op == 5 || op == 6 )
            src1 = Integer.parseInt(operands[2].substring(1));
        else
            src1 = Integer.parseInt(operands[2]);
        int src2 = Integer.parseInt(operands[3].substring(1));
        return new int[]{op, des, src1, src2};
    }
    
    // frees a reservation station
    public static void freeStation(int tag) {
    	if(tag < 3)
    		addStations.get(getIdx(tag)).busy = false;
    	else if(tag < 5)
    		mulStations.get(getIdx(tag)).busy = false;
    	else if(tag < 7)
    		loadStations.get(getIdx(tag)).busy = false;
    	else if(tag < 9)
    		storeStations.get(getIdx(tag)).busy = false;
    }
    
    public static double[] getRegisterValues(int idx_j, int idx_k) {
        double Vj = 0, Vk = 0;
        int Qj= 0, Qk = 0;
    	if(registerFile.Qi[idx_j] == 0)
    		Vj = registerFile.get_f(idx_j);
    	else 
    		Qj = registerFile.Qi[idx_j];
    	if(registerFile.Qi[idx_k] == 0)
    		Vk = registerFile.get_f(idx_k);
    	else 
    		Qk = registerFile.Qi[idx_k];
    	return new double[] {Vj, Vk, Qj, Qk};
    }
    
    // returns the tag of the free station, -1 otherwise.
    public static int isStationFree(int op) {
    	if(op == 1 || op == 5) {
    		if(!addStations.get(0).busy)
    			return 1;
    		if(!addStations.get(1).busy)
    			return 2;
    	}
    	if(op == 2 || op == 6) {
    		if(!mulStations.get(0).busy)
    			return 3;
    		if(!mulStations.get(1).busy)
    			return 4;
    	}
    	if(op == 3) {
    		if(!loadStations.get(0).busy)
    			return 5;
    		if(!loadStations.get(1).busy)
    			return 6;
    	}
    	if(op == 4) {
    		if(!storeStations.get(0).busy)
    			return 7;
    		if(!storeStations.get(1).busy)
    			return 8;
    	}
    	return -1;
    }
    
    // gets tag
    public static int getIdx(int tag) {
    	return (tag + 1) % 2;
    }
    
    public static boolean isNameDependent(int address, int op) {
    	if(op == 3) {
    		for(int i = 0; i < 2; i++)
    			if(storeStations.get(i).A == address && storeStations.get(i).busy)
    				return true;
    	}
    	else if(op == 4) {
    		for(int i = 0; i < 2; i++)
    			if((storeStations.get(i).A == address && (storeStations.get(i).busy || storeEndedNow())) || (loadStations.get(i).A == address && loadStations.get(i).busy))
    				return true;
    	}
    	return false;
    }
    public static boolean storeEndedNow() {
    	return (issuedInstructions.get(7)!=null && issuedInstructions.get(7).endCycle == clockCycle) || (issuedInstructions.get(8)!=null && issuedInstructions.get(8).endCycle == clockCycle);
    }
    public static void print_states() {
    	System.out.println("Instruction States :");
    	for(Map.Entry<Integer, InstructionState> state : issuedInstructions.entrySet()) {
    		state.getValue().print_state();
    	}
    }
    
    public static void addToQueue() {
    	if(instructionQueue.getSize() == instructionQueue.size || PC == instructionMemory.getSize())
    		return;
    	String instruction = instructionMemory.getInstruction(PC++);
    	instructionQueue.addInstruction(instruction);
    }
    
    public static boolean isExecutionFinished() {
    	if(!instructionQueue.instructions.isEmpty()) return false;
    	for(Map.Entry<Integer, InstructionState> state : issuedInstructions.entrySet()) {
    		if(!state.getValue().finished)
    			return false;
    	}
    	return true;
    }
}
