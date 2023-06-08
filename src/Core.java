import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

class Core extends JFrame {
    SQL_Connection sql;
    JTabbedPane tabs;
    JMenuBar menuBar;
    JMenu TableMenu;
    ArrayList<JTable> jTables;
    public Core() throws SQLException {
        super("JTable Example");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        sql = new SQL_Connection();
        jTables = new ArrayList<>();
        tabs = new JTabbedPane();
        menuBar = new JMenuBar();
        TableMenu = new JMenu("Table");
        start();
        AddTable();
        DeleteTable();
        InsertRow();
        DeleteRow();
        menuBar.add(TableMenu);
        setJMenuBar(menuBar);
    }
    public String doID(){
        return "id INTEGER PRIMARY KEY AUTOINCREMENT, ";
    }
    public void setTableColumns(DefaultTableModel model, ResultSet tableData, int columnCount) throws SQLException {
        for (int i = 2; i <= columnCount; i++) {
            model.addColumn(tableData.getMetaData().getColumnName(i));
        }
    }
    public String[] getColumnNames(JTable table) throws SQLException {
        String[] columnNames = new String[table.getColumnCount()];
        for (int i = 0; i < table.getColumnCount(); i++) {
           columnNames[i] = table.getColumnName(i);
        }
        return columnNames;
    }
    public void setTableRows(DefaultTableModel model, ResultSet tableData, int columnCount) throws SQLException {
        while (tableData.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 2; i <= columnCount; i++) {
                row[i - 2] = tableData.getObject(i);
            }
            model.addRow(row);
        }
    }
    public int getRowID(String tableName,int SelectedRow) throws SQLException {
        ResultSet tableData = sql.CreateResultSet("SELECT id FROM " + tableName);
        int rowID = 0;
        for (int i = 0; i <= SelectedRow; i++) {
            tableData.next();
            rowID = tableData.getInt(1);
        }
        return rowID;

    }
    public JTable getSelectedTable() {
        return jTables.get(tabs.getSelectedIndex());
    }
    public String getSelectedTableName() throws SQLException {

        return tabs.getTitleAt(tabs.getSelectedIndex());
    }
    public void addTableChangeListener(DefaultTableModel model, String tableName) throws SQLException {
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                System.out.println("row: " + row + " column: " + column);
                if (row == -1 || column == -1) {
                    return;
                }
                String columnName = model.getColumnName(column);
                Object value = model.getValueAt(row, column);
                System.out.println("Column name: " + columnName + " Value: " + value);
                System.out.println("UPDATE " + tableName + " SET " + columnName + " = '"+value+"' WHERE id = ");
                try {
                    sql.SDUpdate("UPDATE " + tableName + " SET " + columnName + " = '"+value+"' WHERE id = "+getRowID(tableName,row));
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
public void addTableMouseListener(JTable table) {
    table.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                int row = table.rowAtPoint(e.getPoint());
                try {
                    new QuickSearch(Core.this, row, getSelectedTableName());
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    });

}
    public Object[][] getData() {
        try {
            String tableName = getSelectedTableName();
            ResultSet rs = sql.CreateResultSet("SELECT * FROM " + tableName);
            int columns = getSelectedTable().getColumnCount();
            int rows = getSelectedTable().getRowCount();
            Object[][] data = new Object[rows][columns];
            int i = 0;
            while (rs.next()) {
                for (int j = 0; j < columns; j++) {
                    data[i][j] = rs.getObject(j + 2);
                }
                i++;
            }
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public void start() throws SQLException {
        while (sql.getTablesSet().next()) {
            if (sql.getTablesSet().getString(1).equals("sqlite_sequence")) {
                continue;
            }
            String tableName = sql.getTablesSet().getString(1);
            ResultSet tableData = sql.CreateResultSet("SELECT * FROM " + tableName);
            int columnCount = tableData.getMetaData().getColumnCount();

            DefaultTableModel model = new DefaultTableModel();
            JTable table = new JTable(model);
            table.setRowHeight(24);

            jTables.add(table);

            model.setColumnCount(0);
            model.setRowCount(0);
            setTableColumns(model, tableData, columnCount);
            setTableRows(model, tableData, columnCount);
            addTableChangeListener(model, tableName);
            addTableMouseListener(table);
            tabs.addTab(tableName, new JScrollPane(table));
        }
        add(tabs);
    }

    public void AddTable() {
        JMenuItem menuItem = new JMenuItem("New Table");
        menuItem.setMnemonic(KeyEvent.VK_F);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tableName = JOptionPane.showInputDialog("Enter table name:", "");
                String columns = JOptionPane.showInputDialog("Enter column names separated by comma+space:", "");
                if ((tableName==null || columns==null)||(tableName.equals("") || columns.equals(""))){
                    tableName = "";
                    columns = "";
                    return;
                }
                while (columns.contains(", ")) {
                    columns = columns.replace(", ", " varchar(255),");
                }
                columns += " varchar(255)";
                System.out.println("CREATE TABLE " + tableName + " ("+doID() + columns+")");

                try {
                    sql.SDUpdate("CREATE TABLE " + tableName + " ("+doID() + columns +")");
                    ResultSet tableData = sql.CreateResultSet("SELECT * FROM " + tableName);
                    int columnCount = tableData.getMetaData().getColumnCount();

                    DefaultTableModel model = new DefaultTableModel();
                    JTable table = new JTable(model);
                    jTables.add(table);
                    model.setColumnCount(0);
                    model.setRowCount(0);

                    setTableColumns(model, tableData, columnCount);
                    tabs.addTab(tableName, new JScrollPane(table));
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

            }
        });
        TableMenu.add(menuItem);
    }
    public void InsertRow(){
            JMenuItem menuItem = new JMenuItem("Add Row");
        menuItem.setMnemonic(KeyEvent.VK_F);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tableName = tabs.getTitleAt(tabs.getSelectedIndex());
                StringBuilder columnNames = new StringBuilder();
                StringBuilder values = new StringBuilder();
                try {
                    ResultSet tableData = sql.CreateResultSet("SELECT * FROM " + tableName);
                    int columnCount = tableData.getMetaData().getColumnCount();

                    for (int i = 2; i <= columnCount; i++) {
                        String columnName = tableData.getMetaData().getColumnName(i);
                        String value = JOptionPane.showInputDialog("Enter value for " + columnName + ":", "");
                        if (value==null){
                            value= "";
                            return;
                        }
                        columnNames.append(columnName).append(", ");
                        values.append("'").append(value).append("', ");
                    }

                    columnNames = new StringBuilder(columnNames.substring(0, columnNames.length() - 2));
                    values = new StringBuilder(values.substring(0, values.length() - 2));
                    sql.SDUpdate("INSERT INTO " + tableName + " (" + columnNames + ") VALUES (" + values + ");");

                    ReloadTable(tableName);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
    }
});
        TableMenu.add(menuItem);
    }

    public void ReloadTable(String tableName) throws SQLException {
        ResultSet tableData = sql.CreateResultSet("SELECT * FROM " + tableName);
        int columnCount = tableData.getMetaData().getColumnCount();

        DefaultTableModel model = (DefaultTableModel) jTables.get(tabs.indexOfTab(tableName)).getModel();

        model.setColumnCount(0);
        model.setRowCount(0);

        setTableColumns(model, tableData, columnCount);
        setTableRows(model, tableData, columnCount);
    }

    public void DeleteRow() {
        JMenuItem deleteRowMenu = new JMenuItem("Delete Chosen Row");
        deleteRowMenu.setMnemonic(KeyEvent.VK_DELETE);
        deleteRowMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.ALT_MASK));
        deleteRowMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tableName = tabs.getTitleAt(tabs.getSelectedIndex());
                JTable table = jTables.get(tabs.getSelectedIndex());
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    // Delete the selected row from the SQLite database
                    try {
                       sql.SDUpdate("DELETE FROM "+tableName+" WHERE id = "+getRowID(tableName,selectedRow)+";");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    // Delete the selected row from the JTable
                    model.removeRow(selectedRow);
                }
                else {
                    JOptionPane.showMessageDialog(table, "Please select a row to delete.");
                }
            }
        });
        TableMenu.add(deleteRowMenu);
    }
    public void DeleteTable() {
        JMenuItem deleteTableItem = new JMenuItem("Delete Table");
        deleteTableItem.setMnemonic(KeyEvent.VK_DELETE);
        deleteTableItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        deleteTableItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tableName = JOptionPane.showInputDialog("Enter table name:", "");
                if (tableName==null || tableName.equals("")){
                    tableName = "";
                    return;
                }
                try {
                    sql.SDUpdate("DROP TABLE " + tableName);
                    JOptionPane.showMessageDialog(Core.this, "Table '" + tableName + "' deleted successfully!");
                    jTables.remove(jTables.get(tabs.indexOfTab(tableName)));
                    tabs.removeTabAt(tabs.indexOfTab(tableName));
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        TableMenu.add(deleteTableItem);
    }
}
