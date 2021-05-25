package vaf;

import io.reactivex.rxjava3.subjects.PublishSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public final List<Scanner> scanners = new ArrayList<>();
    public final List<ScannerProfile> scannerProfiles = new ArrayList<>();

    public final PublishSubject<ScannerProfile> onScannerProfileAdd = PublishSubject.create();
    public final PublishSubject<ScannerProfile> onScannerProfileRemove = PublishSubject.create();

    public final PublishSubject<ScannerProfile> onScannerStartScan = PublishSubject.create();
    public final PublishSubject<ScannerProfile> onScannerSuccessfulScan = PublishSubject.create();
    public final PublishSubject<ScannerProfile> onScannerStopScan = PublishSubject.create();

    private Logger log = LoggerFactory.getLogger(this.getClass());

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
        log.info("Adding: " + profile);
        scannerProfiles.add(profile);
        onScannerProfileAdd.onNext(profile);
    }

    public void removeScannerProfile(final ScannerProfile profile) {
        log.info("Removing: " + profile);
        if (scannerProfiles.remove(profile))
            onScannerProfileRemove.onNext(profile);
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
