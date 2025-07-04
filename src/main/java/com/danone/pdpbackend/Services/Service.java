package com.danone.pdpbackend.Services;

import java.util.List;

public interface Service<T> {
    List<T> getAll();

    T getById(Long id);

    T create(T entity);

    T update(Long id, T entityDetails);

    void delete(Long id);

    List<T> getByIds(List<Long> ids);
}