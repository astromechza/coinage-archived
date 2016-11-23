package org.coinage.gui.windows;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.coinage.core.Resources;
import org.coinage.core.helpers.AccountTreeHelper;
import org.coinage.core.models.SubTransaction;
import org.coinage.core.models.Transaction;
import org.coinage.gui.ConnectionSourceProvider;
import org.coinage.gui.components.AccountAutoCompleteComboBox;
import org.coinage.gui.components.HExpander;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;

/**
 * Created At: 2016-11-22
 */
public class ViewTransactionWindow extends BaseWindow
{
    private DecimalFormat displayFormat = new DecimalFormat("#,##0.00");
    private Label commentBoxLabel;
    private Label dateLabel;
    private Label positiveSubtransactionsLabel;
    private Label negativeSubtransactionsLabel;

    {
        displayFormat.setPositivePrefix("R  ");
        displayFormat.setNegativePrefix("R -");
    }

    private DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private Label dateValueLabel;
    private Text commentBox;
    private Button backBtn;
    private Button editBtn;
    private VBox negativeSubtransactions;
    private VBox positiveSubtransactions;

    public ViewTransactionWindow(long transactionId) throws IOException
    {
        super();
        initialise();
        reload(transactionId);
    }

    private void reload(long transactionId)
    {
        ConnectionSource source = ConnectionSourceProvider.get();
        try
        {
            Dao<Transaction, Long> transactionDao = DaoManager.createDao(source, Transaction.class);
            Transaction transaction = transactionDao.queryForId(transactionId);
            Collection<SubTransaction> subtransactions = transaction.getSubTransactions();
            Map<Long, String> nameMap = new AccountTreeHelper(source).nameMap();

            negativeSubtransactions.getChildren().clear();
            positiveSubtransactions.getChildren().clear();
            dateValueLabel.setText(dateFormat.print(transaction.getDatetime()));
            for (SubTransaction st : subtransactions)
            {
                if (st.getValue().compareTo(BigDecimal.ZERO) < 0)
                {
                    negativeSubtransactions.getChildren().add(
                        new HBox(10, new Label(nameMap.get(st.getAccount().getId())), new HExpander(), new Label(displayFormat.format(st.getValue())))
                    );
                }
                else
                {
                    positiveSubtransactions.getChildren().add(
                            new HBox(10, new Label(nameMap.get(st.getAccount().getId())), new HExpander(), new Label(displayFormat.format(st.getValue())))
                    );
                }
            }
            commentBox.setText(transaction.getComment());
            ((VBox) this.getRootLayout()).getChildren().add(new AccountAutoCompleteComboBox(nameMap));
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void initControls()
    {
        dateLabel = new Label("Date:");
        dateValueLabel = new Label();
        negativeSubtransactions = new VBox(10);
        positiveSubtransactions = new VBox(10);
        commentBoxLabel = new Label("Comment:");
        commentBox = new Text();
        backBtn = new Button("Back");
        editBtn = new Button("Edit..");
        positiveSubtransactionsLabel = new Label("To:");
        negativeSubtransactionsLabel = new Label("From:");
    }

    @Override
    public Parent initLayout()
    {
        VBox root = new VBox(10);
        root.setMinWidth(300);
        root.setPadding(new Insets(10));
        root.getChildren().add(new HBox(dateLabel, new HExpander(), dateValueLabel));
        root.getChildren().add(positiveSubtransactionsLabel);
        root.getChildren().add(positiveSubtransactions);
        root.getChildren().add(negativeSubtransactionsLabel);
        root.getChildren().add(negativeSubtransactions);
        root.getChildren().add(commentBoxLabel);
        root.getChildren().add(new BorderPane(commentBox));
        root.getChildren().add(new HBox(10, new HExpander(), backBtn, editBtn));
        return root;
    }

    @Override
    public void bindEvents()
    {
        backBtn.setOnAction(e -> this.getStage().close());
    }

    @Override
    public void applyStyling()
    {
        dateLabel.getStyleClass().add("header-label");
        positiveSubtransactionsLabel.getStyleClass().add("header-label");
        negativeSubtransactionsLabel.getStyleClass().add("header-label");
        commentBoxLabel.getStyleClass().add("header-label");
    }

    @Override
    public Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout());
        scene.getStylesheets().add(this.cssCommon);
        try { scene.getStylesheets().add(Resources.getExternalPath("/resources/css/view-transaction-window.css")); } catch (IOException e) { e.printStackTrace(); }
        this.getStage().setTitle("Coinage - View Transaction");
        this.getStage().setScene(scene);
        this.getStage().setResizable(false);
        this.getStage().sizeToScene();
        return scene;
    }
}
