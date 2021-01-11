package be.acerta.webhook.dispatcher.persistence;


import be.acerta.webhook.dispatcher.model.Application;
import be.acerta.webhook.dispatcher.model.Message;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface MessageRepository extends CrudRepository<Message, Long> {

    List<Message> findAllByApplicationOrderByIdAsc(Application destination);

}
