package actions;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import mainline.UserInterface.TabNames;

/*******
 * <p> Title: EffortCategorySpecific Class. </p>
 * 
 * <p> Description: A JavaFX application: This controller class establishes the user interface for
 * EffortCategories Tab objects by inheriting the ListItem and building upon it.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2019-05-11 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.01	2019-06-05 Baseline
 * 
 * @author Jatin Thakur
 * @Team 2
 * @version 1.02	28-10-2020	Database Connectivity
 */
public class EffortCategorySpecific extends ListItem {

	/**********************************************************************************************

	Class Attributes

	 **********************************************************************************************/

	//---------------------------------------------------------------------------------------------
	// These attributes enable us to hide the details of the tab height and the height and width of
	// of the window decorations for the code that implements this user interface
	private int xOffset = 0;
	private int yOffset = 0;

	//---------------------------------------------------------------------------------------------
	// These attributes define the Effort Category Specific aspects of the GUI
	public enum EffortCategories {ARTIFACTS, PLANS, INTERRUPTIONS, DEFECTS, OTHER}

	//	private EffortCategories theEffortCategory = EffortCategories.ARTIFACTS;

	//---------------------------------------------------------------------------------------------
	// The following are the GUI objects that make up the Effort Category specific elements of the
	// user interface	
	private Label theTitle = new Label("Effort Category Kind:");
	private String effortCategoryList[] = { "Artifacts", "Plans", "Interruptions", "Defects", "Other" };
	private ComboBox <String> theChoice = new ComboBox <String>(FXCollections.observableArrayList(effortCategoryList));
	private Label theListTitle = new Label("Artifact options for this Effort Category:");
	protected TableView <ECItem> theDetailList = new TableView <ECItem>();
	private TableColumn <ECItem, String> nameColumn = new TableColumn <>("Option Name");
	private ComboBox <String> theOption = new ComboBox <String>();
	private Button btnInsert = new Button("Insert the option into the above list");
	private Button btnMoveUp = new Button("Move Up");
	private Button btnMoveDn = new Button("Move Dn");
	private Button btnDelete = new Button("Delete");
	private Label theOptionTitle = new Label("Select an option to be added to the list:");

	//---------------------------------------------------------------------------------------------
	// These attributes define the lists that are used to populate this tab's select lists.
	private ObservableList <String> currentList;

	private ObservableList <String> artifactOptions = FXCollections.observableArrayList();
	private ObservableList <String> artifactsSelected = FXCollections.observableArrayList();
	private ObservableList <String> artifactsAvailable = FXCollections.observableArrayList();

	private ObservableList <String> planOptions = FXCollections.observableArrayList();
	private ObservableList <String> plansSelected = FXCollections.observableArrayList();
	private ObservableList <String> plansAvailable = FXCollections.observableArrayList();

	private ObservableList <String> interruptionOptions = FXCollections.observableArrayList();
	private ObservableList <String> interruptionsSelected = FXCollections.observableArrayList();
	private ObservableList <String> interruptionsAvailable = FXCollections.observableArrayList();

	private ObservableList <String> defectOptions = FXCollections.observableArrayList();
	private ObservableList <String> defectsSelected = FXCollections.observableArrayList();
	private ObservableList <String> defectsAvailable = FXCollections.observableArrayList();

	private ListItem artTabList;
	private ListItem plnTabList;
	private ListItem intrTabList;
	private ListItem defTabList;

	// This flag is used to keep track of changes that are programmatically made as opposed to GUI
	private boolean updatingTheList = false;

	// This is the tab specific detail list
	protected ObservableList <EffortCategoryDetail> theEffortCategoryDetailList = FXCollections.observableArrayList();

	/**********
	 * This constructor establishes the base ListItem and then initializes the Effort Category
	 * specific attributes for the application.
	 * 
	 * @param g		The Group link is used to establish the list of GUI elements for this tab
	 * @param x		The x offset for the GUI elements to fit into the decorative borders
	 * @param y		The y offset
	 * @param t		The enumeration that helps select the right strings for labels, etc.
	 */
	public EffortCategorySpecific(Group g, int x, int y, TabNames t, ListItem art, ListItem pln, 
			ListItem intr, ListItem def) {
		super(g, x, y, t);

		xOffset = x;
		yOffset = y;

		artTabList = art;
		plnTabList = pln;
		intrTabList = intr;
		defTabList = def;

		// Establish empty selected lists
		artifactsSelected = FXCollections.observableArrayList();
		plansSelected = FXCollections.observableArrayList();
		interruptionsSelected = FXCollections.observableArrayList();
		defectsSelected = FXCollections.observableArrayList();
		theEffortCategoryDetailList = FXCollections.observableArrayList();

		constructTheLists(artTabList, plnTabList, intrTabList, defTabList);

		setupLabelUI(theTitle, "Arial", 14, 100, Pos.BASELINE_LEFT, 360 + xOffset, 260 + yOffset);
		setupComboBoxUI(theChoice, 75, 370 + xOffset, 280 + yOffset);
		theChoice.setValue("Artifacts");
		theChoice.setOnAction((event) -> { try {performTheChoice();} catch (Exception e) {e.printStackTrace();}});
		setupLabelUI(theListTitle, "Arial", 14, 100, Pos.BASELINE_LEFT, 690 + xOffset, 20 + yOffset);

		// Set up the TableView object for the list of the named list items
		setupTableViewUI(theDetailList, 250, 455, 695 + xOffset,  40 + yOffset, false);
		// Establish the columns in the Table View
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("item"));
		theDetailList.getColumns().add(nameColumn);
		nameColumn.setMaxWidth(245);
		nameColumn.setMinWidth(245);

		// Set up the label for the comboBox below the table view on the right side of the tab
		setupLabelUI(theOptionTitle, "Arial", 14, 100, Pos.BASELINE_LEFT, 690 + xOffset, 505 + yOffset);

		// This comboBox specifies an available option, but no action is performed. The user must 
		// press the insert button to cause an action
		setupComboBoxUI(theOption, 245, 700 + xOffset, 523 + yOffset);

		setupButtonUI(btnInsert, "Arial", 14, 245, Pos.BASELINE_CENTER, 690 + 10 + xOffset, 555 + yOffset);
		btnInsert.setOnAction((event) -> { insertOption(); });
		btnInsert.setTooltip(new Tooltip("Click here to move the above selected entry up one position"));


		// Set up the Move Up button. It moves the current selected TableView entry up one
		// position. Link the button to the action method and define the tool tip to help the user
		// understand the user interface.
		setupButtonUI(btnMoveUp, "Arial", 14, 70, Pos.BASELINE_CENTER, 690 + 10 + xOffset, 585 + yOffset);
		btnMoveUp.setOnAction((event) -> { moveOptionUp(); });
		btnMoveUp.setTooltip(new Tooltip("Click here to move the above selected entry up one position"));

