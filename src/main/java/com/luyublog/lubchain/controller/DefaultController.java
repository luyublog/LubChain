package com.luyublog.lubchain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * description:
 *
 * @author luyublog
 * @date 2023/8/14 16:18.
 */
@Controller
public class DefaultController {
    @GetMapping("/")
    public String redirectToSwagger() {
        return "redirect:/doc.html";
    }
}

