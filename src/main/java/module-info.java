module com.xmu.notepad {
    requires javafx.controls;
    requires javafx.fxml;
            
                            
    opens com.xmu.controllers to javafx.fxml;
    exports com.xmu.controllers;
    exports com.xmu;
    opens com.xmu to javafx.fxml;
}