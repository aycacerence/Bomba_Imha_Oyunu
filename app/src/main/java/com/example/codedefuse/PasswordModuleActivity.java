package com.example.codedefuse;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class PasswordModuleActivity extends Activity {

    private static final String SECURITY_WORD_API_URL = "https://random-word-api.herokuapp.com/word?number=1&length=5";
    private static final String BACKUP_WORD_API_URL = "https://api.datamuse.com/words?sp=?????&max=50";
    private static final String[] FALLBACK_WORDS = {"ORBIT", "PANEL", "SIGNAL", "VECTOR", "MODULE", "CIRCUIT", "ACCESS"};

    private TextView serialText;
    private TextView batteryText;
    private TextView indicatorText;
    private TextView wordText;
    private TextView wordSourceText;
    private EditText pinEditText;
    private Button checkButton;
    private Button backButton;

    private String securityWord = "";
    private LowTimeWarningOverlay lowTimeWarningOverlay;
    private final Random random = new Random();
    private final Handler wordRefreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable wordRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (GameState.securityWordLoaded) {
                showLoadedSecurityWord(false);
            } else if (isSecurityWordRequestStale()) {
                useFallbackWord(true);
            } else {
                wordRefreshHandler.postDelayed(this, 300);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_module);
        lowTimeWarningOverlay = new LowTimeWarningOverlay(this);
        lowTimeWarningOverlay.start();

        connectViews();

        if (GameState.serialNumber == null || GameState.serialNumber.isEmpty()) {
            GameState.setupGame("Oyuncu");
        }

        showInfo();
        prepareSecurityWord();

        checkButton.setOnClickListener(v -> checkPin());
        backButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        if (lowTimeWarningOverlay != null) {
            lowTimeWarningOverlay.stop();
        }

        wordRefreshHandler.removeCallbacks(wordRefreshRunnable);
        super.onDestroy();
    }

    private void connectViews() {
        serialText = findViewById(R.id.txtPasswordSerial);
        batteryText = findViewById(R.id.txtPasswordBattery);
        indicatorText = findViewById(R.id.txtPasswordIndicator);
        wordText = findViewById(R.id.txtPasswordWord);
        wordSourceText = findViewById(R.id.txtPasswordWordSource);
        pinEditText = findViewById(R.id.editPin);
        checkButton = findViewById(R.id.btnCheckPin);
        backButton = findViewById(R.id.btnBackFromPassword);
    }

    private void showInfo() {
        serialText.setText("Seri No: " + GameState.serialNumber);
        batteryText.setText("Pil Sayısı: " + GameState.batteryCount);
        indicatorText.setText("Indicator: " + GameState.indicatorCode);
    }

    private void prepareSecurityWord() {
        if (GameState.securityWordLoaded) {
            showLoadedSecurityWord(false);
            return;
        }

        showSecurityWordLoading();

        if (GameState.securityWordRequestStarted) {
            wordRefreshHandler.postDelayed(wordRefreshRunnable, 300);
            return;
        }

        GameState.securityWordRequestStarted = true;
        GameState.securityWordRequestStartedAt = System.currentTimeMillis();
        int requestId = GameState.securityWordRequestId;

        new Thread(() -> {
            String apiWord = fetchSecurityWordFromApi();
            boolean fallbackUsed = apiWord == null;
            String finalWord = fallbackUsed ? pickFallbackWord() : apiWord;
            boolean stored = storeSecurityWordIfNeeded(finalWord, fallbackUsed, requestId);

            if (stored) {
                runOnUiThread(() -> {
                    if (!isFinishing()) {
                        showLoadedSecurityWord(fallbackUsed);
                    }
                });
            }
        }).start();
    }

    private void showSecurityWordLoading() {
        securityWord = "";
        wordText.setText("Yükleniyor...");
        wordSourceText.setText("Güvenlik kelimesi alınıyor.");
        pinEditText.setEnabled(false);
        checkButton.setEnabled(false);
    }

    private void showLoadedSecurityWord(boolean showFallbackToast) {
        securityWord = GameState.securityWord;
        wordText.setText(securityWord);
        pinEditText.setEnabled(true);
        checkButton.setEnabled(true);

        if (GameState.securityWordFallbackUsed) {
            wordSourceText.setText("Yerel güvenlik kelimesi kullanılıyor.");
            if (showFallbackToast) {
                Toast.makeText(this, "Yerel güvenlik kelimesi kullanılıyor.", Toast.LENGTH_SHORT).show();
            }
        } else {
            wordSourceText.setText("Güvenlik kelimesi API'den alındı.");
        }
    }

    private void checkPin() {
        if (!GameState.securityWordLoaded) {
            Toast.makeText(this, "Güvenlik kelimesi yükleniyor.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userPin = pinEditText.getText().toString().trim();

        if (userPin.isEmpty()) {
            Toast.makeText(this, "PIN gir.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userPin.equals(calculatePin())) {
            GameState.markPasswordSolved();
            Toast.makeText(this, "Şifre paneli çözüldü.", Toast.LENGTH_SHORT).show();
        } else {
            GameState.addMistake();
            Toast.makeText(this, "Yanlış PIN.", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private String calculatePin() {
        int result = GameState.getFirstDigitOfSerial();

        if (result < 0) {
            result = 0;
        }

        if (GameState.batteryCount >= 2) {
            result += 3;
        }

        if (GameState.indicatorCode.equals("CAR")) {
            result += 2;
        }

        String word = GameState.securityWord;

        if (word.length() == 5) {
            result += 5;
        }

        if (startsWithVowel(word)) {
            result += 2;
        }

        if (word.contains("R")) {
            result *= 2;
        }

        if (result < 10) {
            return "0" + result;
        }

        return String.valueOf(result);
    }

    private String fetchSecurityWordFromApi() {
        String primaryWord = fetchSecurityWordFromUrl(SECURITY_WORD_API_URL, true);
        if (primaryWord != null) {
            return primaryWord;
        }

        return fetchSecurityWordFromUrl(BACKUP_WORD_API_URL, false);
    }

    private String fetchSecurityWordFromUrl(String apiUrl, boolean stringArrayResponse) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "CodeDefuse/1.0");

            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }

            reader.close();
            String response = responseBuilder.toString();
            String parsedWord = stringArrayResponse
                    ? parseStringArraySecurityWord(response)
                    : parseDatamuseSecurityWord(response);

            if (!isValidApiWord(parsedWord)) {
                return null;
            }

            return parsedWord.toUpperCase(Locale.US);
        } catch (Exception e) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String parseStringArraySecurityWord(String response) {
        String trimmedResponse = response.trim();
        int firstQuote = trimmedResponse.indexOf('"');
        int secondQuote = trimmedResponse.indexOf('"', firstQuote + 1);

        if (firstQuote >= 0 && secondQuote > firstQuote) {
            return trimmedResponse.substring(firstQuote + 1, secondQuote).trim();
        }

        return trimmedResponse
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "")
                .trim();
    }

    private String parseDatamuseSecurityWord(String response) {
        ArrayList<String> words = new ArrayList<>();
        int searchStart = 0;

        while (searchStart < response.length()) {
            int keyIndex = response.indexOf("\"word\"", searchStart);
            if (keyIndex < 0) {
                break;
            }

            int colonIndex = response.indexOf(':', keyIndex);
            int firstQuote = response.indexOf('"', colonIndex);
            int secondQuote = response.indexOf('"', firstQuote + 1);

            if (colonIndex < 0 || firstQuote < 0 || secondQuote < 0) {
                break;
            }

            String candidate = response.substring(firstQuote + 1, secondQuote).trim();
            if (isValidApiWord(candidate) && candidate.length() == 5) {
                words.add(candidate);
            }

            searchStart = secondQuote + 1;
        }

        if (words.isEmpty()) {
            return "";
        }

        return words.get(random.nextInt(words.size()));
    }

    private boolean isValidApiWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }

        String trimmedWord = word.trim();
        for (int i = 0; i < trimmedWord.length(); i++) {
            if (!Character.isLetter(trimmedWord.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private boolean storeSecurityWordIfNeeded(String word, boolean fallbackUsed, int requestId) {
        synchronized (GameState.class) {
            if (requestId != GameState.securityWordRequestId) {
                return false;
            }

            if (GameState.securityWordLoaded) {
                return false;
            }

            GameState.securityWord = word.toUpperCase(Locale.US);
            GameState.passwordWord = GameState.securityWord;
            GameState.passwordDataGenerated = true;
            GameState.securityWordFallbackUsed = fallbackUsed;
            GameState.securityWordLoaded = true;
            GameState.securityWordRequestStarted = false;
            return true;
        }
    }

    private void useFallbackWord(boolean showFallbackToast) {
        if (storeSecurityWordIfNeeded(pickFallbackWord(), true, GameState.securityWordRequestId)) {
            showLoadedSecurityWord(showFallbackToast);
        }
    }

    private String pickFallbackWord() {
        return FALLBACK_WORDS[random.nextInt(FALLBACK_WORDS.length)];
    }

    private boolean isSecurityWordRequestStale() {
        return GameState.securityWordRequestStarted
                && System.currentTimeMillis() - GameState.securityWordRequestStartedAt > 13000;
    }

    private boolean startsWithVowel(String word) {
        return word.startsWith("A") || word.startsWith("E") || word.startsWith("I")
                || word.startsWith("O") || word.startsWith("U");
    }
}
