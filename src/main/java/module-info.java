module lk.ijse.introductiontonetworkprogrammingexam {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;


    opens lk.ijse.introductiontonetworkprogrammingexam to javafx.fxml;
    exports lk.ijse.introductiontonetworkprogrammingexam;
}