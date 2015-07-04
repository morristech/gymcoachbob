package com.dermobbbda.gymcoachbob;

import java.io.Serializable;

public class Exercise implements Serializable {
    /* Name of the exercise */
    private String name;

    Exercise(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Exercise: " + this.name;
    }
}