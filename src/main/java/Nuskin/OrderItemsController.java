package Nuskin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OrderItemsController {

    @RequestMapping("/orderitems")
    public String orderItems(@RequestParam String orderNumber, Model model) {
    	model.addAttribute("orderNumber", orderNumber);
        return "orderitems";
    }

}
