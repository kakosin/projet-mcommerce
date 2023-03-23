package com.mproduits.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mproduits.MproduitsApplication;
import com.mproduits.dao.ProductDao;
import com.mproduits.model.Product;
import com.mproduits.web.controller.ProductController;
import com.mproduits.web.exceptions.ProductNotFoundException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

@Tag("IntegrationTests")
@SpringBootTest(classes = {MproduitsApplication.class},
        properties = { "spring.cloud.config.enabled:false"},
        webEnvironment = RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-it.properties")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MicroserviceProduitsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    ProductDao productDao;

    @Autowired
    ProductController productControllerUnderTest;

    @Test
    @Order(1)
    public void listeDesProduitsContainsAllProducts() throws Exception{
        //given
        Product product1 = Product.builder().id(8)
                .titre("titre1")
                .description("desc1")
                .image("img1")
                .prix(10.0)
                .build();

        Product product2 = Product.builder().id(9)
                .titre("titre2")
                .description("desc2")
                .image("img2")
                .prix(20.0)
                .build();
        List<Product> productList = new ArrayList<Product>();
        productList.add(product1);
        productList.add(product2);
        productDao.saveAllAndFlush(productList);

        //when
        //then
        ResultActions resultActions = mockMvc
                .perform(get("/produits"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString("desc1")))
                        .andExpect(content().string(containsString("desc2")));
    }

    @Test
    @Order(3)
    public void listeDesProduitsContainsNoProduct() throws Exception{
        //given
        productDao.deleteAllInBatch();

        //when
        //then
        ResultActions resultActions = mockMvc
                .perform(get("/produits"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ProductNotFoundException));
    }

    @Test
    @Order(2)
    public void recupererUnProduitExistant() throws Exception{
        //given
        Product product1 = Product.builder().id(8)
                .titre("titre1")
                .description("desc1")
                .image("img1")
                .prix(10.0)
                .build();

        productDao.saveAndFlush(product1);

        //when
        //then
        ResultActions resultActions = mockMvc
                .perform(get("/produits/8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("desc1")));
    }

    @Test
    public void recupererUnProduitInexistant() throws Exception{
        //given
        int idProduitInexistant = 55;
        //when
        //then
        ResultActions resultActions = mockMvc
                .perform(get("/produits/" + idProduitInexistant))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ProductNotFoundException));

    }


    @Test
    public void healthIsUpWhenThereIsProductInDB(){
        //given
        Product product1 = Product.builder().id(8)
                .titre("titre1")
                .description("desc1")
                .image("img1")
                .prix(10.0)
                .build();
        productDao.saveAndFlush(product1);
        Health up = Health.up().build();

        //when
        Health currentHealth = productControllerUnderTest.health();

        //then
        assertThat(currentHealth).isEqualTo(up);
    }

    @Test
    public void healthIsDownWhenThereIsNoProductInDB(){
        //given
        productDao.deleteAllInBatch();
        Health down = Health.down().build();

        //when
        Health currentHealth = productControllerUnderTest.health();

        //then
        assertThat(currentHealth).isEqualTo(down);
    }

    @Test
    void testProduitToString(){
        //given
        Product produit = new Product();
        produit.setId(1);
        produit.setTitre("titre1");
        produit.setPrix(10.0);
        produit.setImage("img1");
        produit.setDescription("desc1");

        //when
        String prodStr = produit.toString();

        //then
        assertThat(prodStr).contains("id=" + produit.getId());
        assertThat(prodStr).contains("titre=" + produit.getTitre());
        assertThat(prodStr).contains("prix=" + produit.getPrix());
        assertThat(prodStr).contains("image=" + produit.getImage());
        assertThat(prodStr).contains("description=" + produit.getDescription());
    }
}
