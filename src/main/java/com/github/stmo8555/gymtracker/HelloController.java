package com.github.stmo8555.gymtracker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

  @GetMapping("/hello")
  public String hello() {
    return "<div style='color:green;'>Hello</div>";
  }

}
