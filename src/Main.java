import javax.swing.*;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        Core core = new Core();
        core.pack();
        core.setLocationRelativeTo(null);
        core.setVisible(true);
    }
}