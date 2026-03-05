package com.algaworks.algasensors.device.management.domain.services;

import com.algaworks.algasensors.device.management.api.model.SensorInput;
import com.algaworks.algasensors.device.management.api.model.SensorOutput;
import com.algaworks.algasensors.device.management.common.IdGeneration;
import com.algaworks.algasensors.device.management.domain.model.Sensor;
import com.algaworks.algasensors.device.management.domain.model.SensorId;
import com.algaworks.algasensors.device.management.domain.repository.SensorRepository;
import io.hypersistence.tsid.TSID;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

@Service
@Validated
@RequiredArgsConstructor
public class SensorService {

    private final SensorRepository sensorRepository;

    @Transactional(readOnly = true)
    public Page<SensorOutput> search(Pageable pageable) {
        return sensorRepository.findAll(pageable).map(this::convertToModel);
    }

    @Transactional(readOnly = true)
    public SensorOutput getOne(TSID sensorId) {
        Sensor sensor = sensorRepository.findById(new SensorId(sensorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return convertToModel(sensor);
    }

    @Transactional
    public SensorOutput create(SensorInput input) {
        Sensor sensor = Sensor.builder()
                .id(new SensorId(IdGeneration.generateTSID()))
                .name(input.getName())
                .ip(input.getIp())
                .location(input.getLocation())
                .protocol(input.getProtocol())
                .model(input.getModel())
                .enabled(false)
                .build();

        sensorRepository.saveAndFlush(sensor);

        return convertToModel(sensor);
    }

    @Transactional
    public SensorOutput update(TSID sensorId, SensorInput input) {
        try {
            Sensor sensor = sensorRepository.getReferenceById(new SensorId(sensorId));

            sensor.setName(input.getName());
            sensor.setIp(input.getIp());
            sensor.setLocation(input.getLocation());
            sensor.setProtocol(input.getProtocol());
            sensor.setModel(input.getModel());

            sensorRepository.saveAndFlush(sensor);

            return convertToModel(sensor);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(TSID sensorId) {

        if (!sensorRepository.existsById(new SensorId(sensorId))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else
            sensorRepository.deleteById(new SensorId(sensorId));
    }

    private SensorOutput convertToModel(Sensor sensor) {
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