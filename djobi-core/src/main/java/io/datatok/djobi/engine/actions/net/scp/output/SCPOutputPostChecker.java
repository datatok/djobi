package io.datatok.djobi.engine.actions.net.scp.output;

import com.google.inject.Inject;
import io.datatok.djobi.engine.actions.net.checkers.listing.HttpListing;
import io.datatok.djobi.engine.actions.net.checkers.listing.ListingPool;
import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionPostChecker;
import org.apache.commons.io.FileUtils;

import java.util.Calendar;
import java.util.Date;

public class SCPOutputPostChecker implements ActionPostChecker {

    @Inject
    ListingPool listingPool;

    @Override
    public CheckResult postCheck(Stage stage) throws Exception {

        final SCPOutputConfig parameters = (SCPOutputConfig) stage.getParameters();

        final HttpListing.Record record = listingPool.getFile(parameters.checkService, parameters.path);

        if (record == null) {
            return ActionPostChecker.error(String.format("not found (%s)!", parameters.path));
        }

        if (record.size < 256) {
            return ActionPostChecker.error(String.format("empty (%s = %s)!", parameters.path, record.size));
        }

        final Date dateToCompare = getDateToCompare();

        if (record.date.before(dateToCompare)) {
            return ActionPostChecker.error(String.format("File \"%s\" is too old (%s)!", parameters.path, record.date.toString()));
        }

        return CheckResult.ok(
            "display", FileUtils.byteCountToDisplaySize(record.size),
            "unit", "byte",
            "value", record.size
        );
    }

    private Date getDateToCompare() {
        Calendar calendarToCompare = Calendar.getInstance();

        calendarToCompare.add(Calendar.DATE, -1);

        return calendarToCompare.getTime();
    }
}
