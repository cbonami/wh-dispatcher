package be.acerta.subscriber;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
public class PostBinController {

    @RequestMapping(value = "/postit", method = POST)
    public ResponseEntity<String> postit(@RequestBody String data) {
        log.debug("Received data: {}", data);
        return ResponseEntity.status(ACCEPTED).build();
    }

}