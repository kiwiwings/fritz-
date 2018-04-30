
module de.kiwiwings.monfritz {
    requires java.xml.bind;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    exports de.kiwiwings.monfritz;
    exports eu.hansolo.fx.smoothcharts;
    opens de.kiwiwings.monfritz to java.xml.bind;
}