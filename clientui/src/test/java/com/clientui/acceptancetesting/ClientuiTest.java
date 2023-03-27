package com.clientui.acceptancetesting;

import com.clientui.ClientUiApplication;
import com.clientui.acceptancetesting.config.EurekaContainerConfig;
import com.clientui.acceptancetesting.util.Utils;
//import io.github.bonigarcia.seljup.SeleniumJupiter;
//import io.github.bonigarcia.seljup.SeleniumJupiter;
//import io.github.bonigarcia.seljup.Arguments;
//import io.github.bonigarcia.seljup.DriverCapabilities;
//import io.github.bonigarcia.seljup.Options;
//import io.github.bonigarcia.seljup.SeleniumJupiter;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
//import org.openqa.selenium.By;
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.chrome.ChromeDriver;
import org.junit.jupiter.params.ParameterizedTest;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@Tag("AcceptanceTests")
@SpringBootTest(classes = { ClientUiApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@EnableConfigurationProperties
//@ExtendWith(SeleniumJupiter.class)
@TestPropertySource(locations = "classpath:application-at.properties")
@ContextConfiguration(initializers = { EurekaContainerConfig.Initializer.class })
public class ClientuiTest {

    static ChromeOptions options = new ChromeOptions();

    WebDriver driver;

    private static boolean isMicroServicesStarted = false;

    private final String[] microServicesNames = {"microservice-clientui", "microservice-produits", "microservice-commandes", "microservice-paiement", "microservice-api-gateway", "config-server"};

    @BeforeAll
    static void beforeAll() throws Exception{
        StopAll();
        WebDriverManager.chromedriver().setup();
        options.addArguments("--remote-allow-origins=*");
    }

    @BeforeEach
    void beforeEach() throws Exception{

        driver = new ChromeDriver(options);

        await().atMost(120, SECONDS).until(() -> EurekaContainerConfig.Initializer.eurekaServer.isRunning());
        if(!isMicroServicesStarted){
            String eurekaServerUrl = "http://localhost:"+ EurekaContainerConfig.Initializer.eurekaServer.getFirstMappedPort().toString();
            Utils.startSpringbootMicroservice("../config-server/pom.xml", eurekaServerUrl);
            Utils.startSpringbootMicroservice("../api-gateway/pom.xml", eurekaServerUrl);
            Utils.startSpringbootMicroservice("../microservice-produits/pom.xml", eurekaServerUrl);
            Utils.startSpringbootMicroservice("../microservice-commandes/pom.xml", eurekaServerUrl);
            Utils.startSpringbootMicroservice("../microservice-paiement/pom.xml", eurekaServerUrl);
            await().atMost(300, SECONDS).until(() -> Utils.isAllMicroServicesUp(eurekaServerUrl, microServicesNames));
            Thread.sleep(45000);//Permet d'assurer que les clients sont connectés à Eureka server
            isMicroServicesStarted = true;
        }
    }

    @AfterAll
    private static void afterAll() throws Exception{
        StopAll();
    }

    private static void StopAll() throws Exception{
        Utils.stopSpringbootMicroservice("../microservice-produits/pom.xml");
        Utils.stopSpringbootMicroservice("../microservice-commandes/pom.xml");
        Utils.stopSpringbootMicroservice("../microservice-paiement/pom.xml");
        Utils.stopSpringbootMicroservice("../api-gateway/pom.xml");
        Utils.stopSpringbootMicroservice("../config-server/pom.xml");
    }

    @Test
    void homeShouldHaveCorrectTitleAndCorrectHeaderAnd8Images() throws Exception{
        //given
        driver.get("http://localhost/mcommerce-ui");
        int expectedNumberOfImages = 8;
        String expectedTitle = "Mcommerce";
        String expectedHeader = "Application Mcommerce";

        //when
        int numberOfImages = driver.findElements(By.tagName("img")).size();
        String title = driver.getTitle();
        String header = driver.findElements(By.tagName("h1")).get(0).getText();

        //then
        assertThat(title).contains(expectedTitle);
        assertThat(numberOfImages).isEqualTo(expectedNumberOfImages);
        assertThat(header).isEqualTo(expectedHeader);
    }

    @Test
    void detailProductPageShouldContainsAnImageAndADescription(){
        //given
        driver.get("http://localhost/mcommerce-ui");
        int expectedNumberOfImages = 1;
        String descriptionToFind = "bougie qui fonctionne comme une ampoule mais sans";

        //when
        driver.findElement(By.linkText("Bougie fonctionnant au feu")).click();
        int numberOfImages = driver.findElements(By.tagName("img")).size();
        String pageSource = driver.getPageSource();

        //then
        assertThat(numberOfImages).isEqualTo(expectedNumberOfImages);
        assertThat(pageSource).contains(descriptionToFind);
    }

    @Test
    void fromDetailProductPageYouShouldBeAbleToReturnToHomePage(){
        //given
        String expectedTitle = "Mcommerce";
        String expectedHeader = "Application Mcommerce";
        driver.get("http://localhost/mcommerce-ui");
        driver.findElement(By.linkText("Bougie fonctionnant au feu")).click();

        //when
        driver.findElement(By.linkText("RETOUR À L'ACCUEIL")).click();
        String title = driver.getTitle();
        String header = driver.findElements(By.tagName("h1")).get(0).getText();

        //then
        assertThat(title).contains(expectedTitle);
        assertThat(header).isEqualTo(expectedHeader);
    }

    @Test
    void paymentPageShouldContainsADescription(){
        //given
        String descriptionToFind = "Ici l'utilisateur sélectionne en temps normal un moyen de paiement et entre les informations de sa carte bancaire.";
        driver.get("http://localhost/mcommerce-ui");
        driver.findElement(By.linkText("Bougie fonctionnant au feu")).click();

        //when
        driver.findElement(By.name("submit")).click();
        String pageSource = driver.getPageSource();

        //then
        assertThat(pageSource).contains(descriptionToFind);
    }

    @Test
    void fromPaymentPageYouShouldBeAbleToReturnToHomePage(){
        //given
        String expectedTitle = "Mcommerce";
        String expectedHeader = "Application Mcommerce";
        driver.get("http://localhost/mcommerce-ui");
        driver.findElement(By.linkText("Bougie fonctionnant au feu")).click();
        driver.findElement(By.name("submit")).click();

        //when
        driver.findElement(By.linkText("RETOUR À L'ACCUEIL")).click();
        String title = driver.getTitle();
        String header = driver.findElements(By.tagName("h1")).get(0).getText();

        //then
        assertThat(title).contains(expectedTitle);
        assertThat(header).isEqualTo(expectedHeader);
    }

    @Test
    void confirmationPageShouldContainsADescription(){
        //given
        String descriptionToFind = "Paiement Accepté";
        driver.get("http://localhost/mcommerce-ui");
        driver.findElement(By.linkText("Bougie fonctionnant au feu")).click();
        driver.findElement(By.name("submit")).click();

        //when
        driver.findElement(By.name("submit")).click();
        String pageSource = driver.getPageSource();

        //then
        assertThat(pageSource).contains(descriptionToFind);
    }

    @Test
    void fromConfirmationPageYouShouldBeAbleToReturnToHomePage(){
        //given
        String expectedTitle = "Mcommerce";
        String expectedHeader = "Application Mcommerce";
        driver.get("http://localhost/mcommerce-ui");
        driver.findElement(By.linkText("Bougie fonctionnant au feu")).click();
        driver.findElement(By.name("submit")).click();
        driver.findElement(By.name("submit")).click();

        //when
        driver.findElement(By.linkText("RETOUR À L'ACCUEIL")).click();
        String title = driver.getTitle();
        String header = driver.findElements(By.tagName("h1")).get(0).getText();

        //then
        assertThat(title).contains(expectedTitle);
        assertThat(header).isEqualTo(expectedHeader);
    }


}
