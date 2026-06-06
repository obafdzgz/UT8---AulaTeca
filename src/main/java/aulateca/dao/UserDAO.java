package aulateca.dao;

import aulateca.model.User;
import aulateca.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class UserDAO extends GenericDAO<User> {
    // Metodo para buscar por username (Login)
    public User findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            //Hacemos referencia a la clase User
            Query<User> query = session.createQuery("FROM User WHERE username = :user", User.class);
            query.setParameter("user", username);

            //devuelve el usuario si existe, o null si no lo encuentra
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar usuario por username: " + e.getMessage(), e);
        }
    }
}
