package org.coinage.gui.components.table;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.coinage.core.models.SubTransaction;
import org.coinage.core.models.Transaction;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimePrinter;

import java.text.DecimalFormat;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created At: 2016-11-17
 */
public class TransactionTable extends TableView<TransactionTableRow>
{
    private final ObservableList<TransactionTableRow> contents;
    private final SimpleStringProperty focusPrefix;
    private DecimalFormat displayFormat = new DecimalFormat("#,##0.00");
    private DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public TransactionTable()
    {
        this(null);
    }

    public TransactionTable(String focusPrefix)
    {
        this.focusPrefix = new SimpleStringProperty(focusPrefix);
        this.contents = FXCollections.observableArrayList();
        this.setItems(this.contents);
        displayFormat.setNegativePrefix("R -");
        displayFormat.setPositivePrefix("R  ");

        TableColumn<TransactionTableRow, String> datetimeCol = new TableColumn<>("Date Time");
        TableColumn<TransactionTableRow, String> commentCol = new TableColumn<>("Comment");
        TableColumn<TransactionTableRow, TransactionTableRow> accountCol = new TableColumn<>("Accounts");
        TableColumn<TransactionTableRow, TransactionTableRow> valuesCol = new TableColumn<>("Values");
        TableColumn<TransactionTableRow, String> balanceCol = new TableColumn<>("Balance");

        this.getColumns().setAll(
                datetimeCol,
                commentCol,
                accountCol,
                valuesCol,
                balanceCol
        );

        this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        datetimeCol.setCellValueFactory(param -> new SimpleStringProperty(dateFormat.print(param.getValue().getDateTime())));
        commentCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getComment()));
        accountCol.setCellValueFactory(
                param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        valuesCol.setCellValueFactory(
                param -> new ReadOnlyObjectWrapper<>(param.getValue()));

        accountCol.setCellFactory(
                new Callback<TableColumn<TransactionTableRow, TransactionTableRow>, TableCell<TransactionTableRow, TransactionTableRow>>() {
                    @Override
                    public TableCell<TransactionTableRow, TransactionTableRow> call(TableColumn<TransactionTableRow, TransactionTableRow> param)
                    {
                        return new TableCell<TransactionTableRow, TransactionTableRow>(){
                            @Override
                            protected void updateItem(TransactionTableRow item, boolean empty)
                            {
                                super.updateItem(item, empty);
                                if (!empty && item != null)
                                {
                                    VBox v = new VBox();
                                    for (SubTransaction st : item.getSubTransactions())
                                    {
                                        Label l = new Label(st.getAccount().getName());
                                        boolean focused = (TransactionTable.this.focusPrefix.isNotNull().get() && st.getAccount().getName().startsWith(TransactionTable.this.focusPrefix.get()));
                                        l.getStyleClass().add(focused ? "table-account-focused" : "table-account-unfocused");
                                        v.getChildren().add(l);
                                    }
                                    this.setGraphic(v);
                                }
                                else
                                {
                                    this.setGraphic(null);
                                }
                            }
                        };
                    }
                });

        valuesCol.setCellFactory(
                new Callback<TableColumn<TransactionTableRow, TransactionTableRow>, TableCell<TransactionTableRow, TransactionTableRow>>() {
                    @Override
                    public TableCell<TransactionTableRow, TransactionTableRow> call(TableColumn<TransactionTableRow, TransactionTableRow> param)
                    {
                        return new TableCell<TransactionTableRow, TransactionTableRow>(){
                            @Override
                            protected void updateItem(TransactionTableRow item, boolean empty)
                            {
                                super.updateItem(item, empty);
                                if (!empty && item != null)
                                {
                                    VBox v = new VBox();
                                    for (SubTransaction st : item.getSubTransactions())
                                    {
                                        Label l = new Label(displayFormat.format(st.getValue()));
                                        boolean focused = (TransactionTable.this.focusPrefix.isNotNull().get() && st.getAccount().getName().startsWith(TransactionTable.this.focusPrefix.get()));
                                        l.getStyleClass().add(focused ? "table-value-focused" : "table-value-unfocused");
                                        v.getChildren().add(l);
                                    }
                                    this.setGraphic(v);
                                }
                                else
                                {
                                    this.setGraphic(null);
                                }
                            }
                        };
                    }
                });


        balanceCol.setCellValueFactory(param -> new SimpleStringProperty(displayFormat.format(param.getValue().getBalance())));
    }

    public SimpleStringProperty focusPrefix()
    {
        return focusPrefix;
    }
}
