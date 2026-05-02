module lk.ijse.theserenitymentalhealththerapycenter {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires java.naming;
    requires static lombok;
    requires jbcrypt;
    requires mysql.connector.j;

    opens lk.ijse.theserenitymentalhealththerapycenter to javafx.fxml;
    opens lk.ijse.theserenitymentalhealththerapycenter.controller to javafx.fxml;
    opens lk.ijse.theserenitymentalhealththerapycenter.entity to org.hibernate.orm.core, javafx.base;

    exports lk.ijse.theserenitymentalhealththerapycenter;
    exports lk.ijse.theserenitymentalhealththerapycenter.controller;
    exports lk.ijse.theserenitymentalhealththerapycenter.entity;
}