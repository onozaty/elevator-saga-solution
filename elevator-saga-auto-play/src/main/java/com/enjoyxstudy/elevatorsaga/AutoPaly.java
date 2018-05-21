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
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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

    private static final boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

    private static final Object lockObject = new Object();

    private static final List<Integer> ALL_CHALLENGE_NUMBERS = Collections.unmodifiableList(
            IntStream.rangeClosed(1, 18)
                    .boxed()
                    .collect(Collectors.toList()));

    private final WebDriver driver = new ChromeDriver();

    public static void main(String[] args) throws IOException {

        if (System.getProperty("webdriver.chrome.driver") == null) {
            System.setProperty("webdriver.chrome.driver", WebDriverUtils.defualtDriverPath());
        }

        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addOption(
                Option.builder("f")
                        .desc("Script file path.")
                        .hasArg()
                        .argName("file")
                        .required()
                        .build());
        options.addOption(
                Option.builder("p")
                        .desc("Play parallel. (default non parallel)")
                        .hasArg(false)
                        .build());
        options.addOption(
                Option.builder("c")
                        .desc("Challenge numbers. (default all challenges)\n'-c 1,2,3' play challenge #1, #2, #3.")
                        .hasArg()
                        .argName("challenges")
                        .build());
        options.addOption(
                Option.builder("n")
                        .desc("Number of play. (default 10)")
                        .hasArg()
                        .argName("numer")
                        .build());

        try {
            CommandLine line = parser.parse(options, args);

            boolean isParallel = line.hasOption("p");

            List<Integer> challengeNumbers = ALL_CHALLENGE_NUMBERS;
            if (line.hasOption("c")) {
                challengeNumbers = Stream.of(line.getOptionValue("c").split(","))
                        .map(x -> Integer.valueOf(x.trim()))
                        .collect(Collectors.toList());
            }

            int numberOfPlay = 10;
            if (line.hasOption("n")) {
                numberOfPlay = Integer.parseInt(line.getOptionValue("n"));
            }

            String script = new String(Files.readAllBytes(Paths.get(line.getOptionValue("f"))), StandardCharsets.UTF_8);

            System.out.println(
                    String.format(
                            "Start auto play.\n* Play parallel    : %s\n* Challenge numbers: %s\n* Number of play   : %s",
                            isParallel,
                            challengeNumbers,
                            numberOfPlay));

            long startTime = System.currentTimeMillis();

            List<ChallengeResult> results = isParallel
                    ? playChallengesParallel(challengeNumbers, script, numberOfPlay)
                    : playChallenges(challengeNumbers, script, numberOfPlay);

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
                            "Finish. Total time: %,d seconds",
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

        } catch (ParseException e) {
            System.out.println("Unexpected exception:" + e.getMessage());
            printUsage(options);
            return;
        }
    }

    private static void printUsage(Options options) {
        HelpFormatter help = new HelpFormatter();
        help.setWidth(100);

        // ヘルプを出力
        help.printHelp("java -jar elevator-saga-auto-play-all.jar", options, true);
        System.exit(1);
    }

    public static List<ChallengeResult> playChallengesParallel(
            List<Integer> challengeNumbers, String script, int numberOfPlay) {

        return challengeNumbers.stream()
                .parallel()
                .map(challengeNumber -> {
                    try (AutoPaly autoPaly = new AutoPaly()) {
                        try {
                            return autoPaly.play(challengeNumber, script, numberOfPlay);
                        } catch (Exception e) {
                            throw new AutoPlayException(challengeNumber, e);
                        }
                    }
                })
                .collect(Collectors.toList());
    }

    public static List<ChallengeResult> playAllChallengesParallel(String script, int numberOfPlay) {

        return playChallengesParallel(ALL_CHALLENGE_NUMBERS, script, numberOfPlay);
    }

    public static List<ChallengeResult> playChallenges(
            List<Integer> challengeNumbers, String script, int numberOfPlay) {

        try (AutoPaly autoPaly = new AutoPaly()) {

            return challengeNumbers.stream()
                    .map(challengeNumber -> {
                        try {
                            return autoPaly.play(challengeNumber, script, numberOfPlay);
                        } catch (Exception e) {
                            throw new AutoPlayException(challengeNumber, e);
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    public static List<ChallengeResult> playAllChallenges(String script, int numberOfPlay) {

        return playChallenges(ALL_CHALLENGE_NUMBERS, script, numberOfPlay);
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

    private boolean run() {

        // 実行
        driver.findElement(By.cssSelector("#button_apply")).click();

        // 結果取得(待ち合わせ)
        WebDriverWait wait = new WebDriverWait(driver, 300);
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

        // クリップボード操作が並列で実行されるとうまく動作しない場合があったので
        // 同時に実行されないように制御
        synchronized (lockObject) {

            // スクリプトの入力
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Clipboard clipboard = toolkit.getSystemClipboard();
            clipboard.setContents(new StringSelection(script), null);

            if (isWindows) {
                // Windows
                new Actions(driver)
                        .sendKeys(Keys.chord(Keys.CONTROL, "a"))
                        .sendKeys(Keys.chord(Keys.DELETE))
                        .sendKeys(Keys.chord(Keys.CONTROL, "v"))
                        .build()
                        .perform();

            } else {
                // Mac
                // https://stackoverflow.com/questions/11750447/performing-a-copy-and-paste-with-selenium-2
                new Actions(driver)
                        .sendKeys(Keys.chord(Keys.COMMAND, "a"))
                        .sendKeys(Keys.chord(Keys.DELETE))
                        .sendKeys(Keys.chord(Keys.SHIFT, Keys.INSERT))
                        .build()
                        .perform();
            }
        }
    }

    @Override
    public void close() {
        driver.quit();
    }

    private static class AutoPlayException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private AutoPlayException(int challengeNumber, Throwable cause) {
            super("An error occurred in Challenge #" + challengeNumber, cause);
        }
    }
}
