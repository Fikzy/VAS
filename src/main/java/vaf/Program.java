package vaf;

import javafx.application.Application;
import javafx.stage.Stage;
import vaf.app.App;
import vaf.scrapper.Browser;

public class Program extends Application {

    public static String[] urls = {
//            "https://www.doctolib.fr/centre-de-sante/antony/centre-de-vaccination-antony-bourg-la-reine-sceaux?highlight%5Bspeciality_ids%5D%5B%5D=5494",
//            "https://www.doctolib.fr/centre-de-vaccinations-internationales/longjumeau/centre-de-vaccination-covid-19-ville-de-longjumeau?highlight%5Bspeciality_ids%5D%5B%5D=5494",
//            "https://www.doctolib.fr/centre-de-sante/paray-vieille-poste/centre-de-vaccination-covid-19-cpts-nord-essonne-paray-vieille-poste-et-morangis?highlight%5Bspeciality_ids%5D%5B%5D=5494",
//            "https://www.doctolib.fr/vaccination-covid-19/palaiseau/centre-de-vaccination-covid-19-de-palaiseau-complexe-sportif-j-allain?highlight%5Bspeciality_ids%5D%5B%5D=5494",
//            "https://www.doctolib.fr/vaccination-covid-19/toulon/centre-de-vaccination-covid-19-du-var?highlight%5Bspeciality_ids%5D%5B%5D=5494",
    };

    public static void main(String... args) {

        // Start UI
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {

        stage.setOnCloseRequest(windowEvent -> {
            VAF.INSTANCE.shutdown();
            System.exit(0);
        });

        Browser.selectBrowser();

        App.INSTANCE.start(stage);
    }
}
