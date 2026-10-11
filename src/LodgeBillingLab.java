import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lodge Billing Lab (ICT261 Lecture 3) - single-file JavaFX application.
 * No FXML, no database, no external libraries (only JavaFX itself).
 * Saved bills live in memory and disappear when the app closes.
 */
public class LodgeBillingLab extends Application {

    // ---------- Business rules ----------
    private static final Map<String, BigDecimal> RATES = new LinkedHashMap<>();
    static {
        RATES.put("Standard", new BigDecimal("350.00"));
        RATES.put("Deluxe", new BigDecimal("550.00"));
        RATES.put("Family", new BigDecimal("750.00"));
    }
    private static final int DISCOUNT_MIN_NIGHTS = 5;
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.05");

    // ---------- Data model ----------
    private static final class Bill {
        final String id, guest, room, notes;
        final int nights;
        final BigDecimal roomCharge, discount, extras, total, paid, balance;

        Bill(String id, String guest, String room, int nights, BigDecimal extras,
             BigDecimal paid, String notes) {
            this.id = id;
            this.guest = guest;
            this.room = room;
            this.nights = nights;
            this.extras = extras;
            this.paid = paid;
            this.notes = notes;
            this.roomCharge = RATES.get(room).multiply(BigDecimal.valueOf(nights));
            this.discount = nights >= DISCOUNT_MIN_NIGHTS
                    ? roomCharge.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2);
            this.total = roomCharge.subtract(discount).add(extras).setScale(2, RoundingMode.HALF_UP);
            this.balance = total.subtract(paid).setScale(2, RoundingMode.HALF_UP);
        }
    }

    private static String money(BigDecimal v) {
        return "K " + v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    // ---------- UI state ----------
    private final ObservableList<Bill> bills = FXCollections.observableArrayList();
    private int nextBillNumber = 1;

    private TextField guestField, nightsField, extrasField, paidField;
    private ComboBox<String> roomBox;
    private TextArea notesArea;
    private Label statusLabel;
    private TableView<Bill> table;

    @Override
    public void start(Stage stage) {
        // Menu bar
        MenuItem newItem = new MenuItem("New guest");
        newItem.setOnAction(e -> clearForm());
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> stage.close());
        Menu fileMenu = new Menu("File", null, newItem, new SeparatorMenuItem(), exitItem);

        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showInfo("About", "Lodge Billing Lab\nICT261 Lecture 3\nMulungushi University"));
        Menu helpMenu = new Menu("Help", null, aboutItem);
        MenuBar menuBar = new MenuBar(fileMenu, helpMenu);

        // Tool bar
        Button newBtn = new Button("New guest");
        newBtn.setOnAction(e -> clearForm());
        Button receiptBtn = new Button("View receipt");
        receiptBtn.setOnAction(e -> viewReceipt());
        Button deleteBtn = new Button("Delete selected bill");
        deleteBtn.setOnAction(e -> deleteSelected());
        ToolBar toolBar = new ToolBar(newBtn, receiptBtn, deleteBtn);

        // Titles
        Label title = new Label("Lodge Billing Lab");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        Label subtitle = new Label("ICT261 Lecture 3 Mr E Nyirenda Mulungushi University");

        // Form
        guestField = new TextField();
        roomBox = new ComboBox<>(FXCollections.observableArrayList(RATES.keySet()));
        roomBox.setMaxWidth(Double.MAX_VALUE);
        nightsField = new TextField();
        extrasField = new TextField();
        paidField = new TextField();
        notesArea = new TextArea();
        notesArea.setPromptText("Optional guest remarks");
        notesArea.setPrefRowCount(3);

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(10);
        ColumnConstraints labelCol = new ColumnConstraints(110);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(labelCol, fieldCol);
        form.addRow(0, new Label("Guest name"), guestField);
        form.addRow(1, new Label("Room type"), roomBox);
        form.addRow(2, new Label("Nights"), nightsField);
        form.addRow(3, new Label("Extras in K"), extrasField);
        form.addRow(4, new Label("Amount paid in K"), paidField);
        form.addRow(5, new Label("Notes"), notesArea);
        guestField.setMaxWidth(Double.MAX_VALUE);
        nightsField.setMaxWidth(Double.MAX_VALUE);
        extrasField.setMaxWidth(Double.MAX_VALUE);
        paidField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(form, Priority.ALWAYS);

        // Right-hand price panel
        Label pricesTitle = new Label("Practice room prices");
        pricesTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        ListView<String> priceList = new ListView<>();
        for (Map.Entry<String, BigDecimal> en : RATES.entrySet()) {
            priceList.getItems().add(en.getKey() + ": " + money(en.getValue()) + " per night");
        }
        priceList.setPrefHeight(110);
        priceList.setMouseTransparent(true);
        Label rule1 = new Label("5 nights or more: 5% off the room charge.");
        Label rule2 = new Label("Extras get no discount. No tax in this lab.");
        Label rule3 = new Label("Saved bills last only until you close the app.");
        for (Label l : new Label[]{rule1, rule2, rule3}) {
            l.setWrapText(true);
        }
        VBox side = new VBox(8, pricesTitle, priceList, rule1, rule2, rule3);
        side.setPrefWidth(300);

        HBox formRow = new HBox(30, form, side);

        // Action buttons + status
        Button calcBtn = new Button("Calculate");
        calcBtn.setOnAction(e -> calculate());
        Button saveBtn = new Button("Save bill");
        saveBtn.setDefaultButton(true);
        saveBtn.setOnAction(e -> saveBill());
        HBox buttonRow = new HBox(10, calcBtn, saveBtn);
        buttonRow.setPadding(new Insets(0, 0, 0, 125));

        statusLabel = new Label("");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        Label savedTitle = new Label("Saved bills");
        savedTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Table
        table = new TableView<>(bills);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No bills saved yet"));
        addColumn("Bill", b -> b.id);
        addColumn("Guest", b -> b.guest);
        addColumn("Room", b -> b.room);
        addColumn("Nights", b -> String.valueOf(b.nights));
        addColumn("Total", b -> money(b.total));
        addColumn("Paid", b -> money(b.paid));
        addColumn("Balance", b -> money(b.balance));
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox content = new VBox(10, title, subtitle, formRow, buttonRow, statusLabel, savedTitle, table);
        content.setPadding(new Insets(15));

        BorderPane root = new BorderPane();
        root.setTop(new VBox(menuBar, toolBar));
        root.setCenter(content);

        stage.setTitle("Lodge Billing Lab");
        stage.setScene(new Scene(root, 1000, 720));
        stage.show();
    }

    private void addColumn(String name, java.util.function.Function<Bill, String> getter) {
        TableColumn<Bill, String> col = new TableColumn<>(name);
        col.setCellValueFactory(c -> new SimpleStringProperty(getter.apply(c.getValue())));
        table.getColumns().add(col);
    }

    // ---------- Actions ----------

    /** Reads the form, validates it and builds a Bill. Throws IllegalArgumentException with a friendly message. */
    private Bill buildBillFromForm(String id) {
        String guest = guestField.getText().trim();
        if (guest.isEmpty()) {
            throw new IllegalArgumentException("Please enter the guest name.");
        }
        String room = roomBox.getValue();
        if (room == null) {
            throw new IllegalArgumentException("Please choose a room type.");
        }
        int nights;
        try {
            nights = Integer.parseInt(nightsField.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Nights must be a whole number, e.g. 3.");
        }
        if (nights < 1) {
            throw new IllegalArgumentException("Nights must be at least 1.");
        }
        BigDecimal extras = parseMoney(extrasField.getText(), "Extras");
        BigDecimal paid = parseMoney(paidField.getText(), "Amount paid");
        return new Bill(id, guest, room, nights, extras, paid, notesArea.getText().trim());
    }

    /** Blank money fields count as 0. Negative or non-numeric values are rejected. */
    private BigDecimal parseMoney(String text, String fieldName) {
        String t = text.trim();
        if (t.isEmpty()) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal v;
        try {
            v = new BigDecimal(t);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a number, e.g. 200 or 200.50.");
        }
        if (v.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative.");
        }
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private void calculate() {
        try {
            Bill b = buildBillFromForm("preview");
            showStatus("Total: " + money(b.total) + "   Balance: " + money(b.balance), false);
        } catch (IllegalArgumentException ex) {
            showStatus(ex.getMessage(), true);
        }
    }

    private void saveBill() {
        try {
            String id = String.format("B%03d", nextBillNumber);
            Bill b = buildBillFromForm(id);
            bills.add(b);
            nextBillNumber++;
            showStatus("Saved " + id + ". Balance: " + money(b.balance), false);
        } catch (IllegalArgumentException ex) {
            showStatus(ex.getMessage(), true);
        }
    }

    private void viewReceipt() {
        Bill b = table.getSelectionModel().getSelectedItem();
        if (b == null) {
            showStatus("Select a bill in the table first.", true);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Bill:        ").append(b.id).append('\n');
        sb.append("Guest:       ").append(b.guest).append('\n');
        sb.append("Room:        ").append(b.room).append(" x ").append(b.nights).append(" night(s)\n");
        sb.append("Room charge: ").append(money(b.roomCharge)).append('\n');
        sb.append("Discount:    -").append(money(b.discount)).append('\n');
        sb.append("Extras:      ").append(money(b.extras)).append('\n');
        sb.append("Total:       ").append(money(b.total)).append('\n');
        sb.append("Paid:        ").append(money(b.paid)).append('\n');
        sb.append("Balance:     ").append(money(b.balance)).append('\n');
        if (!b.notes.isEmpty()) {
            sb.append("Notes:       ").append(b.notes).append('\n');
        }
        showInfo("Receipt " + b.id, sb.toString());
    }

    private void deleteSelected() {
        Bill b = table.getSelectionModel().getSelectedItem();
        if (b == null) {
            showStatus("Select a bill in the table first.", true);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete bill " + b.id + " for " + b.guest + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(answer -> {
            if (answer == ButtonType.YES) {
                bills.remove(b);
                showStatus("Deleted " + b.id + ".", false);
            }
        });
    }

    private void clearForm() {
        guestField.clear();
        roomBox.getSelectionModel().clearSelection();
        nightsField.clear();
        extrasField.clear();
        paidField.clear();
        notesArea.clear();
        statusLabel.setText("");
        guestField.requestFocus();
    }

    // ---------- Helpers ----------
    private void showStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setStyle(error ? "-fx-text-fill: #b00020;" : "-fx-text-fill: #0b5d3b;");
    }

    private void showInfo(String title, String text) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        TextArea area = new TextArea(text);
        area.setEditable(false);
        area.setStyle("-fx-font-family: monospace;");
        a.getDialogPane().setContent(area);
        a.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}