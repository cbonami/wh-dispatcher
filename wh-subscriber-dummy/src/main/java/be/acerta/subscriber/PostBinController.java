package be.acerta.subscriber;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PostBinController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PostBinController.class);

    @RequestMapping(value = "/postit", method = POST)
    public ResponseEntity<String> postit(@RequestBody String data) {
        log.debug("Received data: {}", data);
        return ResponseEntity.status(ACCEPTED).build();
    }

}
