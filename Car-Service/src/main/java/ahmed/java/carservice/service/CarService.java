package ahmed.java.carservice.service;

import ahmed.java.carservice.entity.Car;
import ahmed.java.carservice.entity.Client;
import ahmed.java.carservice.model.CarResponse;
import ahmed.java.carservice.repository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarService {
    @Autowired
    private CarRepository carRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final String CLIENT_SERVICE_URL = "http://CLIENTS/api/client/";

    public List<CarResponse> findAll() {
        List<Car> cars = carRepository.findAll();
        return cars.stream()
                .map(car -> {
                    Client client = null;
                    if (car.getClientId() != null) {
                        try {
                            client = restTemplate.getForObject(
                                    CLIENT_SERVICE_URL + car.getClientId(),
                                    Client.class
                            );
                        } catch (Exception e) {
                            System.err.println("Error fetching client for car ID " + car.getId() + ": " + e.getMessage());
                        }
                    }
                    return CarResponse.builder()
                            .id(car.getId())
                            .brand(car.getBrand())
                            .model(car.getModel())
                            .matricule(car.getMatricule())
                            .client(client)
                            .build();
                })
                .collect(Collectors.toList());
    }
    private Client fetchClient(Long clientId) {
        if (clientId == null) return null;
        try {
            return restTemplate.getForObject(CLIENT_SERVICE_URL + clientId, Client.class);
        } catch (Exception e) {
            // Log error and return null instead of breaking the process
            System.err.println("Error fetching client for ID " + clientId + ": " + e.getMessage());
            return null;
        }
    }


    public Car save(Car car) {
        return carRepository.save(car);
    }

    public void deleteById(Long id) {
        carRepository.deleteById(id);
    }

    public CarResponse findById(Long id) {
        Car car = carRepository.findById(id).orElseThrow(() -> new RuntimeException("Car not found"));
        Client client = fetchClient(car.getClientId());

        return CarResponse.builder()
                .id(car.getId())
                .brand(car.getBrand())
                .model(car.getModel())
                .matricule(car.getMatricule())
                .client(client)
                .build();
    }

    public CarResponse getCarWithClient(Long id) {
        Car car = carRepository.findById(id).orElse(null);
        Client client = restTemplate.getForObject(CLIENT_SERVICE_URL + car.getClientId(), Client.class);

        return CarResponse.builder()
                .id(car.getId())
                .brand(car.getBrand())
                .model(car.getModel())
                .matricule(car.getMatricule())
                .client(client)
                .build();
    }
}