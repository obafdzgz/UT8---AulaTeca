package aulateca.dao;

import aulateca.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class GenericDAO<T> implements IGenericDAO<T> {
    @Override
    public void save(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(entity); // Guarda en la BD
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback(); // Si falla, cancela. No se quedan datos a medias
            throw new RuntimeException("Error al guardar la entidad: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(entity); // Actualiza en la BD
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error al actualizar la entidad: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(session.contains(entity) ? entity : session.merge(entity)); // Borra de la BD
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error al eliminar la entidad: " + e.getMessage(), e);
        }
    }

    @Override
    public T findById(Class<T> clazz, Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.find(clazz, id); // Busca por clave primaria
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public java.util.List<T> findAll(Class<T> clazz) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Consulta HQL genérica: "FROM User", "FROM Resource", etc.
            return session.createQuery("from " + clazz.getName(), clazz).list();
        } catch (Exception e) {
            throw new RuntimeException("Error al listar las entidades: " + e.getMessage(), e);
        }
    }
}
