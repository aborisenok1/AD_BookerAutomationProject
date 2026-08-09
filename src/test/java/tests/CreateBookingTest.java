package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import core.clients.APIClient;
import core.models.Booking;
import core.models.BookingDates;
import core.models.CreatedBooking;
import core.models.NewBooking;
import io.restassured.response.Response;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateBookingTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private CreatedBooking createdBooking;
    private NewBooking newBooking;

    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
        newBooking = new NewBooking();
        newBooking.setFirstname("John");
        newBooking.setLastname("Doe");
        newBooking.setTotalprice(150);
        newBooking.setDepositpaid(true);
        newBooking.setBookingdates(new BookingDates("2024-01-01", "2024-01-05"));
        newBooking.setAdditionalneeds("Breakfast");

    }


    @Test
    public void one_createBooking() throws JsonProcessingException {
        String requestBody = objectMapper.writeValueAsString(newBooking);
        System.out.println(requestBody);
        Response response = apiClient.createBooking(requestBody);

        assertThat(response.getStatusCode()).isEqualTo(200);

        String responseBody = response.asString();
        createdBooking = objectMapper.readValue(responseBody, CreatedBooking.class);

        assertThat(createdBooking).isNotNull();
        assertEquals(createdBooking.getBooking().getFirstname(), newBooking.getFirstname());
        assertEquals(createdBooking.getBooking().getLastname(), newBooking.getLastname());
        assertEquals(createdBooking.getBooking().getTotalprice(), newBooking.getTotalprice());
        assertEquals(createdBooking.getBooking().isDepositpaid(), newBooking.isDepositpaid());
        assertEquals(createdBooking.getBooking().getBookingdates().getCheckin(), newBooking.getBookingdates().getCheckin());
        assertEquals(createdBooking.getBooking().getBookingdates().getCheckout(), newBooking.getBookingdates().getCheckout());
        assertEquals(createdBooking.getBooking().getAdditionalneeds(), newBooking.getAdditionalneeds());
    }


    @Test
    public void two_getBooking() throws JsonProcessingException {
        one_createBooking();

        Response responseAllBookings = apiClient.getBooking();
        AssertionsForClassTypes.assertThat(responseAllBookings.getStatusCode()).isEqualTo(200);

        String responseAllBookingList = responseAllBookings.getBody().asString();
        System.out.println(responseAllBookingList);
        List<Booking> bookings = objectMapper.readValue(responseAllBookingList, new TypeReference<List<Booking>>() {
        });
        assertThat(bookings).isNotEmpty();

        for (Booking booking : bookings) {
            assertThat(booking.getBookingid()).isGreaterThan(0);
        }
    }


    @Test
    public void three_getBookingById() throws JsonProcessingException {
        one_createBooking();

        int createdBookingId = createdBooking.getBookingid();
        System.out.println(createdBookingId);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("Созданный айдишник - " + createdBookingId);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    }


    @Test
    public void four_updateBookingByPut() throws JsonProcessingException {
        one_createBooking();

        int id = createdBooking.getBookingid();
        String newResponse = "{\"firstname\":\"JohnPUT\",\"lastname\":\"Doe\",\"totalprice\":273,\"depositpaid\":true,\"bookingdates\":{\"checkin\":\"2024-01-01\",\"checkout\":\"2024-01-05\"},\"additionalneeds\":\"Breakfast\"}";

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(id);
        System.out.println(objectMapper.writeValueAsString(newBooking));
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        newBooking.setFirstname(newBooking.getFirstname() + "PUT");
        newBooking.setTotalprice(newBooking.getTotalprice() + 123);

        String requestBodyUpdated = objectMapper.writeValueAsString(newBooking);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("New Request Body!!!");
        System.out.println(requestBodyUpdated);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        apiClient.createToken("admin", "password123");
        apiClient.putBookingById(id, requestBodyUpdated);

        System.out.println(apiClient.getBookingById(id).getBody().asString());

        assertThat(createdBooking).isNotNull();
        //System.out.println(apiClient.getBookingById(id).getBody().asString());
        assertEquals(newResponse, apiClient.getBookingById(id).getBody().asString());

        //System.out.println(apiClient.getBooking().asString());
    }


    @Test
    public void five_updateBookingByPatch() throws JsonProcessingException {
        one_createBooking();

        int bookingId = createdBooking.getBookingid();

        // Отправляем PATCH только с новым firstname
        apiClient.createToken("admin", "password123");
        String patchBody = "{\"firstname\": \"JamesPATCH\"}";
        String newResponseBody = "{\"firstname\":\"JamesPATCH\",\"lastname\":\"Doe\",\"totalprice\":150,\"depositpaid\":true,\"bookingdates\":{\"checkin\":\"2024-01-01\",\"checkout\":\"2024-01-05\"},\"additionalneeds\":\"Breakfast\"}";
        Response patchResponse = apiClient.patchBookingById(bookingId, patchBody);

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("New PATCH Body!!!");
        System.out.println(bookingId);
        System.out.println(patchResponse.getBody().asString());
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        assertEquals(newResponseBody, apiClient.getBookingById(bookingId).getBody().asString());

    }


    @AfterEach
    public void tearDown() {
        apiClient.createToken("admin", "password123");
        apiClient.deleteBooking(createdBooking.getBookingid());
        assertThat(apiClient.getBookingById(createdBooking.getBookingid()).getStatusCode()).isEqualTo(404);
//        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~");
//        System.out.println(apiClient.getBooking().getBody().asString());
    }
}