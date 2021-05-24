package vaf;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeOptions;
import vaf.app.App;
import vaf.scrapper.AppointmentScanner;
import vaf.scrapper.AppointmentSetupScanner;
import vaf.scrapper.ScannerInstance;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public enum VAF {
    INSTANCE();

    public static ChromeOptions baseOptions = new ChromeOptions();
    public static ChromeOptions headlessOptions = new ChromeOptions();

    private static final String baseArguments = "--window-size=1920,1080 --no-sandbox --disable-gpu" +
            "--disable-crash-reporter --disable-extensions --disable-in-process-stack-traces" +
            "--disable-logging --disable-dev-shm-usage --log-level=3 --output=/dev/null";

    private static final String headlessArguments = "--headless";

    static {
        baseOptions.addArguments(baseArguments);
        headlessOptions.addArguments(baseArguments);
        headlessOptions.addArguments(headlessArguments);
    }

    public LocalDateTime maxDate;

    public final ScheduledExecutorService service = Executors.newScheduledThreadPool(8);
    private final Timer timer = new Timer();
    private final Map<ScannerInstance, AppointmentScanner> scanners = new HashMap<>();

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

    public void instantiateScanner(final Supplier<AppointmentScanner> supplier) {

        CompletableFuture
                .supplyAsync(supplier, service)
                .thenAcceptAsync(scanner -> {
                    scanners.put(scanner.scannerInstance, scanner);
                    App.INSTANCE.addScannerDisplay(scanner.scannerInstance);
                    scanner.schedule();
                }, service);
    }

    public void removeScanner(final ScannerInstance scannerInstance) {

        AppointmentScanner scanner = scanners.get(scannerInstance);
        if (scanner == null)
            return;

        VAF.INSTANCE.service.submit(() -> {
            scanner.stop();
            scanners.remove(scannerInstance);
        });
    }

    public <T> void instantiateScrapper(final Supplier<T> supplier) {
        service.submit(supplier::get);
    }

    public void loadCenters(final List<String> urls) {
        urls.forEach(url -> instantiateScrapper(() -> new AppointmentSetupScanner(url)));
    }

    public void clearScanners() {
        scanners.forEach((instance, display) -> VAF.INSTANCE.removeScanner(instance));
    }

    public void start() {

        // Setup drivers
        WebDriverManager.chromiumdriver().setup();

        // Launch scrappers
        VAF.INSTANCE.loadCenters(Arrays.asList(Program.urls));
    }

    public void shutdown() {
        service.shutdown();
        scanners.forEach((instance, scanner) -> scanner.stop());
    }
}
