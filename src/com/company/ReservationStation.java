package com.company;

import java.util.Formatter;

public class ReservationStation {
    boolean busy;
    int op, tag, Qj, Qk, A;
    double Vj, Vk;

    public ReservationStation(int tag) {
        this.tag = tag;
    }

    public void setAddMul(int op, double Vj, double Vk, int Qj, int Qk) {
        this.busy = true;
        this.op = op;
        this.Vj = Vj;
        this.Vk = Vk;
        this.Qj = Qj;
        this.Qk = Qk;
    }

    public void setLoad(int op, double Vj, double Vk, int A) {
        this.busy = true;
        this.op = op;
        this.Vj = Vj;
        this.Vk = Vk;
        this.A = A;
    }

    public void setStore(int op, double Vj, int Qj, int A) {
        this.busy = true;
        this.op = op;
        this.Vj = Vj;
        this.Qj = Qj;
        this.A = A;
    }
    
    public void print_station() {
    	System.out.printf("Tag : %d | Busy : %b | op : %d | Vj : %.2f | Vk : %.2f | Qj : %d | Qk : %d | A : %d%n", tag, busy, op, Vj, Vk, Qj, Qk, A);
    }
    
    public void fillStation() {
    	busy = true;
    	op = 2;
    	Vj = 122.2;
    	Vk = 5252682.12;
    	Qj = 0;
    	Qk = 4;
    	A = 0;
    }

    public static void main(String[] args) {
		ReservationStation rs = new ReservationStation(4);
		rs.fillStation();
		rs.print_station();
	}
}
