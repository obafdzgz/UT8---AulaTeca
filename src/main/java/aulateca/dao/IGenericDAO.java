package aulateca.dao;

import java.util.List;

// interfaz genérica que sirve para cualquier entidad del proyecto
// <T> CRUD para cualquier objeto, no tengo que estar tipando cada metodo

public interface IGenericDAO<T> {
    void save(T entity);

    void update(T entity);

    void delete(T entity);

    T findById(Class<T> clazz, Integer id);

    List<T> findAll(Class<T> clazz);
}
