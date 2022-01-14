package io.datatok.djobi.engine.parameters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import io.datatok.djobi.engine.Parameter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateParameter extends Parameter {

    private Calendar calendar;

    static SimpleDateFormat descFormat = new SimpleDateFormat("yyyy-MM-dd");

    public DateParameter(final String id, final String date) throws ParseException {
        this(id, Calendar.getInstance());

        final SimpleDateFormat dateParser = new SimpleDateFormat("dd/MM/yy");

        calendar.setTime(dateParser.parse(date));
    }

    public DateParameter(final String id, final Calendar calendar) {
        super(id, calendar);

        this.calendar = calendar;
    }

    public String getDay() {
        return String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }

    public String getDay0() {
        return String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
    }

    public String getMonth() {
        return String.valueOf(1 + calendar.get(Calendar.MONTH));
    }

    public String getMonth0() {
        return String.format("%02d", 1 + calendar.get(Calendar.MONTH));
    }

    public String getYear() {
        return String.valueOf(calendar.get(Calendar.YEAR));
    }

    @JsonIgnore
    public DateParameter getDayBefore() {
        final Calendar cc = (Calendar) calendar.clone();

        cc.add(Calendar.DAY_OF_MONTH,-1);

        return new DateParameter(getId() + "_day_before", cc);
    }

    @JsonIgnore
    public DateParameter getDayAfter() {
        final Calendar cc = (Calendar) calendar.clone();

        cc.add(Calendar.DAY_OF_MONTH,1);

        return new DateParameter(getId() + "_day_before", cc);
    }

    @Override
    public String getValueDescription() {
        return descFormat.format(calendar.getTime());
    }

    public String getValueAsString() {
        return this.toString();
    }

    @Override
    @JsonValue
    public String toString() {
        return descFormat.format(calendar.getTime());
    }
}
