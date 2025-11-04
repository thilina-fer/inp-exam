module lk.ijse.demo {
    requires javafx.controls;
    requires javafx.fxml;


    opens lk.ijse.demo to javafx.fxml;
    exports lk.ijse.demo;
}