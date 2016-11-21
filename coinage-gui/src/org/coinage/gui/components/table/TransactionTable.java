package org.coinage.gui.components.table;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
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

import java.text.DecimalFormat;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created At: 2016-11-17
 */
public class TransactionTable extends TableView<TransactionTableRow>
{
    private final ObservableList<TransactionTableRow> contents;
    private DecimalFormat displayFormat = new DecimalFormat("#,##0.00");

    public TransactionTable()
    {
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

        datetimeCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDateTime().toString()));
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
                                        v.getChildren().add(new Label(st.getAccount().getName()));
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
                                        v.getChildren().add(new Label(displayFormat.format(st.getValue())));
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
}
