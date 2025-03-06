package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.entities.ObjectAnsweredEntreprises;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;


@org.springframework.stereotype.Repository
public interface ObjectAnswerEntreprisesRepo extends Repository<ObjectAnsweredEntreprises, Long> {


    //void save(ObjectAnswered objectAnswered);

    ObjectAnsweredEntreprises save(ObjectAnsweredEntreprises objectAnsweredEntreprises);

    ObjectAnsweredEntreprises findById(Long id);
}
