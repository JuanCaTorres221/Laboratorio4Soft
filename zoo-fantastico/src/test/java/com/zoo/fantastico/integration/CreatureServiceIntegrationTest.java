package com.zoo.fantastico.integration;

import com.zoo.fantastico.model.Creature;
import com.zoo.fantastico.repository.CreatureRepository;
import com.zoo.fantastico.service.CreatureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class CreatureServiceIntegrationTest {

    @Autowired
    private CreatureService creatureService;

    @Autowired
    private CreatureRepository creatureRepository;

    @Test
    void testCreateCreature_ShouldPersistInDatabase() {
        Creature creature = new Creature();
        creature.setName("Unicornio");
        creature.setSpecies("Místico");
        creature.setDangerLevel(3);
        creature.setHealthStatus("stable");

        creatureService.createCreature(creature);

        Optional<Creature> found = creatureRepository.findById(creature.getId());

        assertTrue(found.isPresent());
        assertEquals("Unicornio", found.get().getName());
    }

    @Test
    void testUpdateCreature_ShouldChangeValuesInDatabase() {
        Creature creature = new Creature();
        creature.setName("Fénix");
        creature.setSpecies("Ave");
        creature.setDangerLevel(4);
        creature.setHealthStatus("stable");

        creatureService.createCreature(creature);

        creature.setDangerLevel(2);
        creature.setHealthStatus("recovering");
        creatureService.updateCreature(creature.getId(), creature);

        Optional<Creature> updated = creatureRepository.findById(creature.getId());
        assertTrue(updated.isPresent());
        assertEquals(2, updated.get().getDangerLevel());
        assertEquals("recovering", updated.get().getHealthStatus());
    }

    @Test
    void testDeleteCreature_ShouldRemoveFromDatabase() {
        Creature creature = new Creature();
        creature.setName("Hydra");
        creature.setSpecies("Reptil");
        creature.setDangerLevel(5);
        creature.setHealthStatus("stable");

        creatureService.createCreature(creature);
        Long id = creature.getId();

        creatureService.deleteCreature(id);

        Optional<Creature> deleted = creatureRepository.findById(id);
        assertFalse(deleted.isPresent());
    }
}
