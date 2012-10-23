/*
 * Copyright ucchy 2012
 */
package com.github.ucchyocean.hitandblow;

/**
 * @author ucchy
 *
 */
public class ScoreData {
    public String name;
    public Double score;
    public ScoreData(String name, Double score) {
        this.name = name;
        this.score = score;
    }
    public String toString() {
        return String.format("%s - %.2f", name, score);
    }
}
