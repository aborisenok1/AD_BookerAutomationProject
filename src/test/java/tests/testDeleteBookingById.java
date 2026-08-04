package tests;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.Booking;
import io.restassured.response.Response;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class testDeleteBookingById {

    private APIClient apiClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testDeleteBookingById() throws Exception {

        Response allBookingsResponse = apiClient.getBooking();
        assertThat(allBookingsResponse.getStatusCode()).isEqualTo(200);

        String allBookingsBody = allBookingsResponse.getBody().asString();
        List<Booking> allBookings = objectMapper.readValue(allBookingsBody, new TypeReference<List<Booking>>() {
        });
        assertThat(allBookings).isNotEmpty();

        int deletedBookingId = allBookings.get(0).getBookingid();

        apiClient.createToken("admin", "password123");
        apiClient.deleteBooking(deletedBookingId);
        assertThat(apiClient.getBookingById(deletedBookingId).getStatusCode()).isEqualTo(404);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("Удалённый айдишник - " + deletedBookingId);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(apiClient.getBooking().getBody().asString());
    }


    @Test
    public void testDeleteBooking() throws Exception {
        Response response = apiClient.getBooking();
        assertThat(response.getStatusCode()).isEqualTo(200);

        //Десериализум тело ответа в список объектов Booking
        String responseBody = response.getBody().asString();
        List<Booking> bookings = objectMapper.readValue(responseBody, new TypeReference<List<Booking>>() {
        });

        //Проверяем, что тело ответа содержит обьекты Booking
        assertThat(bookings).isNotEmpty();

        int bookingId = bookings.get(0).getBookingid();


        apiClient.createToken("admin", "password123");
        // Удаление выбранного бронирования
        Response deleteResponse = apiClient.deleteBooking(bookingId);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(201);
    }

}