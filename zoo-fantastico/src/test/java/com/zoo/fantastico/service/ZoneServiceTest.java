package com.zoo.fantastico.service;

import com.zoo.fantastico.model.Zone;
import com.zoo.fantastico.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZoneServiceTest {

    @Mock
    private ZoneRepository zoneRepository;

    @InjectMocks
    private ZoneService zoneService;

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testCreateZone_ShouldReturnSavedZone() {
        Zone zone = new Zone();
        zone.setName("Zona de Dragones");
        zone.setCapacity(10);

        when(zoneRepository.save(any(Zone.class))).thenAnswer(invocation -> {
            Zone z = invocation.getArgument(0);
            z.setId(1L);
            return z;
        });

        Zone saved = zoneService.createZone(zone);

        assertNotNull(saved);
        assertEquals("Zona de Dragones", saved.getName());
        assertEquals(10, saved.getCapacity());
        verify(zoneRepository, times(1)).save(zone);
    }

    @Test
    void testGetZoneById_ShouldReturnZone() {
        Zone zone = new Zone();
        zone.setId(2L);
        zone.setName("Zona de Fénix");

        when(zoneRepository.findById(2L)).thenReturn(Optional.of(zone));

        Zone found = zoneService.getById(2L);

        assertNotNull(found);
        assertEquals("Zona de Fénix", found.getName());
        verify(zoneRepository, times(1)).findById(2L);
    }

    @Test
    void testUpdateZone_ShouldUpdateFields() {
        Zone existing = new Zone();
        existing.setId(3L);
        existing.setName("Vieja Zona");
        existing.setCapacity(5);

        when(zoneRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(zoneRepository.save(any(Zone.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Zone update = new Zone();
        update.setName("Nueva Zona");
        update.setCapacity(8);

        Zone result = zoneService.updateZone(3L, update);

        assertNotNull(result);
        assertEquals("Nueva Zona", result.getName());
        assertEquals(8, result.getCapacity());
        verify(zoneRepository).findById(3L);
        verify(zoneRepository).save(any(Zone.class));
    }

    @Test
    void testDeleteZone_ShouldDeleteSuccessfully() {
        // Arrange
        Zone zone = new Zone();
        zone.setId(4L);
        zone.setName("Zona a borrar");
        zone.setCreatures(new ArrayList<>());

        when(zoneRepository.findById(4L)).thenReturn(Optional.of(zone));
        doNothing().when(zoneRepository).delete(any(Zone.class));

        // Act & Assert
        assertDoesNotThrow(() -> zoneService.deleteZone(4L));

        // Verificamos que haya llamado a delete(Zone)
        verify(zoneRepository).delete(any(Zone.class));
    }


    @Test
    void testZoneValidation_ShouldDetectInvalidData() {
        Zone invalidZone = new Zone();
        invalidZone.setName(""); // violara @NotBlank
        invalidZone.setCapacity(-3); // violara @Min(0)

        Set<ConstraintViolation<Zone>> violations = validator.validate(invalidZone);

        assertFalse(violations.isEmpty(), "Debe haber violaciones de validacion");

        boolean hasNameViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        boolean hasCapacityViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("capacity"));

        assertTrue(hasNameViolation);
        assertTrue(hasCapacityViolation);
    }
}
