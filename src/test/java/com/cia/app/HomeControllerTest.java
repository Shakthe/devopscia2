package com.cia.app;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Value("${app.version}")
    private String version;

    @Value("${app.message}")
    private String message;

    @Test
    void homePageShowsMessageAndVersion() throws Exception {
        mvc.perform(get("/"))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString(message)))
           .andExpect(content().string(containsString(version)));
    }

    @Test
    void healthEndpointReportsUp() throws Exception {
        mvc.perform(get("/health"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.status").value("UP"))
           .andExpect(jsonPath("$.version").value(version));
    }

    @Test
    void unknownPathReturns404() throws Exception {
        mvc.perform(get("/does-not-exist")).andExpect(status().isNotFound());
    }
}
