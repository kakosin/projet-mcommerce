package com.mproduits.web.controller;

import com.mproduits.configurations.ApplicationPropertiesConfiguration;
import com.mproduits.dao.ProductDao;
import com.mproduits.model.Product;
import com.mproduits.web.exceptions.ProductNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("produits")
public class ProductController implements HealthIndicator {

    @Autowired
    ProductDao productDao;

    @Autowired
    ApplicationPropertiesConfiguration appProperties;

    @Override
    public Health health() {

        List<Product> products = productDao.findAll();

        if(products.isEmpty()) {
            return Health.down().build();
        }
        return Health.up().build();
    }

    // Affiche la liste de tous les produits disponibles
    @GetMapping
    public List<Product> listeDesProduits(){

        List<Product> products = productDao.findAll();

        if(products.isEmpty()){
            throw new ProductNotFoundException("Aucun produit n'est disponible à la vente");
        }

//        List<Product> listeLimitee = products.subList(0, appProperties.getLimitDeProduits());
        //Correction de défaut, ajout de la fonction Math.min() pour éviter un java.lang.IndexOutOfBoundsException lorsque le DOA retourne moins de produits qu'indiquer par le appProperties
        List<Product> listeLimitee = products.subList(0, Math.min(appProperties.getLimitDeProduits(), products.size()));

        log.info("Récupération de la liste des produits");

        return listeLimitee;

    }

    //Récuperer un produit par son id
    @GetMapping( value = "{id}")
    public Optional<Product> recupererUnProduit(@PathVariable int id) {

        Optional<Product> product = productDao.findById(id);

        if(!product.isPresent()){
            throw new ProductNotFoundException("Le produit correspondant à l'id " + id + " n'existe pas");
        }

        return product;
    }
}

