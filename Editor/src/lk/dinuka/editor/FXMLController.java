package lk.dinuka.editor;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.print.attribute.standard.DialogTypeSelection;

import org.json.JSONObject;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.sun.org.apache.bcel.internal.generic.NEW;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Dialogs;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class FXMLController implements Initializable {
	@FXML
	Button openbtn;
	@FXML
	TextArea textArea;
	@FXML
	Label errorMsg;
	@FXML
	TextField passText;
	private static CryptoManager cryptoManager = new CryptoManager();
	private JSONObject currentFileJO;
	private static String initVector = "RandomInitVector";
	private File tempFile = null;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.out.println("FXML initialized");
		// creates a JSONObject and add data and status
		currentFileJO = new JSONObject();
		currentFileJO.put("data", "");
		currentFileJO.put("status", "");
	}

	public void openFile() {
		currentFileJO = new JSONObject();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("open .tms files only");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(".tms files", "*.tms"),
				new FileChooser.ExtensionFilter("All files", "*.*"));
		Window stage = null;
		File file = fileChooser.showOpenDialog(stage);
		if (file != null) {
			System.out.println(file.getPath().toString());
			// add the opened file to tempFile
			tempFile = file;
			try {
				// read the file content
				String inputFileString = Files.toString(file, Charsets.UTF_8);
				// add the read string to the currentFileJO
				currentFileJO = new JSONObject(inputFileString);
				jsonFilter(currentFileJO);
			} catch (Exception e) {
				// JSONException means the file content is not in a valid format
				// valid format is
				// {"data":" ","status":"visible|hidden"}
				showAlartDialog(AlertType.ERROR, "ERROR !", "", "File is corrupted or not a valid file format");
				e.printStackTrace();
			}
		}
	}

	public void saveFile() {
		File toSaveFile;
		Window stage = null;
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save File");
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(".tms files", "*.tms"),
				new FileChooser.ExtensionFilter("All files", "*.*"));
		if (tempFile == null) {
			// if the content is not from a file opened
			fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
			fileChooser.setInitialFileName("new.tms");
			toSaveFile = fileChooser.showSaveDialog(stage);
		} else {
			// if a file is edited
			// set the path of the edited file
			fileChooser.setInitialDirectory(tempFile.getParentFile());
			// set the name of the edited file
			fileChooser.setInitialFileName(tempFile.getName());
			toSaveFile = fileChooser.showSaveDialog(stage);
		}
		if (toSaveFile != null) {
			try {
				// save the file using google-guava
				Files.write(currentFileJO.toString(), toSaveFile, Charsets.UTF_8);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// clear the text fields and currentFileJO
		// set tempFile to null
		clearAll();
	}

	public void encryptText() {
		if (passText.getText().isEmpty()) {
			// password must be >0 and <16
			showAlartDialog(AlertType.WARNING, "Warning !", "", "Password cannot be empty !");
		} else {
			String pass = passText.getText();
			if (pass.length() > 16) {
				showAlartDialog(AlertType.WARNING, "Warning !", "",
						"Password should be less than 16 characters \nA-Z a-z 0-9");
			} else {
				// stats hidden means the data is already encrypted
				// not alowed to encrypt again
				if ((currentFileJO.getString("status")).equals("hidden")) {
					showAlartDialog(AlertType.WARNING, "Warning !", "", "Data is already encrypted");
				} else {
					// if the data is not encrypted
					String encrypted = null;
					try {
						// encrypt the data
						encrypted = cryptoManager.encrypt(cryptoManager.passwordPadding(pass), initVector,
								textArea.getText());
						// set encrypted data to the JSONObject
						currentFileJO.put("data", encrypted);
						// change status to hidden
						currentFileJO.put("status", "hidden");
						jsonFilter(currentFileJO);
					} catch (IllegalBlockSizeException | UnsupportedEncodingException | NoSuchAlgorithmException
							| NoSuchPaddingException | InvalidAlgorithmParameterException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidKeyException | BadPaddingException e) {
						// TODO: handle exception
						// Handled if the password is invalid
						showAlartDialog(AlertType.ERROR, "Error !", "", "Password is INVALID !");
					}

				}
			}
		}
	}

	public void decryptText() {
		if (passText.getText().isEmpty()) {
			showAlartDialog(AlertType.WARNING, "Warning !", "", "Password cannot be empty !");
		} else {
			String pass = passText.getText();
			if (pass.length() > 16) {
				showAlartDialog(AlertType.WARNING, "Warning !", "",
						"Password should be less than 16 characters \nA-Z a-z 0-9");
			} else {
				// decrypt only if the status id hidden
				if ((currentFileJO.getString("status")).equals("hidden")) {
					String decrypted = null;
					try {
						decrypted = cryptoManager.decrypt(cryptoManager.passwordPadding(pass), initVector,
								textArea.getText());
						// set the decrypted data to the currentFileJO
						currentFileJO.put("data", decrypted);
						// set the status as visible
						currentFileJO.put("status", "visible");
						jsonFilter(currentFileJO);
					} catch (IllegalBlockSizeException | UnsupportedEncodingException | NoSuchAlgorithmException
							| NoSuchPaddingException | InvalidAlgorithmParameterException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidKeyException | BadPaddingException e) {
						// TODO: handle exception
						showAlartDialog(AlertType.ERROR, "Error !", "", "Password is INVALID !");
					}

				} else {
					showAlartDialog(AlertType.WARNING, "Warning !", "", "Text is alredy in visible state");
				}
			}
		}
	}

	public void showAlartDialog(AlertType alertType, String title, String header, String content) {
		try {
			Alert alert = new Alert(alertType);
			alert.setTitle(title);
			alert.setHeaderText(header);
			alert.setContentText(content);
			alert.showAndWait();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void jsonFilter(JSONObject jo) {
		// get data field from the JSONObject and set it to textArea
		String data = jo.get("data").toString();
		textArea.setText(data);
	}

	public void clearAll() {
		textArea.setText("");
		passText.setText("");
		currentFileJO.put("data", "");
		currentFileJO.put("status", "");
		tempFile = null;
	}

}
