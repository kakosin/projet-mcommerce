package com.mpaiement.integration;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mpaiement.MpaiementApplication;
import com.mpaiement.beans.CommandeBean;
import com.mpaiement.dao.PaiementDao;
import com.mpaiement.model.Paiement;
import com.mpaiement.proxies.MicroserviceCommandeProxy;
import com.mpaiement.web.controller.PaiementController;
import com.mpaiement.web.exceptions.PaiementExistantException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("IntegrationTests")
@SpringBootTest(classes = {MpaiementApplication.class},
        properties = { "spring.cloud.config.enabled:false"},
        webEnvironment = RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-it.properties")
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MicroservicePaiementTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    PaiementDao paiementDao;

    @Autowired
    PaiementController paiementControllerUnderTest;

    @MockBean
    private MicroserviceCommandeProxy microserviceCommandeProxy;

    @Test
    @Order(1)
    void payerUneCommandeNotPaid() throws Exception{
        //given
        Paiement paiement = Paiement.builder()
                .id(1)
                .idCommande(1)
                .montant(10.0)
                .numeroCarte(Long.valueOf("1234567891234567"))
                .build();

        CommandeBean commande = CommandeBean.builder()
                .id(1)
                .dateCommande(LocalDate.now())
                .quantite(1)
                .productId(1)
                .commandePayee(Boolean.FALSE)
                .build();
        Optional<CommandeBean> commandeOpt = Optional.of(commande);

        paiementDao.deleteAllInBatch();

        when(microserviceCommandeProxy.recupererUneCommande(paiement.getIdCommande())).thenReturn(commandeOpt);

        //when

        //then
        mockMvc.perform( MockMvcRequestBuilders
                        .post("/paiement")
                        .content(asJsonString(paiement))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.idCommande").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.idCommande").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.montant").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.montant").value(10.0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.numeroCarte").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.numeroCarte").value("1234567891234567"));
    }

    @Test
    @Order(2)
    void payerUneCommandeAlreadyPaid() throws Exception {
        //given
        Paiement paiement = Paiement.builder()
                .id(2)
                .idCommande(1)
                .montant(10.0)
                .numeroCarte(Long.valueOf("1234567891234567"))
                .build();

        paiementDao.deleteAllInBatch();
        paiementDao.saveAndFlush(paiement);

        //when

        //then
        mockMvc.perform( MockMvcRequestBuilders
                        .post("/paiement")
                        .content(asJsonString(paiement))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof PaiementExistantException))
                .andExpect(result -> assertEquals("Cette commande est déjà payée", result.getResolvedException().getMessage()));

    }

    @Test
    @Order(3)
    void testCommandeBeanToString(){
        //given
        CommandeBean commande = new CommandeBean();
        commande.setId(1);
        commande.setProductId(1);
        commande.setCommandePayee(Boolean.FALSE);
        commande.setDateCommande(LocalDate.now());
        commande.setQuantite(1);

        //when
        String cmdStr = commande.toString();

        //then
        assertThat(cmdStr).contains("id=1");
        assertThat(cmdStr).contains("productId=1");
        assertThat(cmdStr).contains("commandePayee=false");
        assertThat(cmdStr).contains("quantite=1");
        assertThat(cmdStr).contains("dateCommande=" + LocalDate.now().toString());
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
