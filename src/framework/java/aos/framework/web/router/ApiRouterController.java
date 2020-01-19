package aos.framework.web.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("api")
public class ApiRouterController {
    private static final Logger logger = LoggerFactory.getLogger(HttpRouterController.class);
}
