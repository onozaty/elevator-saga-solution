package com.enjoyxstudy.elevatorsaga;

import lombok.Value;

@Value
public class ChallengeResult {

    private int challengeNumber;

    private int successCount;

    private int failedCount;
    
    public int getTotalCount() {
        return successCount + failedCount;
    }
}
