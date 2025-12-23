package com.example.sptdataanalysisandreportingtool;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField tfUser;
    @FXML private PasswordField tfPass;

    @FXML
    private void login() {
        String u=tfUser.getText();

        if(u.equalsIgnoreCase("sub")) Session.role="SUB";
        else if(u.equalsIgnoreCase("senior")) Session.role="SENIOR";
        else return;

        try {
            FXMLLoader f=new FXMLLoader(
                    getClass().getResource("dashboard-view.fxml")
            );
            Stage s=(Stage)tfUser.getScene().getWindow();
            s.setScene(new Scene(f.load()));
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
