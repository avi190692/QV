package com.quickveggies;
//GLOBAL METHODS FOR GENERAL USE
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.quickveggies.controller.CustomDialogController;
import com.quickveggies.controller.FreshEntryController;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class GeneralMethods {
	
 public static final String emailPattern="^[A-Za-z0-9+_.-]+@(.+)$";
	
 public static void errorMsg(String msg){
	 Alert alert = new Alert(Alert.AlertType.WARNING);
     alert.setTitle("Error!");
     alert.setHeaderText(null);
     alert.setContentText(msg);
     alert.showAndWait();
 }
 
public static ArrayList<Node> getAllNodes(Parent root) {
    ArrayList<Node> nodes = new ArrayList<Node>();
    for (Node node : root.getChildrenUnmodifiable()) {
        nodes.add(node);
        try{TextField field=(TextField)node;}
        catch(Exception e){}
    }
    return nodes;
}

public static void setTraversePolicy(Scene scene,final ObservableList<Node> nodes,final KeyCode keyCode){
    Scene currentScene=scene;//saveButton.getScene();
    currentScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
        public void handle(KeyEvent event) {
         if (event.getCode() == keyCode) {
              //check which node has the focus and focus the next one
        	  for(Node currentNode : nodes){
        		  if(currentNode==null)continue;
        		  if(currentNode.isFocused()){
        			  int currInd=nodes.indexOf(currentNode)+1;
        			  if(currInd<nodes.size())nodes.get(currInd).requestFocus();
        			  else nodes.get(0).requestFocus();
        			  }
        	  }
            }
        }
    });
}

public static ArrayList<TextField> getAllTxtFields(Parent root) {
	ArrayList<TextField> result=new ArrayList<TextField>();
	for(Node node : getAllNodes(root)){
		try{result.add((TextField)node);}
        catch(Exception e){continue;}
	}
	return result;
}

 
 public static void confirm(Button[] buttons,Stage dialogStage,Object callingObj,String msg){
     final Stage stage = dialogStage;
     stage.centerOnScreen();
     stage.setTitle("Confirmation");
     stage.initModality(Modality.WINDOW_MODAL);
     
     try {
    	 FXMLLoader loader=new FXMLLoader(callingObj.getClass().getResource("/fxml/customdialog.fxml"));
     	CustomDialogController controller=new CustomDialogController(buttons, msg);
     	loader.setController(controller);
         Parent parent = loader.load();
         Scene scene = new Scene(parent);
         
//         scene.setOnMouseClicked(new EventHandler<MouseEvent>() {
//             public void handle(MouseEvent event) {
//              stage.close();	 
//             }
//         });
         
         scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
             public void handle(KeyEvent event) {
              if (event.getCode() == KeyCode.ESCAPE) {
            	  stage.close();
                 }
             }
         });
         stage.setScene(scene);
         stage.show();
     } catch (IOException e) {
         e.printStackTrace();
     }
	 
 }
 
 public static void msg(String msg){
	 Alert alert = new Alert(Alert.AlertType.INFORMATION);
     alert.setTitle("Notification");
     alert.setHeaderText(null);
     alert.setContentText(msg);
     alert.showAndWait();
 }
 
 public static void openNewWindow(javafx.fxml.Initializable callingWindow,String fxmlRes,Object controller,EventHandler<WindowEvent> onHidingEventHandler){
     final Stage stage = new Stage();
     stage.centerOnScreen();
     stage.initModality(Modality.APPLICATION_MODAL);
     try {
     	FXMLLoader newStageLoader=new FXMLLoader(callingWindow.getClass().getResource(fxmlRes));
     	Parent parent=null;
     	
     	if(controller!=null){
     		newStageLoader.setController(controller);
            parent = newStageLoader.load();
     	}else parent=FXMLLoader.load(callingWindow.getClass().getResource(fxmlRes));

         Scene scene = new Scene(parent);
         scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
             public void handle(KeyEvent event) {
                 if (event.getCode() == KeyCode.ESCAPE) {
                 	stage.close();
                 }
             }
         });
        stage.setScene(scene);
        
        if(onHidingEventHandler!=null)stage.setOnHiding(onHidingEventHandler);
        
	     stage.show();
     } catch (IOException e) {
         e.printStackTrace();
     }
 
 }
  
 
 
 public static ChangeListener<Boolean> createTxtFieldChangeListener(final TextField field,final Integer minval,final Integer maxval,final TextField[] linkedTxtFields){
	 return(
	  new ChangeListener<Boolean>()
	  {
	     @Override
	     public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
	     {	
	     	if(newPropertyValue){}
	     	else
	         { 
	     	   String fieldTxt=field.getText();
	            if(!fieldTxt.equals("")){
	         	try{
	             int newValue=Integer.parseInt(fieldTxt);
	             if(minval!=null)
	              if(newValue<minval){GeneralMethods.errorMsg("Value must be >"+minval);
	              field.setText(field.getPromptText());}
	             if(maxval!=null)
	              if(newValue>maxval){GeneralMethods.errorMsg("Value must be <"+maxval);
	                  field.setText(field.getPromptText());}
	              
	             //update linked text fields
	             if(linkedTxtFields!=null){
	              for(TextField linkedField : linkedTxtFields){
	             	linkedField.setText(field.getText());
	               }
	              }

	             }catch(Exception e){GeneralMethods.errorMsg("Value must be integer!");field.setText(field.getPromptText());}
	            }
	         }
	     }
	  });
	 }
 
 @SuppressWarnings({ "unchecked", "rawtypes" })
public static void refreshTableView(TableView table, ObservableList listOfNewRows){
	 ObservableList oldRows=table.getItems();
	 table.setItems(null);
	 table.layout();
	 if(listOfNewRows==null)
		 table.setItems(oldRows);
	 else table.setItems(listOfNewRows);
 }

 public static void printStringArray(String[] array){
     for(String str : array)System.out.print(str+" ");
     System.out.print("\n");
 }
 
 
 //FORMS
 //=----------------------

public static void fitTableViewHeight(TableView table,int cellSize){
	table.setFixedCellSize(cellSize);
	table.prefHeightProperty().bind(table.fixedCellSizeProperty().multiply(Bindings.size(table.getItems()).add(1.01)));
	table.minHeightProperty().bind(table.prefHeightProperty());
	table.maxHeightProperty().bind(table.prefHeightProperty());
}
public static void fixTableViewWidth(TableView table,double width){
	table.prefWidthProperty().set(width);
	table.minWidthProperty().bind(table.prefWidthProperty());
	table.maxWidthProperty().bind(table.prefWidthProperty());
	
}

public static void fixListViewHeight(ListView list,double height){
	list.prefHeightProperty().set(height);
	list.minHeightProperty().bind(list.prefHeightProperty());
	list.maxHeightProperty().bind(list.prefHeightProperty());
	
}
public static void fixListViewWidth(ListView list,double width){
	list.prefWidthProperty().set(width);
	list.minWidthProperty().bind(list.prefWidthProperty());
	list.maxWidthProperty().bind(list.prefWidthProperty());
	
}

public static void fixTableColumnWidth(TableView table,double width){
   ObservableList cols=table.getColumns();
   for(Object col : cols){
	   ((TableColumn)col).setPrefWidth(width);
   }
}

public static void fixListCellWidth(ListView listView,double width){
	   ObservableList cells=listView.getItems();
	   for(Object cell : cells){
		   ((ListCell)cell).setPrefWidth(width);
	   }
	}
//---------------------------------

}//end of GeneralMethods

