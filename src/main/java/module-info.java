
module de.kiwiwings.monfritz {
    requires jakarta.xml.bind;
    requires jakarta.activation;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    exports de.kiwiwings.monfritz;
    exports eu.hansolo.fx.smoothcharts;
    opens de.kiwiwings.monfritz to jakarta.xml.bind;
}