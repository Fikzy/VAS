package vaf;

import io.reactivex.rxjava3.subjects.PublishSubject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import vaf.scrapper.Scanner;
import vaf.scrapper.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public enum VAF {
    INSTANCE();

    public LocalDateTime maxDate;

    public final ScheduledExecutorService service = Executors.newScheduledThreadPool(8);
    private final Timer timer = new Timer();

    public final ObservableSet<ScannerProfile> profiles = FXCollections.observableSet(new HashSet<>());

    public final List<Scrapper> scrappers = new ArrayList<>();

    public final CenterSearcher centerSearcher = new CenterSearcher();
    public final ProfileFactory profileFactory = new ProfileFactory();
    private final Scanner scanner = new Scanner();
    private final AtomicBoolean scanning = new AtomicBoolean(false);

    public final BlockingDeque<ScannerProfile> queuedProfiles = new LinkedBlockingDeque<>();

    public final PublishSubject<ScannerProfile> onScannerProfileAdd = PublishSubject.create();
    public final PublishSubject<ScannerProfile> onScannerProfileRemove = PublishSubject.create();
    public final PublishSubject<ScannerProfile> onScannerStartScan = PublishSubject.create();
    public final PublishSubject<ScannerProfile> onScannerSuccessfulScan = PublishSubject.create();
    public final PublishSubject<ScannerProfile> onScannerStopScan = PublishSubject.create();

    VAF() {
        updateMaxDate();
        scrappers.add(centerSearcher);
        scrappers.add(profileFactory);
        scrappers.add(scanner);
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

    public void enqueueScannerProfile(final ScannerProfile profile) {
        if (!queuedProfiles.contains(profile)) {
            queuedProfiles.add(profile);
            if (queuedProfiles.size() == 1)
                startScanning();
        }
    }

    public void dequeueScannerProfile(final ScannerProfile profile) {
        queuedProfiles.remove(profile);
        if (queuedProfiles.isEmpty())
            stopScanning();
    }

    public void addScannerProfile(final ScannerProfile profile) {

        if (profiles.contains(profile)) {
            System.err.println("Profile already in set");
            return;
        }

        System.out.println("Adding: " + profile);
        VAF.INSTANCE.profiles.add(profile);
        queuedProfiles.add(profile);
        onScannerProfileAdd.onNext(profile);
    }

    public ScannerProfile processScannerProfile() {
        final ScannerProfile profile = queuedProfiles.poll();
        if (profile != null)
            queuedProfiles.add(profile);
        return profile;
    }

    public void removeScannerProfile(final ScannerProfile profile) {
        if (queuedProfiles.remove(profile)) {
            System.out.println("Removing: " + profile);
            profiles.remove(profile);
            onScannerProfileRemove.onNext(profile);
        }
    }

    public void startScanning() {
        if (scanning.get())
            return;
        scanning.set(true);
        System.out.println("Started scanning");
        service.submit(() -> {
            while (scanning.get())
                scanner.scan();
        });
    }

    public void stopScanning() {
        if (!scanning.get())
            return;
        scanning.set(false);
        System.out.println("Stopped scanning");
    }

    public void shutdown() {
        stopScanning();
        service.shutdown();
        scrappers.forEach(Scrapper::dispose);
    }
}
