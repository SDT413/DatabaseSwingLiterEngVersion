import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

class QuickSearch extends JFrame {
    private final JTextField[] textFields;
    private final Core core;
    private final String tableName;
    private JList<String> suggestionList;
    private JScrollPane suggestionListScroller;
    String[] columnsNames;
    int selectedRow;

    public QuickSearch(Core core, int row, String tableName) throws SQLException {
        this.core = core;
        this.tableName = tableName;
        this.selectedRow = row;
        System.out.println(tableName);
        setLayout(new GridLayout(0, 2));
        columnsNames = core.getColumnNames(core.getSelectedTable());
        int columns = core.getColumnNames(core.getSelectedTable()).length;
        textFields = new JTextField[columns];
        for (int i = 0; i < columns; i++) {
            add(new JLabel(core.getColumnNames(core.getSelectedTable())[i]));
            textFields[i] = new JTextField(core.getData()[row][i].toString());
            add(textFields[i]);
        }


        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String query = "UPDATE "+tableName+" SET "+" WHERE id = "+core.getRowID(tableName,row)+";";
                    StringBuilder columns = new StringBuilder();
                    for (int i = 0; i < textFields.length; i++) {
                        columns.append(core.getColumnNames(core.getSelectedTable())[i]).append(" = '").append(textFields[i].getText()).append("', ");
                    }
                    query = query.replace("SET", "SET "+ columns.substring(0, columns.toString().length()-2));
                    core.sql.SDUpdate(query);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                try {
                    core.ReloadTable(tableName);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                QuickSearch.this.dispose();
            }
        });
        add(saveButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                QuickSearch.this.dispose();
            }
        });
        add(cancelButton);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }
}