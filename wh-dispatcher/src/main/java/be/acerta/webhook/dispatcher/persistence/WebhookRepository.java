package be.acerta.webhook.dispatcher.persistence;

import java.util.List;
import java.util.Optional;

import be.acerta.webhook.dispatcher.model.Webhook;
import org.springframework.data.repository.CrudRepository;

public interface WebhookRepository extends CrudRepository<Webhook, String> {

    Optional<Webhook> findByName(String name);

    // @Modifying
    // @Transactional
    // @Query("update Application d set d.online = true where d.id = :id")
    // int setOnline(@Param("id") String id);

    // @Modifying
    // @Transactional
    // @Query("update Application d set d.online = false where d.id = :id")
    // int setOffline(@Param("id") String id);

}
