package com.company;

import java.util.Arrays;

public class Memory {
    int size;
    double[] data;

    public Memory(int s) {
        size = s;
        data = new double[size];
    }

    public void writeMem(int address, double val) {
        if(address < size)
            data[address] = val;
    }

    public double readMem(int address) {
        return data[address];
    }
    
    public void print_mem() {
    	System.out.println("Memory :");
    	System.out.println(Arrays.toString(data));
    }
}
