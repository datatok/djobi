package io.datatok.djobi.engine.utils;

public class StageCandidate {

    private final Integer score;

    private final String fullNameQualified;

    public StageCandidate(String fullNameQualified, Integer score) {
        this.score = score;
        this.fullNameQualified = fullNameQualified;
    }

    public Integer getScore() {
        return score;
    }

    public String getFullNameQualified() {
        return fullNameQualified;
    }
}
