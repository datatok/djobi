package io.datatok.djobi.engine.check;

import io.datatok.djobi.utils.MyMapUtils;

import java.util.HashMap;
import java.util.Map;

public class CheckResult {

    static final public String REASON = "reason";

    private CheckStatus status = CheckStatus.TODO;

    private Map<String, Object> meta;

    public CheckResult() {
    }

    public CheckResult(CheckStatus status) {
        this.status = status;
    }

    public CheckResult(CheckStatus status, Map<String, Object> meta) {
        this.status = status;
        this.meta = meta;
    }

    static public CheckResult todo() {
        return new CheckResult(CheckStatus.TODO);
    }

    static public CheckResult no() {
        return new CheckResult(CheckStatus.NO);
    }

    static public CheckResult ok(Object... args) {
        return new CheckResult(CheckStatus.DONE_OK, MyMapUtils.map(args));
    }

    static public CheckResult error(String reason) {
        return new CheckResult(CheckStatus.DONE_ERROR, MyMapUtils.map("reason", reason));
    }

    public CheckStatus getStatus() {
        return status;
    }

    public CheckResult setStatus(CheckStatus status) {
        this.status = status;
        return this;
    }

    public Object getMeta(String k) {
        return meta.get(k);
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public CheckResult putMeta(String k, Object v) {
        if (this.meta == null) {
            this.meta = new HashMap<>();
        }

        this.meta.put(k, v);

        return this;
    }
}
