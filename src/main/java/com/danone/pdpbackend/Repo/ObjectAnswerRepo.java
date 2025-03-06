package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.entities.ObjectAnswered;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;


@org.springframework.stereotype.Repository
public interface ObjectAnswerRepo extends Repository<ObjectAnswered, Long> {


    ObjectAnswered save(ObjectAnswered objectAnswered);

    void deleteById(Long id);



    ObjectAnswered findById(Long id);

    void flush();

    void delete(ObjectAnswered objectAnswered);
}
