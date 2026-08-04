package core.clients;

import core.settings.ApiEndpoints;
import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.Matchers.equalTo;

public class APIClient {

    private final String baseUrl;
    private String token;


    public APIClient() {
        this.baseUrl = determineBaseUrl();
    }


    private String determineBaseUrl() {
        String environment = System.getProperty("env", "test");
        String configFileName = "application-" + environment + ".properties";

        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFileName)) {
            if (input == null) {
                throw new IllegalStateException("Configuration file not found: "
                        + configFileName);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load configuration file: " + configFileName, e);
        }
        return properties.getProperty("baseUrl");
    }

    // Настройка базовых параметров HTTP-запросов
    private RequestSpecification getRequestSpec() {
        return RestAssured.given()
                .baseUri(baseUrl)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .filter(addAuthTokenFilter());
    }

    // Настройка базовых параметров HTTP-запросов
    public Response ping() {
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.PING.getPath()) //Используем ENUM Для эндпоинта /ping
                .then()
                .statusCode(201) //Ожидаемы статус-код 201 Created
                .extract()
                .response();

    }

    // Метод для получения токена
    public void createToken(String username, String password) {
        // Формирование JSON тела для запроса
        String requestBody = String.format("{ \"username\": \"%s\", \"password\": \"%s\" }", username, password);
        // Отправка POST-запроса на эндпоинт для аутентификации и получение токена
        Response response = getRequestSpec()
                .body(requestBody) // Устанавливаем тело запроса
                .when()
                .post(ApiEndpoints.AUTH.getPath()) // POST-запрос на эндпоинт аутентификации
                .then()
                .statusCode(200) // Проверяем, что статус ответа 200 (ОК)
                .extract()
                .response();
        // Извлечение токена из ответа и сохранение в переменной
        token = response.jsonPath().getString("token");
    }

    private Filter addAuthTokenFilter() {
        return (FilterableRequestSpecification requestSpec,
                FilterableResponseSpecification responseSpec, FilterContext ctx) -> {
            if (token != null) {
                requestSpec.header("Cookie", "token=" + token);
            }
            return ctx.next(requestSpec, responseSpec); // Продолжает выполнение запроса
        };
    }

    // GET запрос на эндпоинт /booking
    public Response getBooking() {
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.BOOKING.getPath()) //Используем ENUM Для эндпоинта /ping
                .then()
                .statusCode(200) //Ожидаемы статус-код 200
                .extract()
                .response();
    }

    public Response getBookingById(int id) {
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.BOOKING.getPath() + "/" + id) //Используем ENUM Для эндпоинта /ping
                .then()
                //.statusCode(200) //Ожидаемы статус-код 200
                .extract()
                .response();
    }

    // DELETE запрос на эндпоинт /booking
    public Response deleteBooking(int bookingId) {
        return getRequestSpec()
                .pathParam("id", bookingId) // указываем path parameter для ID
                .when()
                .delete(ApiEndpoints.BOOKING.getPath() + "/{id}")
                .then()
                .log().all()
                .statusCode(201) //Ожидаемы статус-код 201
                .extract()
                .response();
    }

    public Response createBooking(String newBooking) {
        return getRequestSpec()
                .body(newBooking)
                .log().all()
                .when()
                .post(ApiEndpoints.BOOKING.getPath())
                .then()
                .log().all()
                .extract()
                .response();
    }

    public Response putBookingById(int id, String newBooking) {
        return getRequestSpec()
                .body(newBooking)
                .log().all()
                .when()
                .put(ApiEndpoints.BOOKING.getPath() + "/" + id) //Используем ENUM Для эндпоинта /ping
                .then()
                .log().all()
                .statusCode(200) //Ожидаемы статус-код 200
                .extract()
                .response();
    }

    public Response patchBookingById(int id, String partialBooking) {
        return getRequestSpec()
                .body(partialBooking)
                .log().all()
                .when()
                .patch(ApiEndpoints.BOOKING.getPath() + "/" + id) //Используем ENUM Для эндпоинта /ping
                .then()
                .log().all()
                .statusCode(200) //Ожидаемы статус-код 200
                .extract()
                .response();
    }

}