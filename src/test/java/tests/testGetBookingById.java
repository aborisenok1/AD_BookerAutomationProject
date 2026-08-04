package tests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;

import core.models.Booking;
import core.models.BookingDetails;
import io.restassured.response.Response;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class testGetBookingById {

    private APIClient apiClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testGetBookingById() throws Exception {
        // Сначала получаем список всех бронирований, чтобы взять существующий ID
        Response allBookingsResponse = apiClient.getBooking();
        assertThat(allBookingsResponse.getStatusCode()).isEqualTo(200);

        String allBookingsBody = allBookingsResponse.getBody().asString();
        List<Booking> allBookings = objectMapper.readValue(allBookingsBody, new TypeReference<List<Booking>>() {
        });
        assertThat(allBookings).isNotEmpty();

        // Берем ID первого бронирования для теста
        int bookingId = allBookings.get(0).getBookingid();

        // Выполняем запрос на получение конкретного бронирования по ID
        Response response = apiClient.getBookingById(bookingId);

        // Проверяем статус код
        AssertionsForClassTypes.assertThat(response.getStatusCode()).isEqualTo(200);

        // Получаем тело ответа
        String responseBody = response.getBody().asString();
        System.out.println();
        System.out.println("- Response Body - ");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(responseBody);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        // Десериализуем ответ в объект Booking (без поля bookingid, так как его нет в ответе)
        BookingDetails bookingDetails = objectMapper.readValue(responseBody, new TypeReference<BookingDetails>() {
        });

        // Проверяем, что все обязательные поля присутствуют и имеют корректные значения
        assertThat(bookingDetails.getFirstname()).isNotBlank();
        assertThat(bookingDetails.getLastname()).isNotBlank();
        assertThat(bookingDetails.getTotalprice()).isGreaterThan(0);
        assertThat(bookingDetails.getDepositpaid()).isNotNull();
        assertThat(bookingDetails.getBookingdates()).isNotNull();
        assertThat(bookingDetails.getBookingdates().getCheckin()).isNotBlank();
        assertThat(bookingDetails.getBookingdates().getCheckout()).isNotBlank();
    }
}
