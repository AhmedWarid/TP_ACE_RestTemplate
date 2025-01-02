package ahmed.java.carservice.service;

import ahmed.java.carservice.controller.CarController;
import ahmed.java.carservice.repository.ClientServiceFeignClient;
import ahmed.java.carservice.entity.Car;
import ahmed.java.carservice.entity.Client;
import ahmed.java.carservice.model.CarResponse;
import ahmed.java.carservice.repository.CarRepository;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;


import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarService {
    @Autowired
    private CarRepository carRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebClient webClient;

    @Autowired
    private ClientServiceFeignClient feignClient;


    private final String CLIENT_SERVICE_URL = "http://localhost:8888/api/client/";

    // RestTemplate implementation
    public List<CarResponse> findAllWithRestTemplate() {
        List<Car> cars = carRepository.findAll();
        return cars.stream()
                .map(car -> {
                    Client client = fetchClientWithRestTemplate(car.getClientId());
                    return buildCarResponse(car, client);
                })
                .collect(Collectors.toList());
    }

    // WebClient implementation
    public List<CarResponse> findAllWithWebClient() {
        List<Car> cars = carRepository.findAll();
        return cars.stream()
                .map(car -> {
                    Client client = fetchClientWithWebClient(car.getClientId());
                    return buildCarResponse(car, client);
                })
                .collect(Collectors.toList());
    }

    // FeignClient implementation
    public List<CarResponse> findAllWithFeignClient() {
        List<Car> cars = carRepository.findAll();
        return cars.stream()
                .map(car -> {
                    Client client = fetchClientWithFeignClient(car.getClientId());
                    return buildCarResponse(car, client);
                })
                .collect(Collectors.toList());
    }

    private Client fetchClientWithRestTemplate(Long clientId) {
        if (clientId == null) return null;
        try {
            ResponseEntity<Client> response = restTemplate.getForEntity(
                    CLIENT_SERVICE_URL + clientId,
                    Client.class
            );
            return response.getBody();
        } catch (Exception e) {
            System.out.println("Error fetching client with RestTemplate for ID {}: {}"+ clientId + e.getMessage());
            return null;
        }
    }

    private Client fetchClientWithWebClient(Long clientId) {
        if (clientId == null) return null;
        try {
            return webClient.get()
                    .uri("/api/client/{id}", clientId)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), response ->
                            Mono.error(new RuntimeException("Client not found")))
                    .onStatus(status -> status.is5xxServerError(), response ->
                            Mono.error(new RuntimeException("Server error")))
                    .bodyToMono(Client.class)
                    .timeout(Duration.ofSeconds(200))
                    .block();
        } catch (Exception e) {
            System.out.println("Error fetching client with WebClient for ID {}: {}"+ clientId+ e.getMessage());
            return null;
        }
    }

    private Client fetchClientWithFeignClient(Long clientId) {
        if (clientId == null) return null;
        try {
            return feignClient.getClientById(clientId);
        } catch (FeignException.NotFound e) {
            System.out.println("Client not found with ID: {}"+clientId);
            return null;
        } catch (Exception e) {
            System.out.println("Error fetching client with FeignClient for ID {}: {}"+ clientId + e.getMessage());
            return null;
        }
    }

    private CarResponse buildCarResponse(Car car, Client client) {
        return CarResponse.builder()
                .id(car.getId())
                .brand(car.getBrand())
                .model(car.getModel())
                .matricule(car.getMatricule())
                .client(client)
                .build();
    }

    public Car save(Car car) {
        return carRepository.save(car);
    }

    public void deleteById(Long id) {
        carRepository.deleteById(id);
    }

    // RestTemplate version
    public CarResponse findByIdWithRestTemplate(Long id) {
        Car car = carRepository.findById(id).orElseThrow(() -> new RuntimeException("Car not found"));
        Client client = fetchClientWithRestTemplate(car.getClientId());
        return buildCarResponse(car, client);
    }

    // WebClient version
    public CarResponse findByIdWithWebClient(Long id) {
        Car car = carRepository.findById(id).orElseThrow(() -> new RuntimeException("Car not found"));
        Client client = fetchClientWithWebClient(car.getClientId());
        return buildCarResponse(car, client);
    }

    // FeignClient version
    public CarResponse findByIdWithFeignClient(Long id) {
        Car car = carRepository.findById(id).orElseThrow(() -> new RuntimeException("Car not found"));
        Client client = fetchClientWithFeignClient(car.getClientId());
        return buildCarResponse(car, client);
    }
}