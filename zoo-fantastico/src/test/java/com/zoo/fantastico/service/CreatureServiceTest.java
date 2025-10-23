package com.zoo.fantastico.service;

import com.zoo.fantastico.exception.ResourceNotFoundException;
import com.zoo.fantastico.model.Creature;
import com.zoo.fantastico.repository.CreatureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatureServiceTest {

    @Mock
    private CreatureRepository creatureRepository;

    @InjectMocks
    private CreatureService creatureService;

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testCreateCreature_ShouldReturnSavedCreature() {
        Creature creature = new Creature();
        creature.setName("Fénix");
        creature.setSpecies("Ave");
        creature.setDangerLevel(3);
        creature.setHealthStatus("stable");

        when(creatureRepository.save(any(Creature.class))).thenAnswer(invocation -> {
            Creature saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Creature result = creatureService.createCreature(creature);

        assertNotNull(result);
        assertEquals("Fénix", result.getName());
        verify(creatureRepository, times(1)).save(creature);
    }

    @Test
    void testGetById_ShouldReturnCreature_WhenExists() {
        Creature creature = new Creature();
        creature.setId(2L);
        creature.setName("Unicornio");

        when(creatureRepository.findById(2L)).thenReturn(Optional.of(creature));

        Creature found = creatureService.getById(2L);

        assertNotNull(found);
        assertEquals("Unicornio", found.getName());
        verify(creatureRepository).findById(2L);
    }

    @Test
    void testGetById_ShouldThrowException_WhenNotFound() {
        when(creatureRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> creatureService.getById(99L));
    }

    @Test
    void testUpdateCreature_ShouldModifyFields() {
        Creature existing = new Creature();
        existing.setId(3L);
        existing.setName("Dragón");
        existing.setSpecies("Reptil");
        existing.setDangerLevel(5);
        existing.setHealthStatus("stable");

        Creature updated = new Creature();
        updated.setName("Dragón Rojo");
        updated.setSpecies("Reptil Volador");
        updated.setDangerLevel(4);
        updated.setHealthStatus("recovering");

        when(creatureRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(creatureRepository.save(any(Creature.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Creature result = creatureService.updateCreature(3L, updated);

        assertEquals("Dragón Rojo", result.getName());
        assertEquals(4, result.getDangerLevel());
        verify(creatureRepository).findById(3L);
        verify(creatureRepository).save(any(Creature.class));
    }

    @Test
    void testDeleteCreature_ShouldRemove_WhenStable() {
        Creature creature = new Creature();
        creature.setId(4L);
        creature.setHealthStatus("stable");

        when(creatureRepository.findById(4L)).thenReturn(Optional.of(creature));

        creatureService.deleteCreature(4L);

        verify(creatureRepository).delete(creature);
    }

    @Test
    void testDeleteCreature_ShouldThrow_WhenCritical() {
        Creature creature = new Creature();
        creature.setId(5L);
        creature.setHealthStatus("critical");

        when(creatureRepository.findById(5L)).thenReturn(Optional.of(creature));

        assertThrows(IllegalStateException.class, () -> creatureService.deleteCreature(5L));
        verify(creatureRepository, never()).delete(any());
    }

    @Test
    void testModelValidation_ShouldDetectInvalidData() {
        Creature invalid = new Creature();
        invalid.setName("");  // viola @NotBlank
        invalid.setSpecies("");  // viola @NotBlank
        invalid.setDangerLevel(15); // viola @Max(10)
        invalid.setHealthStatus("");

        Set<ConstraintViolation<Creature>> violations = validator.validate(invalid);

        assertFalse(violations.isEmpty());
        boolean hasNameViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        assertTrue(hasNameViolation);
    }
}
