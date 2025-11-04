package lk.ijse.introductiontonetworkprogrammingexam;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientController {

    public VBox messageContainer;
    public Button btnConnect;
    public Button btnSend;
    private int port = 5000;
    Socket socket;
    DataOutputStream out;
    DataInputStream in;

    boolean isConnect = false;
    private String userName;

    @FXML
    private TextField txtHost;

    @FXML
    private TextField txtMessageBox;

    @FXML
    private TextField txtPort;

    @FXML
    private TextField txtUserName;

    @FXML
    void btnConnectOnAction(ActionEvent event) {
        if (isConnect) {
            ConnectionDisconnect();
            return;
        }

        String host = txtHost.getText();
        Integer port = Integer.parseInt(txtPort.getText());
        userName = txtUserName.getText();

        if (userName.isEmpty()) {
            showAlert("Please enter a username");
            return;
        }

        // Connect to Server
        try {
            socket = new Socket(host, port);
            System.out.println("CONNECTED TO SERVER: " + socket.getInetAddress().getHostAddress());

            isConnect = true;
            btnConnect.setText("DISCONNECT");

            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            // Send username to server
            out.writeUTF(userName);
            out.flush();

            // Start thread to listen for messages from server
            new Thread(() -> {
                try {
                    while (isConnect) {
                        String resMessage = in.readUTF();
                        Platform.runLater(() -> {
                            loadingMessages(resMessage, false);
                        });
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        if (isConnect) {
                            showAlert("Connection lost to server");
                            ConnectionDisconnect();
                        }
                    });
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Cannot connect to server: " + e.getMessage());
        }
    }

    // Disconnect server
    void ConnectionDisconnect() {
        try {
            isConnect = false;
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("CONNECTION DISCONNECTED!");
            socket = null;
            in = null;
            out = null;

            Platform.runLater(() -> {
                btnConnect.setText("CONNECT");
                txtUserName.setDisable(false);
                txtHost.setDisable(false);
                txtPort.setDisable(false);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnSendOnAction(ActionEvent event) {
        String message = txtMessageBox.getText().trim();

        if (message.isEmpty()) {
            showAlert("Please enter a message");
            return;
        }

        if (!isConnect) {
            showAlert("Not connected to server");
            return;
        }

        try {
            // Display message locally immediately
            loadingMessages("You: " + message, true);

            // Send message to server
            out.writeUTF(message);
            out.flush();
            System.out.println("Client sent: " + message);
        } catch (Exception e) {
            showAlert("Error sending message: " + e.getMessage());
            ConnectionDisconnect();
        } finally {
            txtMessageBox.clear();
        }
    }

    public void loadingMessages(String text, boolean isSent) {
        System.out.println("Displaying message: " + text);

        HBox messageRow = new HBox(10);
        messageRow.setAlignment(isSent ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setPadding(new Insets(5, 10, 5, 10));
        messageLabel.setStyle(
                "-fx-background-color: " + (isSent ? "#007bff" : "#e9ecef") + ";" +
                        "-fx-text-fill: " + (isSent ? "white" : "black") + ";" +
                        "-fx-background-radius: 15;" +
                        "-fx-font-size: 12pt;"
        );

        messageRow.getChildren().add(messageLabel);
        messageContainer.getChildren().add(messageRow);

        // Auto-scroll to bottom
        messageContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            messageContainer.setPrefHeight(newValue.doubleValue());
            messageContainer.layout();
            messageContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);
        });
    }

    private void showAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // Handle Enter key in message box
    @FXML
    void txtMessageBoxOnAction(ActionEvent event) {
        btnSendOnAction(event);
    }
}