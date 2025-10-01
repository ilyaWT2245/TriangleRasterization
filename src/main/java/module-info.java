module com.vsu.cs.cg2025.g12.konchakov_i_v.trianglerasterization.trianglerasterization {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.vsu.cs.cg2025.g12.konchakov_i_v.trianglerasterization.trianglerasterization to javafx.fxml;
    exports com.vsu.cs.cg2025.g12.konchakov_i_v.trianglerasterization.trianglerasterization;
}