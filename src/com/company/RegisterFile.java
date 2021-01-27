package com.company;
import java.util.Formatter;

public class RegisterFile {
    int size;
    int[] register_gp, Qi;
    double[] register_fp;

    public RegisterFile(int s) {
        size = s;
        register_gp = new int[s];
        register_fp = new double[s];
        Qi = new int[s];
    }

    public void writeReg(int idx, double val) {
        Qi[idx] = 0;
        register_fp[idx] = val;
    }
    
    public double get_f(int idx) {
    	return register_fp[idx];
    }
    
    public int get_i(int idx) {
    	return register_gp[idx];
    }
    
    public void print_f() {
    	System.out.println("Register Name | Qi | Value");
    	for(int i = 0; i < size; i++) {
    		Formatter fmt = new Formatter();
    		fmt.format("%s%-13d %2d %9.2f\n", "F", i, Qi[i], register_fp[i]);
    		System.out.println(fmt);
    	}
    }
    
    public void fillRegs() {
    	for(int i = 0; i < size; i++) {
    		register_fp[i] = Math.random() * 1000;
    		register_gp[i] = (int)(Math.random() * 40) + 1;
    	}
    }
    
    public static void main(String[] args) {
		RegisterFile r = new RegisterFile(16);
		r.fillRegs();
		r.print_f();
	}
}
