package aulateca.main;

import aulateca.view.LoginView;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        // Iniciar la interfaz gráfica dentro del Hilo de Despacho de Eventos para evitar bloqueos y cuelgues visuales.
        SwingUtilities.invokeLater(() -> {
            // Instanciamos la ventana de Login y la hacemos visible
            LoginView ventanaLogin = new LoginView();
            ventanaLogin.setVisible(true);
        });
    }
}