package be.acerta.webhook.dispatcher.persistence;

import be.acerta.webhook.dispatcher.model.Application;
import org.springframework.data.repository.CrudRepository;

public interface ApplicationRepository extends CrudRepository<Application, String> {

    // @Modifying
    // @Transactional
    // @Query("update Application d set d.online = true where d.id = :id")
    // int setOnline(@Param("id") String id);

    // @Modifying
    // @Transactional
    // @Query("update Application d set d.online = false where d.id = :id")
    // int setOffline(@Param("id") String id);

}
