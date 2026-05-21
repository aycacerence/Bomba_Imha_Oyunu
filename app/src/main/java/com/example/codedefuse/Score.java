package com.example.codedefuse;

public class Score {

    private int id;
    private String username;
    private int score;
    private String result;
    private int remainingTime;
    private int mistakeCount;
    private int solvedModuleCount;
    private int totalModuleCount;
    private String date;

    public Score(int id, String username, int score, String result, int remainingTime,
                 int mistakeCount, int solvedModuleCount, int totalModuleCount, String date) {
        this.id = id;
        this.username = username;
        this.score = score;
        this.result = result;
        this.remainingTime = remainingTime;
        this.mistakeCount = mistakeCount;
        this.solvedModuleCount = solvedModuleCount;
        this.totalModuleCount = totalModuleCount;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }

    public String getResult() {
        return result;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public int getMistakeCount() {
        return mistakeCount;
    }

    public int getSolvedModuleCount() {
        return solvedModuleCount;
    }

    public int getTotalModuleCount() {
        return totalModuleCount;
    }

    public String getDate() {
        return date;
    }
}
