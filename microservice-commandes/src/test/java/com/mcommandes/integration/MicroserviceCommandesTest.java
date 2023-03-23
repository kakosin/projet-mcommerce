package com.mcommandes.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mcommandes.McommandesApplication;
import com.mcommandes.dao.CommandesDao;
import com.mcommandes.model.Commande;
import com.mcommandes.web.controller.CommandeController;
import com.mcommandes.web.exceptions.CommandeNotFoundException;
import com.mcommandes.web.exceptions.ImpossibleAjouterCommandeException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("IntegrationTests")
@SpringBootTest(classes = {McommandesApplication.class},
        properties = { "spring.cloud.config.enabled:false"},
        webEnvironment = RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-it.properties")
@AutoConfigureMockMvc
public class MicroserviceCommandesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    CommandesDao commandesDao;

    @Autowired
    CommandeController commandeControllerUnderTest;

    @Test
    void ajouterCommandeValid() throws Exception{
        //given
        Commande commande = Commande.builder()
                .id(1)
                .dateCommande(LocalDate.now())
                .quantite(1)
                .productId(1)
                .commandePayee(Boolean.FALSE)
                .build();

        //when

        //then
        mockMvc.perform( MockMvcRequestBuilders
                        .post("/commandes")
                        .content(asJsonString(commande))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.quantite").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.quantite").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.productId").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.productId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.commandePayee").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.commandePayee").value("false"));
    }

    @Test
    void ajouterCommandeInvalid() throws Exception{
        //given
        Commande commande = null;
        //when

        //then
        mockMvc.perform( MockMvcRequestBuilders
                        .post("/commandes")
                        .content(asJsonString(commande))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        assertThatThrownBy(() -> commandeControllerUnderTest.ajouterCommande(null))
                .isInstanceOf(ImpossibleAjouterCommandeException.class)
                .hasMessageContaining("Impossible d'ajouter cette commande");
    }

    @Test
    void recupererUneCommandeExists() throws Exception{
        //given
        Commande commande = Commande.builder()
                .id(1)
                .dateCommande(LocalDate.now())
                .quantite(1)
                .productId(1)
                .commandePayee(Boolean.FALSE)
                .build();

        commandesDao.saveAndFlush(commande);

        //when

        //then
        ResultActions resultActions = mockMvc
                .perform(get("/commandes/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.quantite").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.quantite").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.productId").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.productId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.commandePayee").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.commandePayee").value("false"));

    }

    @Test
    void recupererUneCommandeNotExists() throws Exception{
        //given

        //when

        //then
        ResultActions resultActions = mockMvc
                .perform(get("/commandes/99"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof CommandeNotFoundException))
                .andExpect(result -> assertEquals("Cette commande n'existe pas", result.getResolvedException().getMessage()));
    }

    @Test
    void updateCommand() throws Exception{
        //given
        Commande commande = Commande.builder()
                .id(1)
                .dateCommande(LocalDate.now())
                .quantite(1)
                .productId(1)
                .commandePayee(Boolean.FALSE)
                .build();

        commandesDao.saveAndFlush(commande);

        commande.setCommandePayee(Boolean.TRUE);
        commande.setQuantite(2);
        //when

        //then
        mockMvc.perform( MockMvcRequestBuilders
                        .put("/commandes")
                        .content(asJsonString(commande))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/commandes/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.quantite").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.quantite").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.productId").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.productId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.commandePayee").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.commandePayee").value("true"));


    }

    private static String asJsonString(final Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
