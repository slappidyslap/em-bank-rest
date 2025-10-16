package kg.musabaev.em_bank_rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmBankRestApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
	void contextLoads() {
	}

//    @Test
    public void getAll() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .param("email", "")
                        .param("fullName", "")
                        .param("id", "0")
                        .param("role", "")
                        .param("pageNumber", "0")
                        .param("pageSize", "0")
                        .param("sort", ""))
                .andExpect(status().isOk())
                .andDo(print());
    }
/*@Test
    public void create() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("user").password("1").roles("ADMIN")))
                .andExpect(status().isOk())
                .andDo(print());
    }*/
}
