package vaf;

import vaf.scrapper.ProfileFactory;
import vaf.scrapper.Scanner;
import vaf.scrapper.ScannerProfile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public enum VAF {
    INSTANCE();

    public LocalDateTime maxDate;

    public final ScheduledExecutorService service = Executors.newScheduledThreadPool(8);
    private final Timer timer = new Timer();

    public final List<ScannerProfile> scannerProfiles = new ArrayList<>();
    public final List<Scanner> scanners = new ArrayList<>();

    VAF() {
        updateMaxDate();
    }

    public void updateMaxDate() {
        this.maxDate = DateUtils.getZeroedDateOffset(2);
        Date nextUpdate = Date.from(DateUtils.getZeroedDateOffset(1).atZone(ZoneId.systemDefault()).toInstant());
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateMaxDate();
            }
        }, nextUpdate);
    }

    public void addScannerProfile(final ScannerProfile profile) {
        System.out.println(profile);
        scannerProfiles.add(profile);
    }

    public void start() {

        service.submit(() -> {
            ProfileFactory profileFactory = new ProfileFactory();
            profileFactory.generateProfiles(Arrays.asList(Program.urls));
            profileFactory.dispose();

            Scanner scanner = new Scanner();
            scanners.add(scanner);
            scanner.scan(scannerProfiles);
        });
    }

    public void shutdown() {
        service.shutdown();
        scanners.forEach(Scanner::dispose);
    }
}
