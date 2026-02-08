package classes;
import javax.swing.SwingUtilities;


public class main {
    public static void main() {
        SwingUtilities.invokeLater(() -> {
            LoginPage loginPage = new LoginPage();
            loginPage.setVisible(true);
        });
    }
}
