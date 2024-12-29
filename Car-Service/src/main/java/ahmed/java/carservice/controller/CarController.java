package ahmed.java.carservice.controller;

import ahmed.java.carservice.entity.Car;
import ahmed.java.carservice.model.CarResponse;
import ahmed.java.carservice.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/car")
public class CarController {
    @Autowired
    private CarService carService;

    @GetMapping
    public List<CarResponse> getAllCars() {
        return carService.findAll();
    }
    @GetMapping("/{id}")
    public ResponseEntity<CarResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(carService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Car> save(@RequestBody Car car) {
        System.out.println(car.getClientId());
        return ResponseEntity.ok(carService.save(car));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Car> update(@PathVariable Long id, @RequestBody Car car) {
        car.setId(id);
        return ResponseEntity.ok(carService.save(car));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        carService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Add method to get cars by client_id
    @GetMapping("/client/{clientId}")
    public CarResponse findByClientId(@PathVariable Long clientId) {
        return carService.findById(clientId);
    }
}
