package com.example.noodleapp;

public class NoVoteException extends Exception{
    public NoVoteException(String s){
        System.out.println(s);
    }
}
