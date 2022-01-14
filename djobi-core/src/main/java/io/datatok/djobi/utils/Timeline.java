package io.datatok.djobi.utils;

import java.util.Calendar;
import java.util.Date;

public class Timeline {

    private Date start = Calendar.getInstance().getTime();
    private Date end;
    private long duration = 0;

    public Date getStart() {
        return start;
    }

    public Timeline start() {
        this.start = Calendar.getInstance().getTime();

        return this;
    }

    public Date getEnd() {
        return end;
    }

    public Timeline end() {
        this.end = Calendar.getInstance().getTime();

        this.duration = this.end.getTime() - this.start.getTime();

        return this;
    }

    public long getDuration() {
        return this.duration;
    }
}
