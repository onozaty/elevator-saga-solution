package com.enjoyxstudy.elevatorsaga;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

public class AutoPalyTest {

    @Test
    public void play() throws URISyntaxException, IOException {

        Path scriptPath = Paths.get(AutoPalyTest.class.getResource("script.txt").toURI());
        String script = new String(Files.readAllBytes(scriptPath), StandardCharsets.UTF_8);

        try (AutoPaly autoPaly = new AutoPaly()) {
            ChallengeResult result = autoPaly.play(1, script, 3);

            assertThat(result.getChallengeNumber()).isEqualTo(1);
            assertThat(result.getSuccessCount()).isGreaterThan(0);
            assertThat(result.getSuccessCount() + result.getFailedCount()).isEqualTo(3);
        }
    }

    @Test
    public void playAllChallenge() throws URISyntaxException, IOException {

        Path scriptPath = Paths.get(AutoPalyTest.class.getResource("script.txt").toURI());
        String script = new String(Files.readAllBytes(scriptPath), StandardCharsets.UTF_8);

        List<ChallengeResult> results = AutoPaly.playAllChallenges(script, 2);

        assertThat(results)
                .extracting(ChallengeResult::getChallengeNumber)
                .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18);
        assertThat(results)
                .extracting(x -> x.getSuccessCount() + x.getFailedCount())
                .containsOnly(2);
    }

    @Test
    public void playAllChallengeParallel() throws URISyntaxException, IOException {

        Path scriptPath = Paths.get(AutoPalyTest.class.getResource("script.txt").toURI());
        String script = new String(Files.readAllBytes(scriptPath), StandardCharsets.UTF_8);

        List<ChallengeResult> results = AutoPaly.playAllChallengesParallel(script, 2);

        assertThat(results)
                .extracting(ChallengeResult::getChallengeNumber)
                .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18);
        assertThat(results)
                .extracting(x -> x.getSuccessCount() + x.getFailedCount())
                .containsOnly(2);
    }

}
