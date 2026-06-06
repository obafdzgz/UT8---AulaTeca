package aulateca.service;

import aulateca.model.User;
import aulateca.dao.UserDAO;

public class UserService {

    private final UserDAO userDAO = new UserDAO();


    //Valida las credenciales de un usuario para permitir el acceso a la aplicación.

    public User login(String username, String password) {
        // Validación campos vacíos
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario y la contraseña no pueden estar vacíos.");
        }

        //Buscamos el usuario en la base de datos a través del DAO
        User user = userDAO.findByUsername(username);

        // Validación para comprobar si existe
        if (user == null) {
            throw new IllegalArgumentException("El nombre de usuario no existe.");
        }

        // Validación de contraseña
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("La contraseña es incorrecta.");
        }

        // Si pasa todos los filtros, devolvemos el user
        return user;
    }
}