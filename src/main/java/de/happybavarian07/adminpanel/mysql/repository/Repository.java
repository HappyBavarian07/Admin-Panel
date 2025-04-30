package de.happybavarian07.adminpanel.mysql.repository;

import java.util.Optional;

public interface Repository<T, ID> {
    <S extends T> S save(S entity);

    Optional<T> findById(ID id);

    Iterable<T> findAll();

    long count();

    void delete(T entity);

    boolean existsById(ID id);

    boolean isDatabaseReady();
}
