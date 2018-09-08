import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

public class MainViewController {
	public static ArrayList<Rule> rules = new ArrayList<Rule>();
	
	@FXML
	public TextField wordTextField;
	
	@FXML
	public TextField ruleLeftTextField;
	@FXML
	public TextField ruleRightTextField;
	@FXML
	public Button addRuleButton;
	@FXML
	public ListView<String> ruleListView;
	
	@FXML
	public Button ruleUpButton;
	@FXML
	public Button ruleDownButton;
	@FXML
	public Button ruleSaveButton;
	@FXML
	public Button ruleLoadButton;

	@FXML
	public TextArea outputArea;
	
	@FXML
	public void handleLoad() throws IOException {
		FileChooser fc = new FileChooser();
		fc.setTitle("Виберіть файл з правилами");
		fc.setInitialDirectory(new File(System.getProperty("user.dir")));
		File tempFile = fc.showOpenDialog(Main.inst.stage);
		if (tempFile != null){
			
			rules.clear();
			ruleListView.getItems().clear();
			
			BufferedReader br = new BufferedReader(new FileReader(tempFile));
			String tempString;
			StringBuilder tempFrom = new StringBuilder(), tempTo = new StringBuilder();
			boolean l;
			char tempChar;
			
			while((tempString = br.readLine()) != null){
				tempFrom.delete(0, tempFrom.length());
				tempTo.delete(0, tempTo.length());
				l = true;
				
				for(int i = 0; i < tempString.length(); i++){
					tempChar = tempString.charAt(i);
					if (tempChar == '=' || tempChar == '>'){l = false;continue;}

					if (l)tempFrom.append(tempChar);else tempTo.append(tempChar);
				}
				
				addRule(tempFrom.toString(), tempTo.toString());
				
			}
			br.close();
		}
	}

	@FXML
	public void handleRuleDeletion() {
		int selectedRuleIndex = ruleListView.getSelectionModel().getSelectedIndex();
		String ruleContent = ruleListView.getItems().get(selectedRuleIndex);
		ruleListView.getItems().remove(selectedRuleIndex);
		for (Iterator<Rule> iterator = rules.iterator(); iterator.hasNext();) {
			Rule rule = iterator.next();
			if (rule.toString().equals(ruleContent)){
				iterator.remove();
				return;
			}
		}
	}


	
	@FXML
	public void handleSave() throws IOException {
	
		FileChooser fc = new FileChooser();
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fc.setInitialDirectory(new File(System.getProperty("user.dir")));
		fc.getExtensionFilters().add(extFilter);
		fc.setTitle("Виберіть куди зберегти правила");
		File tempFile = fc.showSaveDialog(Main.inst.stage);
		if (tempFile == null)return;
		BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
		for(Rule rule : rules)bw.write(rule.toString()+"\n");
		bw.close();
	}
	
	@FXML
	public void handleUp(){
		swapElement(-1);
	}
	
	private void swapElement(int sigh){
		int index = ruleListView.getSelectionModel().getSelectedIndex();
		if (index == -1 || index + sigh > rules.size()-1 || index + sigh < 0)return;
		Platform.runLater(new Runnable() {
		    @Override
		    public void run() {
		    	ruleListView.scrollTo(index + sigh);
				ruleListView.getSelectionModel().select(index + sigh);
		    }
		});
		
		String tempString = ruleListView.getItems().get(index);
		ruleListView.getItems().remove(index);
		ruleListView.getItems().add(index + sigh, tempString);
		Rule tempRule = rules.get(index);
		rules.remove(index);
		rules.add(index, tempRule);
	}
	
	@FXML
	public void handleDown(){
		swapElement(1);
	}

	@FXML
	public void handleAddRule(){
		String from = ruleLeftTextField.getText();
		String to = ruleRightTextField.getText();
		
		addRule(from, to);
	}
	
	private void addRule(String from, String to){
		for(Rule r : rules){
			if(r.getFrom().equals(from)){
				return;
			}
		}
		rules.add(new Rule(from, to));
		ruleRightTextField.clear();
		ruleLeftTextField.clear();
		ruleListView.getItems().add(rules.get(rules.size()-1).toString());
	}
	
	@FXML
	public void handleCalculate() throws IOException {
		ArrayList<String> words = new ArrayList<>();
		words.add(wordTextField.getText());
		// main loop
		m: while(true){
			
			for(Rule r : rules){
				if (r.canApply(words.get(words.size()-1))){
					words.add(r.apply(words.get(words.size()-1)));
					if (r.isLast())
						break m;
					else
						continue m;
				}
			}
		
			break;
			
		}

		StringBuilder builder = new StringBuilder("");
		for (String s : words) {
			builder.append(s);
			builder.append("\n");
		}

		outputArea.setText(builder.toString());
	}

	@FXML
	public void handleInfiniteLoop() throws IOException {
		for (int i = 0; i < rules.size(); i++){
			Rule temporaryRule = rules.get(i);
			for (int j = 0; j < rules.size(); j++){
				Rule ruleToCompareWith = rules.get(j);
				if (temporaryRule.getTo().equals(ruleToCompareWith.getFrom()) && temporaryRule.getFrom().equals(ruleToCompareWith.getTo())){
					Alert infiniteLoopConfirmation = new Alert(Alert.AlertType.CONFIRMATION);
					infiniteLoopConfirmation.setTitle("Виявлено зациклення");
					infiniteLoopConfirmation.setHeaderText("У ваших правилах було виявлено зациклення.");
					infiniteLoopConfirmation.setContentText("Ви точно хочете продовжити?");

					Optional<ButtonType> type = infiniteLoopConfirmation.showAndWait();
					if (type.get() == ButtonType.OK){
						handleCalculate();
						return;
					}else return;
				}
			}
		}
		handleCalculate();
	}
}
