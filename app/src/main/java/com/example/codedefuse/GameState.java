package com.example.codedefuse;

import java.util.ArrayList;
import java.util.Random;

public class GameState {

    public static String username;

    public static int totalTime = 300;
    public static int remainingTime = 300;
    public static int mistakeCount = 0;
    public static int maxMistakeCount = 3;
    public static int solvedModuleCount = 0;
    public static int totalModuleCount = 5;

    public static boolean isWireSolved = false;
    public static boolean isButtonSolved = false;
    public static boolean isColorMemorySolved = false;
    public static boolean isDialSolved = false;
    public static boolean isPasswordSolved = false;

    public static String serialNumber = "";
    public static int batteryCount = 0;
    public static String indicatorCode = "NONE";

    public static boolean isGameWon = false;
    public static boolean isGameLost = false;

    // Module Data Retention
    public static boolean wireDataGenerated = false;
    public static ArrayList<String> wireColors = new ArrayList<>();
    public static int correctWireIndex = -1;

    public static boolean buttonDataGenerated = false;
    public static String buttonColor = "";
    public static String buttonText = "";
    public static String stripColor = "";

    public static boolean colorMemoryDataGenerated = false;
    public static ArrayList<String> colorSequence = new ArrayList<>();
    public static int colorMemoryStep = 0;

    public static boolean dialDataGenerated = false;
    public static ArrayList<String> dialCorrectSequence = new ArrayList<>();
    public static int dialStep = 0;

    public static boolean passwordDataGenerated = false;
    public static String passwordWord = "";
    public static String securityWord = "";
    public static boolean securityWordLoaded = false;
    public static boolean securityWordFallbackUsed = false;
    public static boolean securityWordRequestStarted = false;
    public static long securityWordRequestStartedAt = 0L;
    public static int securityWordRequestId = 0;
    public static String correctPassword = "";

    private static final String SERIAL_LETTERS = "ABCDEFGHIJKLMNPQRSTUVWXYZ";
    private static final Random random = new Random();

    public static void resetGame() {
        remainingTime = totalTime;
        mistakeCount = 0;
        solvedModuleCount = 0;

        isWireSolved = false;
        isButtonSolved = false;
        isColorMemorySolved = false;
        isDialSolved = false;
        isPasswordSolved = false;

        isGameWon = false;
        isGameLost = false;

        // Reset module data
        wireDataGenerated = false;
        wireColors.clear();
        correctWireIndex = -1;

        buttonDataGenerated = false;
        buttonColor = "";
        buttonText = "";
        stripColor = "";

        colorMemoryDataGenerated = false;
        colorSequence.clear();
        colorMemoryStep = 0;

        dialDataGenerated = false;
        dialCorrectSequence.clear();
        dialStep = 0;

        passwordDataGenerated = false;
        passwordWord = "";
        securityWord = "";
        securityWordLoaded = false;
        securityWordFallbackUsed = false;
        securityWordRequestStarted = false;
        securityWordRequestStartedAt = 0L;
        securityWordRequestId++;
        correctPassword = "";

        generateBombInfo();
    }

    public static void setupGame(String playerName) {
        username = playerName;
        resetGame();
    }

    public static void generateBombInfo() {
        char firstLetter = getRandomSerialLetter();
        int firstDigit = getRandomSerialDigit();
        char secondLetter = getRandomSerialLetter();
        int secondDigit = getRandomSerialDigit();
        char thirdLetter = getRandomSerialLetter();

        serialNumber = "" + firstLetter + firstDigit + secondLetter + secondDigit + thirdLetter;
        batteryCount = random.nextInt(5);

        String[] indicators = {"CAR", "FRK", "NSA", "SIG", "NONE"};
        indicatorCode = indicators[random.nextInt(indicators.length)];
    }

    private static char getRandomSerialLetter() {
        return SERIAL_LETTERS.charAt(random.nextInt(SERIAL_LETTERS.length()));
    }

    private static int getRandomSerialDigit() {
        return 1 + random.nextInt(9);
    }

    public static void addMistake() {
        if (isGameOver()) {
            return;
        }

        mistakeCount++;

        if (mistakeCount >= maxMistakeCount) {
            isGameLost = true;
        }
    }

    public static void markWireSolved() {
        if (!isWireSolved) {
            isWireSolved = true;
            solvedModuleCount++;
            checkWin();
        }
    }

    public static void markButtonSolved() {
        if (!isButtonSolved) {
            isButtonSolved = true;
            solvedModuleCount++;
            checkWin();
        }
    }

    public static void markColorMemorySolved() {
        if (!isColorMemorySolved) {
            isColorMemorySolved = true;
            solvedModuleCount++;
            checkWin();
        }
    }

    public static void markDialSolved() {
        if (!isDialSolved) {
            isDialSolved = true;
            solvedModuleCount++;
            checkWin();
        }
    }

    public static void markPasswordSolved() {
        if (!isPasswordSolved) {
            isPasswordSolved = true;
            solvedModuleCount++;
            checkWin();
        }
    }

    public static boolean isAllModulesSolved() {
        return solvedModuleCount >= totalModuleCount;
    }

    public static boolean isGameOver() {
        return isGameWon || isGameLost;
    }

    public static int calculateScore() {
        if (!isGameWon) {
            int lossScore = solvedModuleCount * 100;
            lossScore += remainingTime / 10;
            lossScore -= mistakeCount * 50;
            return Math.max(lossScore, 0);
        }

        int score = 1000;
        score += solvedModuleCount * 250;
        score += remainingTime * 5;
        score -= mistakeCount * 150;

        return Math.max(score, 0);
    }

    public static boolean hasVowelInSerial() {
        String serial = serialNumber.toUpperCase();
        return serial.contains("A") || serial.contains("E") || serial.contains("I")
                || serial.contains("O") || serial.contains("U");
    }

    public static int getLastDigitOfSerial() {
        for (int i = serialNumber.length() - 1; i >= 0; i--) {
            char character = serialNumber.charAt(i);
            if (Character.isDigit(character)) {
                return Character.getNumericValue(character);
            }
        }
        return -1;
    }

    public static int getFirstDigitOfSerial() {
        for (int i = 0; i < serialNumber.length(); i++) {
            char character = serialNumber.charAt(i);
            if (Character.isDigit(character)) {
                return Character.getNumericValue(character);
            }
        }
        return -1;
    }

    private static void checkWin() {
        if (isAllModulesSolved()) {
            isGameWon = true;
        }
    }
}
