package com.enjoyxstudy.elevatorsaga;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AutoPaly implements AutoCloseable {

    private static final List<Integer> ALL_CHALLENGE_NUMBERS = Collections.unmodifiableList(
            IntStream.rangeClosed(1, 18)
                    .boxed()
                    .collect(Collectors.toList()));

    private final WebDriver driver = new ChromeDriver();

    public static void main(String[] args) throws IOException {

        String script = new String(Files.readAllBytes(Paths.get(args[0])), StandardCharsets.UTF_8);

        int numberOfPlay = 10;
        if (args.length == 2) {
            numberOfPlay = Integer.parseInt(args[1]);
        }

        if (System.getProperty("webdriver.chrome.driver") == null) {
            System.setProperty("webdriver.chrome.driver", "driver/chromedriver.exe");
        }

        try (AutoPaly autoPaly = new AutoPaly()) {

            long startTime = System.currentTimeMillis();

            List<ChallengeResult> results = autoPaly.playAllChallenge(script, numberOfPlay);

            long totalTime = System.currentTimeMillis() - startTime;

            int allTotalCount = results.stream()
                    .map(ChallengeResult::getTotalCount)
                    .mapToInt(Integer::intValue)
                    .sum();

            int allSuccessCount = results.stream()
                    .map(ChallengeResult::getSuccessCount)
                    .mapToInt(Integer::intValue)
                    .sum();

            System.out.println(
                    String.format(
                            "Total time: %,d seconds",
                            totalTime / 1000));

            System.out.println("--------------------------------------------");
            System.out.println(
                    String.format(
                            "All         : %6.2f (%d/%d)",
                            allSuccessCount / (double) allTotalCount * 100,
                            allSuccessCount,
                            allTotalCount));

            for (ChallengeResult result : results) {
                System.out.println(
                        String.format(
                                "Challenge %2d: %6.2f (%d/%d)",
                                result.getChallengeNumber(),
                                result.getSuccessCount() / (double) result.getTotalCount() * 100,
                                result.getSuccessCount(),
                                result.getTotalCount()));
            }
            System.out.println("--------------------------------------------");
        }
    }

    public ChallengeResult play(int challengeNumber, String script, int numberOfPlay) {

        int successCount = 0;
        int failedCount = 0;

        setup(challengeNumber, script);
        
        for (int i = 0; i < numberOfPlay; i++) {

            boolean successed = run();

            if (successed) {
                successCount++;
            } else {
                failedCount++;
            }
        }

        return new ChallengeResult(challengeNumber, successCount, failedCount);
    }

    public List<ChallengeResult> playAllChallenge(String script, int numberOfPlay) {

        return ALL_CHALLENGE_NUMBERS.stream()
                .map(x -> play(x, script, numberOfPlay))
                .collect(Collectors.toList());
    }

    private boolean run() {

        // 実行
        driver.findElement(By.cssSelector("#button_apply")).click();

        // 結果取得(待ち合わせ)
        WebDriverWait wait = new WebDriverWait(driver, 60);
        WebElement feedbackElement = wait
                .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".feedback h2")));

        return feedbackElement.getText().equals("Success!");
    }

    private void setup(int challengeNumber, String script) {

        driver.get("https://play.elevatorsaga.com/#challenge=" + challengeNumber);

        // スピードのスケールを最大に
        // (要素が都度置き換わるので、毎回要素を取得して呼び出し)
        driver.findElement(By.cssSelector(".fa-plus-square")).click();
        driver.findElement(By.cssSelector(".fa-plus-square")).click();
        driver.findElement(By.cssSelector(".fa-plus-square")).click();
        driver.findElement(By.cssSelector(".fa-plus-square")).click();
        driver.findElement(By.cssSelector(".fa-plus-square")).click();
        driver.findElement(By.cssSelector(".fa-plus-square")).click();
        driver.findElement(By.cssSelector(".fa-plus-square")).click();
        driver.findElement(By.cssSelector(".fa-plus-square")).click();

        // 入力欄がSeleniumからフォーカス当てられなかったので、JavaScriptで設定
        ((JavascriptExecutor) driver).executeScript("document.querySelector('.CodeMirror textarea').focus()");

        // スクリプトの入力
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        clipboard.setContents(new StringSelection(script), null);

        new Actions(driver)
                .sendKeys(Keys.chord(Keys.CONTROL, "a"))
                .sendKeys(Keys.chord(Keys.DELETE))
                .sendKeys(Keys.chord(Keys.CONTROL, "v"))
                .build()
                .perform();
    }

    @Override
    public void close() {
        driver.close();
    }
}
