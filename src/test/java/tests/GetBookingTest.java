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

import static org.assertj.core.api.Assertions.assertThat;

public class GetBookingTest {

    private APIClient apiClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testGetBooking() throws Exception {
        Response response = apiClient.getBooking();

        AssertionsForClassTypes.assertThat(response.getStatusCode()).isEqualTo(200);

        String responseBody = response.getBody().asString();
        //System.out.println(responseBody);
        List<Booking> bookings = objectMapper.readValue(responseBody, new TypeReference<List<Booking>>() {});

        assertThat(bookings).isNotEmpty();


        for (Booking booking : bookings) {
            assertThat(booking.getBookingid()).isGreaterThan(0);
        }
    }
}