		// Set up the Move Dn button. It moves the current selected TabkeView entry down one
		// position. Link the button to the action method and define the tool tip to help the user
		// understand the user interface.
		setupButtonUI(btnMoveDn, "Arial", 14, 70, Pos.BASELINE_CENTER, 690 + 100 + xOffset, 585 + yOffset);
		btnMoveDn.setOnAction((event) -> { moveOptionDn(); });
		btnMoveDn.setTooltip(new Tooltip("Click here to move the above selected entry down one position"));

		// Set up the Delete button. It removes the current selected TabkeView entry. Link the 
		// button to the action method and define the tool tip to help the user understand the
		// user interface.
		setupButtonUI(btnDelete, "Arial", 14, 70, Pos.BASELINE_CENTER, 690 + 185 + xOffset, 585 + yOffset);
		btnDelete.setOnAction((event) -> { deleteOption(); });
		btnDelete.setTooltip(new Tooltip("Click here to delete the above selected entry"));

		theNameField.setOnAction((event) -> { changeEvent(); });

		//insertOption2();
		hideTheContentChangedMessage();
		try {connectivity();} catch (Exception e) {e.printStackTrace();} 

		g.getChildren().addAll(theTitle, theChoice, theListTitle, theDetailList, theOptionTitle, theOption, 
				btnInsert, btnMoveUp, btnMoveDn, btnDelete);
	}

	/**********
	 * Private local method to initialize the standard fields for a JavaFX Label object
	 * 
	 * @param l		The Label object to be initialized
	 * @param ff	The font face for the label's text
	 * @param f		The font size for the label's text
	 * @param w		The minimum width for the Label
	 * @param p		The alignment for the text within the specified width
	 * @param x		The x-axis location for the Label
	 * @param y		The y-axis location for the Label
	 */
	private void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, double y){
		l.setFont(Font.font(ff, f));
		l.setMinWidth(w);
		l.setAlignment(p);
		l.setLayoutX(x);
		l.setLayoutY(y);
	}

	/**********
	 * Private local method to initialize the standard fields for a JavaFX ComboBox object
	 * 
	 * @param c		The ComboBox object
	 * @param w		The width of the GUI element
	 * @param x		The x-axis location for the ComboBox
	 * @param y		The y-axis location for the ComboBox
	 */
	private void setupComboBoxUI(ComboBox <String> c, double w, double x, double y){
		c.setMinWidth(w);
		c.setLayoutX(x);
		c.setLayoutY(y);
	}

	/**********
	 * Private local method to initialize the standard fields for a TableView
	 * 
	 * @param a		The TableView to be initialized
	 * @param w		The minimum width for the TextArea
	 * @param h		The maximum height for the TextArea
	 * @param x		The x-axis location for the TextArea
	 * @param y		The y-axis location for the TextArea
	 * @param e		A flag that specific if the TextArea is editable
	 */
	private void setupTableViewUI(TableView<ECItem> a, double w, double h, double x, double y, boolean e){
		a.setMinWidth(w);
		a.setMaxWidth(w);
		a.setMinHeight(h);
		a.setMaxHeight(h);
		a.setLayoutX(x);
		a.setLayoutY(y);		
		a.setEditable(e);
	}

	/**********
	 * Private local method to initialize the standard fields for a button
	 * 
	 * @param b		The Button to be initialized
	 * @param ff	The font face for the label's text
	 * @param f		The font size for the label's text
	 * @param w		The minimum width for the TextArea
	 * @param p		The alignment for the text within the specified width
	 * @param x		The x-axis location for the TextField
	 * @param y		The y-axis location for the TextField
	 */
	private void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y){
		b.setFont(Font.font(ff, f));
		b.setMinWidth(w);
		b.setAlignment(p);
		b.setLayoutX(x);
		b.setLayoutY(y);		
	}

	/*********************************************************************************************/
	/*                                                                                           */
	/* This set of methods extends the super class methods by doing the same function of the tab */
	/* specific attributes. In this case, the items are the lists of available and selected      */
	/* effort category names.                                                                    */
	/*                                                                                           */
	/*********************************************************************************************/

	/**********
	 * Handle the MoveTheEntryUp Button action by doing the super class function and then the Tab
	 * specific work
	 */
	protected void moveTheEntryUp() {
		super.moveTheEntryUp();
		if (tabSpecificIndex == -1) return;
	}

	/**********
	 * Handle the moveTheEntryDn Button action by doing the super class function and then the Tab
	 * specific work
	 */
	protected void moveTheEntryDn() {
		super.moveTheEntryDn();
		if (tabSpecificIndex == -1) return;
	}

	/**********
	 * Handle the deleteTheEntry Button action by doing the super class function and then the Tab
	 * specific work
	 */
	protected void deleteTheEntry() {
		super.deleteTheEntry();
		if (tabSpecificIndex == -1) return;
		theEffortCategoryDetailList.remove(tabSpecificIndex);
	}

	/**********
	 * Handle the editTheEntry Button action by doing the super class function and then the Tab
	 * specific work
	 */	
	protected void editTheEntry() {
		super.editTheEntry();
		int ndx = theChoice.getSelectionModel().getSelectedIndex();

		switch (ndx) {
		case 0:
			artifactsSelected.clear();
			plansSelected.clear();
			for(int i=0;i<theDetailList.getItems().size();i++) {
				theDetailList.getItems().clear();
			}

			try{
				Class.forName("com.mysql.cj.jdbc.Driver");
				Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pets?user=root&password=qwertyuiop1234[]");
				PreparedStatement ps = con.prepareStatement("select effortcategories_artifacts,effortcategories_plans,effortcategories_interruptions,effortcategories_defects from effortcategories where effortcategories_id="+tabSpecificIndex);
				ResultSet rs = ps.executeQuery();
				while(rs.next()){
					String artifacts1 = rs.getString(1);
					String plans1 = rs.getString(2);
					String interruptions1 = rs.getString(3);
					String defects1 = rs.getString(4);

					//For Artifacts
					String artifacts2=artifacts1.substring(1, artifacts1.length()-1);
					String[] artifacts3=artifacts2.split(",");
					for(int i=0;i<artifacts3.length;i++) {
						String artifactsValue=artifacts3[i].trim();
						if(artifactsValue.equals(""))
							System.out.println("");
						else {
							artifactsSelected.add(artifactsValue);
							ObservableList<ECItem> de = theDetailList.getItems();
							ECItem def = new ECItem(artifactsValue);
							de.add(def);
							theDetailList.setItems(de);
						}
					}

					//For Plans
					String plans2=plans1.substring(1, plans1.length()-1);
					String[] plans3=plans2.split(",");
					for(int i=0;i<plans3.length;i++) {
						String plansValue=plans3[i].trim();
						if(plansValue.equals(""))
							System.out.println("");
						else {
							plansSelected.add(plansValue);
						}
					}

					//For Interruptions
					String interruptions2=interruptions1.substring(1, interruptions1.length()-1);
					String[] interruptions3=interruptions2.split(",");
					for(int i=0;i<interruptions3.length;i++) {
						String interruptionsValue=interruptions3[i].trim();
						if(interruptionsValue.equals(""))
							System.out.println("");
						else {
							interruptionsSelected.add(interruptionsValue);
						}
					}

					//For Defects
					String defects2=defects1.substring(1, defects1.length()-1);
					String[] defects3=defects2.split(",");
					for(int i=0;i<defects3.length;i++) {
						String defectsValue=defects3[i].trim();
						if(defectsValue.equals(""))
							System.out.println("");
						else {
							defectsSelected.add(defectsValue);
						}
					}

				}
			}
			catch (Exception e) {
				System.out.println("OK");
			}

			//For Options Combo-box
			ArrayList<String> Optionslist1 = new ArrayList<String>(); 
			for(int i=0;i<theOption.getItems().size();i++) {
				Optionslist1.add(theOption.getItems().get(i));
			}
			ArrayList<String> Optionslist2 = new ArrayList<String>(); 
			for(int i=0;i<theDetailList.getItems().size();i++) {
				Optionslist2.add(theDetailList.getItems().get(i).toString());
			}

			Optionslist1.removeAll(Optionslist2); 
			theOption.getItems().clear();

			for(int i=0;i<Optionslist1.size();i++) {
				theOption.getItems().add(Optionslist1.get(i));
			}

		}
	}

	/**********
	 * Handle the saveTheEntry Button action by doing the super class function and then the Tab
	 * specific work
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */	
	protected void saveTheEntry() throws ClassNotFoundException, SQLException {
		ArrayList<Integer> listid=new ArrayList<Integer>();
		ArrayList<String> listname=new ArrayList<String>();
		ArrayList<String> listdesc=new ArrayList<String>();
		ArrayList<String> listarti=new ArrayList<String>();
		ArrayList<String> listplan=new ArrayList<String>();
		ArrayList<String> listinte=new ArrayList<String>();
		ArrayList<String> listdefec=new ArrayList<String>();
		
		Class.forName("com.mysql.cj.jdbc.Driver");
		Connection con2 = DriverManager.getConnection("jdbc:mysql://localhost:3306/pets?user=root&password=qwertyuiop1234[]");
		Statement stmt2=con2.createStatement();
		ResultSet rs2 = stmt2.executeQuery("select * from effortcategories");
		 while(rs2.next()){
	         //Retrieve by column name
	         int id  = rs2.getInt(1);
	         String name = rs2.getString(2);
	         String des = rs2.getString(3);
	         String artivalue = rs2.getString(4);
	         String plansvalue = rs2.getString(5);
	         String intvalue = rs2.getString(6);
	         String defvalue=rs2.getString(7);
	         listid.add(id);
	         listname.add(name);
	         listdesc.add(des);
	         listarti.add(artivalue);
	         listplan.add(plansvalue);
	         listinte.add(intvalue);
	         listdefec.add(defvalue);
	        
	      }
	      rs2.close();
		super.saveTheEntry();

		ArrayList<Integer> listid2=new ArrayList<Integer>();
		ArrayList<String> listname2=new ArrayList<String>();
		ArrayList<String> listdesc2=new ArrayList<String>();
		ArrayList<String> listarti2=new ArrayList<String>();
		ArrayList<String> listplan2=new ArrayList<String>();
		ArrayList<String> listinte2=new ArrayList<String>();
		ArrayList<String> listdefec2=new ArrayList<String>();
		
		
		Class.forName("com.mysql.cj.jdbc.Driver");
		Connection con4 = DriverManager.getConnection("jdbc:mysql://localhost:3306/pets?user=root&password=qwertyuiop1234[]");
		Statement stmt4=con4.createStatement();
		ResultSet rs4 = stmt4.executeQuery("select * from effortcategories");
		 while(rs4.next()){
	         //Retrieve by column name
	         int id  = rs4.getInt(1);
	         String name = rs4.getString(2);
	         String des = rs4.getString(3);

	         listid2.add(id);
	         listname2.add(name);
	         listdesc2.add(des);
	         listarti2.add("6&");
	         listplan2.add("6&");
	         listinte2.add("6&");
	         listdefec2.add("6&");
	         //Display values
//	         System.out.print("ID: " + id);
//	         System.out.print(", Age: " + age);
//	         System.out.print(", First: " + first);
//	         System.out.println(", Last: " + last);
	      }
	      rs4.close();
		
	      	      

		
			ArrayList<Integer> listid3=new ArrayList<Integer>();
			ArrayList<String> listname3=new ArrayList<String>();
			ArrayList<String> listdesc3=new ArrayList<String>();
			ArrayList<String> listarti3=new ArrayList<String>();
			ArrayList<String> listplan3=new ArrayList<String>();
			ArrayList<String> listinte3=new ArrayList<String>();
			ArrayList<String> listdefec3=new ArrayList<String>();
		
	        listid2.removeAll(listid);
	        listid3.addAll(listid);
	        for(int li:listid2) {
	        	listid3.add(li);
	        }
	        
	        listname2.removeAll(listname);
	        listname3.addAll(listname);
	        for(String li:listname2) {
	        	listname3.add(li);
	        }
	        
	        listdesc2.removeAll(listdesc);
	        listdesc3.addAll(listdesc);
	        for(String li:listdesc2) {
	        	listdesc3.add(li);
	        }
	        
	        listarti2.removeAll(listarti);
	        listarti3.addAll(listarti);
	        for(String li:listarti2) {
	        	listarti3.add(li);
	        }
	        
	        listplan2.removeAll(listplan);
	        listplan3.addAll(listplan);
	        for(String li:listplan2) {
	        	listplan3.add(li);
	        }
	        
	        listinte2.removeAll(listinte);
	        listinte3.addAll(listinte);
	        for(String li:listinte2) {
	        	listinte3.add(li);
	        }
	        
	        listdefec2.removeAll(listdefec);
	        listdefec3.addAll(listdefec);
	        for(String li:listdefec2) {
	        	listdefec3.add(li);
	        }
	        
	        System.out.println(listid3.toString());
	        System.out.println(listname3.toString());
	        System.out.println(listdesc3.toString());
	        System.out.println(listarti3.toString());
	        System.out.println(listplan3.toString());
	        System.out.println(listinte3.toString());
	        System.out.println(listdefec2.toString());
		
//
	        System.out.println("dasfasdfasdfasfasdfas "+listarti3);
		Class.forName("com.mysql.cj.jdbc.Driver");
		Connection con3 = DriverManager.getConnection("jdbc:mysql://localhost:3306/pets?user=root&password=qwertyuiop1234[]");
		
		Statement ps2=con3.createStatement();
		int a =ps2.executeUpdate("truncate table effortcategories");
		if (a==1)
			System.out.println("");
		else
			System.out.println("");
		for(int i=0;i<listid3.size();i++) {
			int id = listid3.get(i);
			String name=listname3.get(i);
			String desc=listdesc3.get(i);
			String artifacts=listarti3.get(i);
			String plans=listplan3.get(i);
			String interruptions=listinte3.get(i);
			String defects=listdefec3.get(i);
			PreparedStatement st = con3.prepareStatement("INSERT INTO effortcategories (effortcategories_id,effortcategories_name,effortcategories_description,effortcategories_artifacts,effortcategories_plans,effortcategories_interruptions,effortcategories_defects) VALUE (?,?,?,?,?,?,?)");
			st.setInt(1, id );
			st.setString(2, name );
			st.setString(3, desc );
			st.setString(4, artifacts );
			st.setString(5, plans);
			st.setString(6, interruptions);
			st.setString(7, defects);
			st.executeUpdate();
		}
		
		
		String artifacts=artifactsSelected.toString();
		String plans=plansSelected.toString();
		String interruptions=interruptionsSelected.toString();
		String defects=defectsSelected.toString();
//
		Class.forName("com.mysql.cj.jdbc.Driver");
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pets?user=root&password=qwertyuiop1234[]");
		Statement ps3=con.createStatement();

		int b =ps3.executeUpdate("update effortcategories set effortcategories_artifacts='"+artifacts+"',effortcategories_plans='"+plans+"',effortcategories_interruptions='"+interruptions+"',effortcategories_defects='"+defects+"' where effortcategories_id"+"="+tabSpecificIndex);
		if (b==1) {
			System.out.println("success");
		}
		else {
		}

	}

	/**********
	 * Handle the AddTheEntryToTheBottom Button action by doing the super class function and then the Tab
	 * specific work
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */	
	protected void AddTheEntryToTheBottom() throws ClassNotFoundException, SQLException {
		super.AddTheEntryToTheBottom();
		//if (tabSpecificIndex == -1) return;
		Class.forName("com.mysql.cj.jdbc.Driver");
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pets?user=root&password=qwertyuiop1234[]");
		Statement stmt=con.createStatement();
		ResultSet rs = stmt.executeQuery("select * from effortcategories");
		 while(rs.next()){
	         //Retrieve by column name
	         int id  = rs.getInt(1);
	         String age = rs.getString(2);
	         String first = rs.getString(3);
	         String last = rs.getString(4);

	         //Display values
//	         System.out.print("ID: " + id);
//	         System.out.print(", Age: " + age);
//	         System.out.print(", First: " + first);
//	         System.out.println(", Last: " + last);
	      }
	      rs.close();
	
	}

	/**********
	 * Handle the AddTheEntryAbove Button action by doing the super class function and then the Tab
	 * specific work
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */	
	protected void AddTheEntryAbove() throws ClassNotFoundException, SQLException {
		super.AddTheEntryAbove();
		//if (tabSpecificIndex == -1) return;
	}

	/**********
	 * Handle the AddTheEntryBelow Button action by doing the super class function and then the Tab
	 * specific work
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */	
	protected void AddTheEntryBelow() throws ClassNotFoundException, SQLException {
		super.AddTheEntryBelow();
		//if (tabSpecificIndex == -1) return;
	}

	/**********
	 * Handle the clearTheEntry Button action by doing the super class function and then the Tab
	 * specific work
	 */	
	protected void clearTheEntry() {
		super.clearTheEntry();
		if (tabSpecificIndex == -1) return;
	}

	/**********
	 * If one of the GUI elements on this Tab specific page has a change event, process the super
	 * class change event and then show the Content Changed Message.
	 */	
	protected void changeEvent() {
		super.changeEvent();
		showTheContentChangedMessage();
	}

	/**********
	 * If one of the GUI elements on this Tab specific page saves the current state, call the
	 * super class to hide the Content Changed Message.
	 */	
	protected void hideTheContentChangedMessage() {
		super.hideTheContentChangedMessage();
	}

	/**********
	 * If one of the GUI elements on this Tab specific page changes the current state without
	 * saving the state, call the super class to show the Content Changed Message.
	 */	
	protected void showTheContentChangedMessage() {
		super.showTheContentChangedMessage();
	}


	/**********
	 * This methods establishes the list of items from a specified Tab
	 * 
	 * @param lItem		The user interface tab whose list we need to access
	 * 
	 * @return			The observable list of String objects from the specified GUI tab
	 */
	private ObservableList <String> establishTabList(ObservableList <ListItemEntry> lItem) {

		ObservableList <String> result = FXCollections.observableArrayList();
		// Start with an empty list
		result.clear();

		// Add in all of the possible elements from the GUI tab
		for (int ndx = 0; ndx < lItem.size(); ndx++) 
			result.add(lItem.get(ndx).getListItemName());

		return result;
	}


	/**********
	 * This method is used to construct all four of the Lists during the initial construction
	 * of this object
	 * 
	 * @param art
	 * @param pln
	 * @param intr
	 * @param def
	 */
	private void constructTheLists(ListItem art, ListItem pln, ListItem intr, ListItem def) {

		// Establish the options lists
		artifactOptions = establishTabList(art.getTheList());
		planOptions = establishTabList(pln.getTheList());
		interruptionOptions = establishTabList(intr.getTheList());
		defectOptions = establishTabList(def.getTheList());

		// Populate the Available lists
		repopulateTheLists(art, pln, intr, def);
	};

	/**********
	 * This method is called by the UserInterface method to re-populate the select lists based on
	 * the assumption that the user may have added, removed, or changed items in the base lists.
	 * The list to be re-populated is the one that is currently visible via the combo box at the
	 * top of that part of the page.
	 * 
	 * @param art	The base list of artifacts name
	 * @param pln	The base list of plan names
	 * @param intr	The base list of interruption names
	 * @param def	The base list of defect names
	 */
	public void repopulate(ListItem art, ListItem pln, ListItem intr, ListItem def) {
		constructTheLists(art, pln, intr, def);
		int ndx = theChoice.getSelectionModel().getSelectedIndex();
		switch (ndx) {
		case 0:
			theOption.setItems(artifactsAvailable);
			theOption.getSelectionModel().clearAndSelect(0);
			break;
		case 1:
			theOption.setItems(plansAvailable);
			theOption.getSelectionModel().clearAndSelect(0);
			break;
		case 2:
			theOption.setItems(interruptionsAvailable);
			theOption.getSelectionModel().clearAndSelect(0);
			break;
		case 3:
			theOption.setItems(defectsAvailable);
			theOption.getSelectionModel().clearAndSelect(0);
			break;
		default:
			break;
		}
	}

	/**********
	 * This method is one of a pair that is used to ensure that the list of all possible options
	 * for a list agrees with the list of available to select items and the list of selected items.
	 * This method returns true only if the specified String is NOT in the specified list of
	 * Strings. Otherwise, it returns false;
	 * 
	 * @param strList	The specified list of Strings
	 * @param str		The specified String
	 * @return			True if str is NOT in the strList; else false
	 */
	private Boolean strIsNotInTheList(ObservableList <String> strList, String str) {

		// Check the second parameter against every ItemEntry in the list
		for (String s : strList)
			// If the second parameter is in the list, return false
			if (s.equals(str)) return false;

		// If the second parameter is *not* in the list, return true
		return true;
	}

	/**********
	 * This method is the second of a pair that is used to ensure that the list of all possible
	 * options for a list agrees with the list of available to select items and the list of 
	 * selected items. This method returns false only if the specified String is NOT in the specified list of
	 * Strings. Otherwise, it returns true;
	 * 
	 * @param strList	The specified list of Strings
	 * @param str		The specified String
	 * @return			False if str is NOT in the strList; else true
	 */
	private Boolean strIsInTheList(ObservableList <String> strList, String str) {

		// Check the second parameter against every ItemEntry in the list
		for (String sItem : strList)
			// If the second parameter is in the list, return false
			if (sItem.equals(str)) return true;

		// If the second parameter is *not* in the list, return true
		return false;
	}

	/**********
	 * This private method is called four times each time this tab is activated.  It rebuilds the
	 * Options list from the Tab's list and duplicates it into the Available list.  The method then
	 * checks each item in the Selected list to ensure it is still in the Options list.  If it
	 * isn't, it is removed. Finally the Available list is created by removing all of the Selected
	 * items from the stating state where it contains all of the Options.
	 * 
	 * @param tabList		The list from one of the tabs
	 * @param options		The list of all possible options for this tab
	 * @param selected		The list of all selected options from the tab
	 * @param available		The list of all of the possible options that have not yet been selected
	 */
	private void repopulateAList(ListItem tabList, ObservableList <String> options, 
			ObservableList <String> selected, ObservableList <String> available) {

		// Clear the generated lists
		options.clear();
		available.clear();

		// 1. Build the Option list and the Available list from the  Tab's list
		for (int ndx = 0; ndx < tabList.getTheList().size(); ndx++) {
			options.add(tabList.getTheList().get(ndx).getListItemName());
			available.add(tabList.getTheList().get(ndx).getListItemName());
		}

		// 2. Examine each Selected list item to ensure it is in STILL in the Option list
		//		If not, remove it from the Selected list.
		for (int ndx = 0; ndx < selected.size(); ndx++)
			if (strIsNotInTheList(options, selected.get(ndx)))
				selected.remove(ndx--);							// This auto-decrement after the
		// remove is really important!

		// 3. Construct the Available List by removing all Selected items from the Option's list
		for (int ndx = 0; ndx < available.size(); ndx++)
			if (strIsInTheList(selected, available.get(ndx))) 
				available.remove(ndx--);						// This auto-decrement after the
		// remove is really important!
	}

	//For Connectivity with Database

	public void connectivity() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.cj.jdbc.Driver");
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pets?user=root&password=qwertyuiop1234[]");
		PreparedStatement ps = con.prepareStatement("select * from effortcategories");
		ResultSet rs = ps.executeQuery();
		while(rs.next()){
			@SuppressWarnings("unused")
			int cid = rs.getInt(1);
			@SuppressWarnings("unused")
			String name = rs.getString(2);
			@SuppressWarnings("unused")
			String desc = rs.getString(3);
			String artifacts = rs.getString(4);
			@SuppressWarnings("unused")
			String plans = rs.getString(5);
			@SuppressWarnings("unused")
			String interruptions = rs.getString(6);
			@SuppressWarnings("unused")
			String defects = rs.getString(7);
			if(artifacts!=null) {
				String artifacts2=artifacts.substring(1, artifacts.length()-1);
				String[] artifacts3=artifacts2.split(",");
				for(int i=0;i<artifacts3.length;i++) {
					String value3=artifacts3[i].trim();
					artifactsAvailable.add(value3);
				}
			}

		}
	}

	/**********
	 * When re-entering the Effort Category tab, the user could have changed one or more of the
	 * Effort Options, so we must rebuild the Options list from the Tabs.  We then must correct
	 * the selected list by removing any items that are not in the Options list.  We then must 
	 * construct the Available list by starting with the Options list and removing any already 
	 * selected entries. Since this pattern is identical for all four lists, we have a private
	 * method, repopulateAList, do it for us for each of the four effort categories.
	 * 
	 * @param art	Artifacts
	 * @param pln	Plans
	 * @param intr	Interruptions
	 * @param def	Defects
	 */
	private void repopulateTheLists(ListItem art, ListItem pln, ListItem intr, ListItem def) {
		// Artifacts
		repopulateAList(art, artifactOptions, artifactsSelected, artifactsAvailable);

		// Plans
		repopulateAList(pln, planOptions, plansSelected, plansAvailable);

		// Interruptions
		repopulateAList(intr, interruptionOptions, interruptionsSelected, interruptionsAvailable);

		// Defects
		repopulateAList(def, defectOptions, defectsSelected, defectsAvailable);	
	}

	/**********
	 * This methods is used to establish the Table View list for one of the effort categories given
	 * a list of names.
	 * 
	 * @param src	The list of names to be used to create the table view list
	 * @return		The table view list
	 */
	private ObservableList<ECItem> establishDetailList(ObservableList <String> src) {
		// Fetch the table view list from the right most GUI Table View
		ObservableList<ECItem> result = theDetailList.getItems();

		// Clear the list
		result.clear();

		// If there is no list of strings, we are done
		if (src == null) return result;

		// Create a list item for each String in the source list
		for (int i = 0; i < src.size(); i++) {
			// Create a new item entry based on those values
			ECItem def = new ECItem(src.get(i));
			// Add the new item entry at the end of the list
			result.add(def);
		}

		// Return the result
		return result;
	}

	/**********
	 * This method changes the display based on which choice was made from the upper right
	 * comboBox
	 * 
	 * @param avail		The list of available options
	 * @param select	The list of selected options
	 */
	private void performThisChoice(ObservableList <String> avail, ObservableList <String> select) {

		// If there was a previous selected list and it is this list, no need to do this
		if (currentList != null && currentList == avail) return;

		// Keep track of this list
		currentList = avail;

		// Set up the the lower right comboBox with the items available to add to the Table View
		theOption.setItems(avail);
		theOption.getSelectionModel().clearAndSelect(0);

		// Set up the right most Table View with the items that have already been selected
		theDetailList.setItems(establishDetailList(select));
		int lastNdx = select.size() - 1;							// Select the just added item
		theDetailList.getSelectionModel().clearAndSelect(lastNdx);	
		theDetailList.scrollTo(lastNdx);							// Make sure it is visible
	}

	/**********
	 * The private handler for a ComboBox change event - When the user chooses a different category
	 * we have to change the Table View and the lower comboBox to agree with the new selection.
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	private void performTheChoice() throws ClassNotFoundException, SQLException {
		// Make sure that this is a user generated event as opposed to a programmatic event
		if (updatingTheList) return;
		updatingTheList = true;

		// Based on the selection the user made, show or hide the Effort Category Specific GUI elements
		if (theChoice.getSelectionModel().getSelectedIndex() >= 0 && 
				theChoice.getSelectionModel().getSelectedIndex() <= 3) {
			theListTitle.setVisible(true);
			theDetailList.setVisible(true);
			theOptionTitle.setVisible(true);
			theOption.setVisible(true);
			btnInsert.setVisible(true);
			btnMoveUp.setVisible(true);
			btnMoveDn.setVisible(true);
			btnDelete.setVisible(true);
		} else {
			theListTitle.setVisible(false);
			theDetailList.setVisible(false);
			theOptionTitle.setVisible(false);
			theOption.setVisible(false);
			btnInsert.setVisible(false);
			btnMoveUp.setVisible(false);
			btnMoveDn.setVisible(false);
			btnDelete.setVisible(false);
		}

		// Based on the selection the user made, set up the Table View and the comboBox
		switch (theChoice.getSelectionModel().getSelectedIndex()) {
		case 0:
			performThisChoice(artifactsAvailable, artifactsSelected);
			break;
		case 1:
			performThisChoice(plansAvailable, plansSelected);
			break;
		case 2:
			performThisChoice(interruptionsAvailable, interruptionsSelected);
			break;
		case 3:
			performThisChoice(defectsAvailable, defectsSelected);
			break;
		default:
			if (currentList == null) break;
			currentList =  null;
			theOption.setItems(null);
			break;
		}

		// Set the label accordingly
		theListTitle.setText(getSubordinateListTitle());

		// We are done changing the list
		updatingTheList = false;
	}

	/**********
	 * Shift the specified String from the Available List to the Selected List and then set up the 
	 * Table View and the ComboBox to reflect this action
	 * 
	 * @param str	The string to be moved from the Available to the Selected List
	 * @param src	The Available List
	 * @param dst	The Selected List
	 */
	private void shift(String str, ObservableList <String> src, ObservableList <String> dst) {
		// Scan through the Available List, find the String, and remove it from the list
		for (int ndx = 0; ndx < src.size(); ndx++)
			if (src.get(ndx).equals(str)) {
				src.remove(ndx);
				break;
			}

		// Add the String to the end of the Selected List
		dst.add(str);

		// Fetch the list of items from the right most Table View GUI items and change it to
		// agree with the new Selected List
		ObservableList<ECItem> de = theDetailList.getItems();	// Fetch the list
		de.clear();												// Clear it
		int lastNdx = 0;										// Keep track of the insert index
		for (int i = 0; i < dst.size(); i++) {
			if (dst.get(i).equals(str))
				lastNdx = i;
			// Create a new item entry based on those values
			ECItem def = new ECItem(dst.get(i));
			// Add the new item entry at the end of the list
			de.add(def);
		}

		// Insert the new list into the Table View, Scroll to the point of insertion, and select it
		theDetailList.setItems(de);
		theDetailList.scrollTo(lastNdx);
		theDetailList.getSelectionModel().clearAndSelect(lastNdx);

		// Select the first item in the Available list
		theOption.getSelectionModel().clearAndSelect(0);
	}

	/**********
	 * The method is used to shift an items back from the Selected List to the Available List when
	 * a selected items is deleted.
	 * 
	 * @param str	The name to be shifted back from the Selected List to the Available List
	 * @param mstr	The Master list, so the new item is inserted in the right place
	 * @param avl	The Available List
	 * @param sel	The Selected List
	 */
	private void shiftBack(String str, ObservableList <String> mstr, ObservableList <String> avl, ObservableList <String> sel) {
		// We will scan through the Master List which gives the order of items that the user specified
		// The Master List should be the union of the Available List and the Selected List
		int theSize = mstr.size();	// Set up the size of the master list
		int availableIndex = 0;		// Establish the index for the Available List
		int selectIndex = 0;		// Establish the index for the Selected List

		// The Master list elements are either in the Available List or the Selected List.  The
		// order of the items in the Available and Selected List match the order in the Master List.
		// We go through the Master list starting at the beginning and examine each item for the
		// that list.  We know it will be in one or the other
		for (int masterIndex = 0; masterIndex < theSize; masterIndex++)
			// If the item from the master list matched the specified String, we know it must be in
			// in the Selected List and so we can remove it.
			if (str.equals(mstr.get(masterIndex))) {
				// Remove it from the selected list at this point
				sel.remove(selectIndex);

				// The item has been found. Add it to the available list at this point.
				avl.add(availableIndex, str);

				// Once the shift has been performed, there is no need to continue the loop
				break;
			}
			else {
				// If the Specified String does not match the element from the Master list, it
				// must be in either the Selected List or the Available list.
				// 
				// So, we see if it is in the Available list.
				if (availableIndex < avl.size() && mstr.get(masterIndex).contentEquals(avl.get(availableIndex)))
					// If it is in the Available List, we increment that index
					availableIndex++;
				else
					// Otherwise if must be in the Selected List, so we increment that index
					selectIndex++;
			}

		// With the shift back performed, we reset the comboBox and select what we just put back
		theOption.setItems(avl);
		theOption.getSelectionModel().clearAndSelect(availableIndex);
	}

	/*********
	 * This method is called with the user presses the insert button to add a ComboBox item to the 
	 * list of effort items on the right most TableView
	 */
	private void insertOption() {
		// If we are already updating the lists, do not process this request
		if (updatingTheList) return;
		// This avoids needless repetitive calls due to programmatic requests
		updatingTheList = true;

		// Check to be sure there is an item in the ComboBox to insert
		if (theOption.getItems().size()<1) {
			Alert alert = new Alert(AlertType.WARNING, "Insert is only valid when there's an item to insert!", ButtonType.OK);
			alert.showAndWait();
			updatingTheList = false;
			return;
		}

		// Signal that a change has been made and do the change
		showTheContentChangedMessage();
		int ndx = theChoice.getSelectionModel().getSelectedIndex();
		String str = theOption.getValue();

		// Perform the change on the appropriate set of lists
		switch (ndx) {
		case 0:
			shift(str, artifactsAvailable, artifactsSelected);
			break;
		case 1:
			shift(str, plansAvailable, plansSelected);
			break;
		case 2:
			shift(str, interruptionsAvailable, interruptionsSelected);
			break;
		case 3:
			shift(str, defectsAvailable, defectsSelected);
			break;
		default:
			break;
		}

		// We are done updating the lists
		updatingTheList = false;	
	}

	/**********
	 * This method moves the currently selected item in the rightmost TableView list up.
	 */
	private void moveOptionUp() {
		// Fetch the index of the selected row in the TableView
		int theIndex = theDetailList.getSelectionModel().getSelectedIndex();

		// If no row is selected, give a warning and ignore the request
		if (theIndex == -1) {
			Alert alert = new Alert(AlertType.WARNING, "Move Up is only valid when a row has been selected!", ButtonType.OK);
			alert.showAndWait();
			return;
		}

		// If the selected row is the top most row, it is not possible to move it up, so give an
		// appropriate warning and ignore the request
		if (theIndex == 0) {
			Alert alert = new Alert(AlertType.WARNING, "Move Up is not valid for the first row!", ButtonType.OK);
			alert.showAndWait();
			return;
		}

		// Updating the list is indeed happening
		updatingTheList = true;

		// Make the Content Changed Message visible
		showTheContentChangedMessage();

		// Given a valid and proper index, produce an observable list from the TableView and swap
		// the selected item with the one above it.
		ObservableList<ECItem> de = theDetailList.getItems();
		ECItem tempDE = de.get(theIndex-1);
		de.set(theIndex-1, de.get(theIndex));
		de.set(theIndex, tempDE);

		// Place the updated list back into the table view
		theDetailList.setItems(de);

		// Select the entry that was moved up and make sure it is visible
		theDetailList.scrollTo(theIndex-1);
		theDetailList.getSelectionModel().select(theIndex-1);

		// Done updating the list
		updatingTheList = false;
	}

	/**********
	 * This method moves the currently selected item in the rightmost TableView list down.
	 */
	private void moveOptionDn() {
		// Fetch the index of the selected row in the TableView
		int theIndex = theDetailList.getSelectionModel().getSelectedIndex();

		// If no row is selected, give a warning and ignore the request
		if (theIndex == -1) {
			Alert alert = new Alert(AlertType.WARNING, "Move Dn is only valid when a row has been selected!", ButtonType.OK);
			alert.showAndWait();
			return;
		}

		// If the selected row is the bottom row, it is not possible to move it down, so give an
		// appropriate warning and ignore the request
		ObservableList<ECItem> de = theDetailList.getItems();
		if (theIndex == de.size()-1) {
			Alert alert = new Alert(AlertType.WARNING, "Move Up is not valid for the last row!", ButtonType.OK);
			alert.showAndWait();
			return;
		}

		// Updating the list
		updatingTheList = true;

		// Make the Content Changed Message visible
		showTheContentChangedMessage();		

		// Given a valid and proper index, produce an observable list from the TableView and swap
		// the selected item with the one above it.
		ECItem tempDE = de.get(theIndex+1);
		de.set(theIndex+1, de.get(theIndex));
		de.set(theIndex, tempDE);

		// Place the updated list back into the table view
		theDetailList.setItems(de);

		// Select the entry that was moved down and make sure it is visible
		theDetailList.scrollTo(theIndex+1);
		theDetailList.getSelectionModel().select(theIndex+1);

		// Done updating the list
		updatingTheList = false;
	}

	/**********
	 * This method deletes the currently selected item in the rightmost TableView list.
	 */
	private void deleteOption() {
		// Fetch the index of the selected row in the TableView
		int theIndex = theDetailList.getSelectionModel().getSelectedIndex();

		// If no row is selected, give a warning and ignore the request
		if (theIndex == -1) {
			Alert alert = new Alert(AlertType.WARNING, "Delete is only valid when a row has been selected!", ButtonType.OK);
			alert.showAndWait();
			return;
		}

		// Updating the list
		updatingTheList = true;

		showTheContentChangedMessage();

		// If beyond the end of the list just return (This should never happen,
		// so we have not spent effort raising a warning.)
		ObservableList<ECItem> de = theDetailList.getItems();
		if (theIndex > de.size()-1) return;

		// Save the name that is being removed
		String removedName = de.get(theIndex).getItem();

		// Remove the item from the TableView list, shift any below up.
		de.remove(theIndex);

		// Place the updated list back into the TableView
		theDetailList.setItems(de);

		// De-select the TableView so no entry is selected
		theDetailList.getSelectionModel().select(-1);

		// Add the deleted item back into the available list and update that comboBox
		// Get the index from the choice comboBox so we know which set of lists to update
		int ndx = theChoice.getSelectionModel().getSelectedIndex();
		switch (ndx) {
		case 0:
			shiftBack(removedName, artifactOptions, artifactsAvailable, artifactsSelected);
			break;
		case 1:
			shiftBack(removedName, planOptions, plansAvailable, plansSelected);
			break;
		case 2:
			shiftBack(removedName, interruptionOptions, interruptionsAvailable, interruptionsSelected);
			break;
		case 3:
			shiftBack(removedName, defectOptions, defectsAvailable, defectsSelected);
			break;
		default:
			break;
		}

		// Done updating the list
		updatingTheList = false;
	}

	/**********
	 * Display the proper title for the rightmost TableView element
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	private String getSubordinateListTitle() throws ClassNotFoundException, SQLException {
		int ndx = theChoice.getSelectionModel().getSelectedIndex();

		switch (ndx) {
		case 0:
			artifactsSelected.clear();

			for(int i=0;i<theDetailList.getItems().size();i++) {
				theDetailList.getItems().clear();
			}
			try{
				Class.forName("com.mysql.cj.jdbc.Driver");
				Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pets?user=root&password=qwertyuiop1234[]");
				PreparedStatement ps = con.prepareStatement("select effortcategories_artifacts from effortcategories where effortcategories_id="+tabSpecificIndex);
				ResultSet rs = ps.executeQuery();
				while(rs.next()){
					String artifacts1 = rs.getString(1);
					if(artifacts1!=null) {
						String artifacts2=artifacts1.substring(1, artifacts1.length()-1);
						String[] artifacts3=artifacts2.split(",");
						for(int i=0;i<artifacts3.length;i++) {
							String value3=artifacts3[i].trim();
							if(value3.equals(""))
								System.out.println("");
							else {
								artifactsSelected.add(value3);
								ObservableList<ECItem> de = theDetailList.getItems();
								ECItem def = new ECItem(value3);
								de.add(def);
								theDetailList.setItems(de);
							}
						}
					}
				}
			}
			catch (Exception e) {
				//System.out.println("1171 OK");
			}
			ArrayList<String> artifactsOptionslist1 = new ArrayList<String>(); 
			for(int i=0;i<theOption.getItems().size();i++) {
				artifactsOptionslist1.add(theOption.getItems().get(i));
			}
			ArrayList<String> artifactsOptionslist2 = new ArrayList<String>(); 
			for(int i=0;i<theDetailList.getItems().size();i++) {
				artifactsOptionslist2.add(theDetailList.getItems().get(i).toString());
			}

			artifactsOptionslist1.removeAll(artifactsOptionslist2); 
			theOption.getItems().clear();

			for(int i=0;i<artifactsOptionslist1.size();i++) {
				theOption.getItems().add(artifactsOptionslist1.get(i));
			}

			return "Artifact options for this Effort Category:";

		case 1:
			plansSelected.clear();
			for(int i=0;i<theDetailList.getItems().size();i++) {
				theDetailList.getItems().clear();
			}
			try{
				Class.forName("com.mysql.cj.jdbc.Driver");
				Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pets?user=root&password=qwertyuiop1234[]");
				PreparedStatement ps = con.prepareStatement("select effortcategories_plans from effortcategories where effortcategories_id="+tabSpecificIndex);
				ResultSet rs = ps.executeQuery();
				while(rs.next()){
					String plans1 = rs.getString(1);
					if(plans1!=null) {
						String plans2=plans1.substring(1, plans1.length()-1);
						String[] plans3=plans2.split(",");
						for(int i=0;i<plans3.length;i++) {
							String plansValue=plans3[i].trim();
							if(plansValue.equals(""))
								System.out.println("");
							else {
								plansSelected.add(plansValue);
								ObservableList<ECItem> de = theDetailList.getItems();
								ECItem def = new ECItem(plansValue);
								de.add(def);
								theDetailList.setItems(de);
							}
						}
					}
				}
			}
			catch (Exception e) {
				System.out.println("OK");
			}
			ArrayList<String> plansOptionslist1 = new ArrayList<String>(); 
			for(int i=0;i<theOption.getItems().size();i++) {
				plansOptionslist1.add(theOption.getItems().get(i));
			}
			ArrayList<String> plansOptionslist2 = new ArrayList<String>(); 
			for(int i=0;i<theDetailList.getItems().size();i++) {
				plansOptionslist2.add(theDetailList.getItems().get(i).toString());
			}
			plansOptionslist1.removeAll(plansOptionslist2); 
			theOption.getItems().clear();
			for(int i=0;i<plansOptionslist1.size();i++) {
				theOption.getItems().add(plansOptionslist1.get(i));
			}

			return "Plan options for this Effort Category:";
			
		case 2:
			interruptionsSelected.clear();
			for(int i=0;i<theDetailList.getItems().size();i++) {
				theDetailList.getItems().clear();
			}
			try{
				Class.forName("com.mysql.cj.jdbc.Driver");
				Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pets?user=root&password=qwertyuiop1234[]");
				PreparedStatement ps = con.prepareStatement("select effortcategories_interruptions from effortcategories where effortcategories_id="+tabSpecificIndex);
				ResultSet rs = ps.executeQuery();
				while(rs.next()){
					String interruptions1 = rs.getString(1);
					if(interruptions1!=null) {
						String interruptions2=interruptions1.substring(1, interruptions1.length()-1);
						String[] interruptions3=interruptions2.split(",");
						for(int i=0;i<interruptions3.length;i++) {
							String interruptionsValue=interruptions3[i].trim();
							if(interruptionsValue.equals(""))
								System.out.println("");
							else {
								interruptionsSelected.add(interruptionsValue);
								ObservableList<ECItem> de = theDetailList.getItems();
								ECItem def = new ECItem(interruptionsValue);
								de.add(def);
								theDetailList.setItems(de);
							}
						}
					}
				}
			}
			catch (Exception e) {
				System.out.println("OK");
			}
			ArrayList<String> interruptionsOptionslist1 = new ArrayList<String>(); 
			for(int i=0;i<theOption.getItems().size();i++) {
				interruptionsOptionslist1.add(theOption.getItems().get(i));
			}
			ArrayList<String> interruptionsOptionslist2 = new ArrayList<String>(); 
			for(int i=0;i<theDetailList.getItems().size();i++) {
				interruptionsOptionslist2.add(theDetailList.getItems().get(i).toString());
			}
			interruptionsOptionslist1.removeAll(interruptionsOptionslist2); 
			theOption.getItems().clear();
			for(int i=0;i<interruptionsOptionslist1.size();i++) {
				theOption.getItems().add(interruptionsOptionslist1.get(i));
			}

			return "Interruption options for this Effort Category:";
			
		case 3:
			defectsSelected.clear();
			for(int i=0;i<theDetailList.getItems().size();i++) {
				theDetailList.getItems().clear();
			}
			try{
				Class.forName("com.mysql.cj.jdbc.Driver");
				Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pets?user=root&password=qwertyuiop1234[]");
				PreparedStatement ps = con.prepareStatement("select effortcategories_defects from effortcategories where effortcategories_id="+tabSpecificIndex);
				ResultSet rs = ps.executeQuery();
				while(rs.next()){
					String defects1 = rs.getString(1);
					if(defects1!=null) {
						String defects2=defects1.substring(1, defects1.length()-1);
						String[] defects3=defects2.split(",");
						for(int i=0;i<defects3.length;i++) {
							String defectsValue=defects3[i].trim();
							if(defectsValue.equals(""))
								System.out.println("");
							else {
								defectsSelected.add(defectsValue);
								ObservableList<ECItem> de = theDetailList.getItems();
								ECItem def = new ECItem(defectsValue);
								de.add(def);
								theDetailList.setItems(de);
							}
						}
					}
				}
			}
			catch (Exception e) {
				System.out.println("OK");
			}
			ArrayList<String> defectsOptionslist1 = new ArrayList<String>(); 
			for(int i=0;i<theOption.getItems().size();i++) {
				defectsOptionslist1.add(theOption.getItems().get(i));
			}
			ArrayList<String> defectsOptionslist2 = new ArrayList<String>(); 
			for(int i=0;i<theDetailList.getItems().size();i++) {
				defectsOptionslist2.add(theDetailList.getItems().get(i).toString());
			}
			defectsOptionslist1.removeAll(defectsOptionslist2); 
			theOption.getItems().clear();
			for(int i=0;i<defectsOptionslist1.size();i++) {
				theOption.getItems().add(defectsOptionslist1.get(i));
			}
			
			return "Defect options for this Effort Category:";
			
		default:
			return "Effort Category: Other";
		}
	}

	/**********
	 * This nested protected Class is used to populate the rightmost TableView GUI.  It is just a 
	 * list of String objects.  The methods for this class are totally obvious, so I'll not 
	 * document them.
	 * 
	 * @author Lynn Robert Carter
	 *
	 */
	protected class ECItem {
		private String item = "";

		public ECItem(String s) {
			item = s;
		}

		public String toString() {
			return item;
		}

		public String getItem() {
			return item;
		}

		public void setItem(String s) {
			item = s;
		}
	}

	/**********
	 * This nested protected Class is used to define a specific Effort Category with a set of four
	 * lists.  Each of the four is just a list of String objects, but each time a specific Effort
	 * Category is selected, all four of the Selected lists and all of the dependent lists must be
	 * set up.  The methods for this class are totally obvious, so I'll not document them.
	 * 
	 * @author Lynn Robert Carter
	 *
	 */
	protected class EffortCategoryDetail {
		private ObservableList<String> selectedArtifactsList = FXCollections.observableArrayList();
		private ObservableList<String> selectedPlansList = FXCollections.observableArrayList();
		private ObservableList<String> selectedInterruptionsList = FXCollections.observableArrayList();
		private ObservableList<String> selectedDefectsList = FXCollections.observableArrayList();

		public EffortCategoryDetail(ObservableList<String> sal, ObservableList<String> spl, 
				ObservableList<String> sil, ObservableList<String> sdl) {
			// Make shallow copies of each of the four lists 
			selectedArtifactsList = FXCollections.observableArrayList(sal);
			selectedPlansList = FXCollections.observableArrayList(spl);
			selectedInterruptionsList = FXCollections.observableArrayList(sil);
			selectedDefectsList = FXCollections.observableArrayList(sdl);
		}

		public String toString() {
			return "\n     " + selectedArtifactsList + "\n     " + selectedPlansList + "\n     " + 
					selectedInterruptionsList + "\n     " + selectedDefectsList;
		}

		public ObservableList<String> getSelectedArtifactList() {
			return selectedArtifactsList;
		}

		public void setSelectedArtifactList(ObservableList<String> sal) {
			selectedArtifactsList = sal;
		}

		public ObservableList<String> getSelectedPlansList() {
			return selectedPlansList;
		}

		public void setSelectedPlansList(ObservableList<String> spl) {
			selectedPlansList = spl;
		}

		public ObservableList<String> getSelectedInterruptionsList() {
			return selectedInterruptionsList;
		}

		public void setSelectedInterruptionsList(ObservableList<String> sil) {
			selectedInterruptionsList = sil;
		}

		public ObservableList<String> getSelectedDefectsList() {
			return selectedDefectsList;
		}

		public void setSelectedDefectsList(ObservableList<String> sdl) {
			selectedDefectsList = sdl;
		}
	}
}
