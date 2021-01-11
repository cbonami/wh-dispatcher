package be.acerta.webhook.dispatcher.persistence;

import be.acerta.webhook.dispatcher.model.Application;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface ApplicationRepository extends CrudRepository<Application, Long> {

    //@Modifying
    @Transactional
    //@Query("update Application d set d.online = true where d.id = :id")
    int setDestinationOnline(@Param("id") Long id);

    //@Modifying
    @Transactional
    //@Query("update Application d set d.online = false where d.id = :id")
    int setDestinationOffline(@Param("id") Long id);

}
