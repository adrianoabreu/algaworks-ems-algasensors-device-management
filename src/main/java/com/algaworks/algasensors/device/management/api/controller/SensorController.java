package com.algaworks.algasensors.device.management.api.controller;

import com.algaworks.algasensors.device.management.api.client.SensorMonitoringClient;
import com.algaworks.algasensors.device.management.api.model.SensorInput;
import com.algaworks.algasensors.device.management.api.model.SensorOutput;
import com.algaworks.algasensors.device.management.common.IdGeneration;
import com.algaworks.algasensors.device.management.domain.model.Sensor;
import com.algaworks.algasensors.device.management.domain.model.SensorId;
import com.algaworks.algasensors.device.management.domain.repository.SensorRepository;
import com.algaworks.algasensors.device.management.domain.services.SensorService;
import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
//import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final SensorRepository sensorRepository;
    private final SensorService sensorService;
    private final SensorMonitoringClient sensorMonitoringClient;

    @GetMapping
    public Page<SensorOutput> search(@PageableDefault Pageable pageable){
        Page<Sensor> sensors = sensorRepository.findAll(pageable);
        return sensors.map(this::convertToModel);
    }

    @GetMapping("{sensorId}")
    public SensorOutput getOne(@PathVariable TSID sensorId) {
        Sensor sensor = sensorRepository.findById(new SensorId(sensorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return convertToModel(sensor);
    }

    @PutMapping(value = "/{sensorId}")
    public ResponseEntity<SensorOutput> update(@PathVariable TSID sensorId, @Validated @RequestBody SensorInput input) {
        return ResponseEntity.ok(sensorService.update(sensorId,input));
    }

    @DeleteMapping(value = "/{sensorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public ResponseEntity<Void> delete(@PathVariable TSID sensorId) {
    public void delete(@PathVariable TSID sensorId) {
//        sensorService.delete(sensorId);
//        return ResponseEntity.noContent().build();
        Sensor sensor = sensorRepository.findById(new SensorId(sensorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        sensorRepository.delete(sensor);

        sensorMonitoringClient.disableMonitoring(sensorId);
    }

    @PutMapping(value = "/{sensorId}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public ResponseEntity<Void> enable(@PathVariable TSID sensorId) {
    public void enable(@PathVariable TSID sensorId) {
//        sensorService.enable(sensorId);
//        return ResponseEntity.noContent().build();
        Sensor sensor = sensorRepository.findById(new SensorId(sensorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        sensor.setEnabled(true);
        sensorRepository.save(sensor);

        sensorMonitoringClient.enableMonitoring(sensorId);
    }

    @DeleteMapping(value = "/{sensorId}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public ResponseEntity<Void> disable(@PathVariable TSID sensorId) {
    public void disable(@PathVariable TSID sensorId) {
//        sensorService.disable(sensorId);
//        return ResponseEntity.noContent().build();
        Sensor sensor = sensorRepository.findById(new SensorId(sensorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        sensor.setEnabled(true);
        sensorRepository.save(sensor);

        sensorMonitoringClient.disableMonitoring(sensorId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SensorOutput create(@RequestBody SensorInput input){
        Sensor sensor = Sensor.builder()
                .id(new SensorId(IdGeneration.generateTSID()))
                .name(input.getName())
                .ip(input.getIp())
                .location(input.getLocation())
                .protocol(input.getProtocol())
                .model(input.getModel())
                .enabled(false)
                .build();

        sensor = sensorRepository.saveAndFlush(sensor);

        return convertToModel(sensor);
    }

    private SensorOutput convertToModel(Sensor sensor){
        return SensorOutput.builder()
                .id(sensor.getId().getValue())
                .name(sensor.getName())
                .ip(sensor.getIp())
                .location(sensor.getLocation())
                .protocol(sensor.getProtocol())
                .model(sensor.getModel())
                .enabled(sensor.getEnabled())
                .build();
    }

}
