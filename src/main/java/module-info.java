module com.example.turingemulator {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;

    opens com.example.turingemulator to javafx.fxml;
    exports com.example.turingemulator;
    exports com.example.turingemulator.controller;
    opens com.example.turingemulator.controller to javafx.fxml;
}