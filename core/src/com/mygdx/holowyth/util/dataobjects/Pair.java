package com.mygdx.holowyth.util.dataobjects;

public class Pair<F, S> {
    private F first; //first member of pair
    private S second; //second member of pair
    
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public void setFirst(F first) {
        this.first = first;
    }

    public void setSecond(S second) {
        this.second = second;
    }

    public F first() {
        return first;
    }

    public S second() {
        return second;
    }
    
    public static <T,U> Pair<T,U> of(T first, U second) {
    	return new Pair<T, U>(first, second);
	}
}