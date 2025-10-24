package com.zoo.fantastico.integration;

import com.zoo.fantastico.model.Zone;
import com.zoo.fantastico.repository.ZoneRepository;
import com.zoo.fantastico.service.ZoneService;
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
class ZoneServiceIntegrationTest {

    @Autowired
    private ZoneService zoneService;

    @Autowired
    private ZoneRepository zoneRepository;

    @Test
    void testCreateZone_ShouldPersistInDatabase() {
        Zone zone = new Zone();
        zone.setName("Zona de Criaturas Acuáticas");
        zone.setDescription("Zona exclusiva para criaturas que viven en el agua.");
        zone.setCapacity(12);

        zoneService.createZone(zone);

        Optional<Zone> found = zoneRepository.findById(zone.getId());

        assertTrue(found.isPresent(), "La zona debe haberse guardado en la BD");
        assertEquals("Zona de Criaturas Acuáticas", found.get().getName());
        assertEquals(12, found.get().getCapacity());
    }

    @Test
    void testUpdateZone_ShouldModifyDataInDatabase() {
        Zone zone = new Zone();
        zone.setName("Zona de Fuego");
        zone.setDescription("Zona original.");
        zone.setCapacity(6);

        zoneService.createZone(zone);

        zone.setName("Zona de Fuego Renovada");
        zone.setCapacity(10);

        zoneService.updateZone(zone.getId(), zone);

        Optional<Zone> updated = zoneRepository.findById(zone.getId());

        assertTrue(updated.isPresent());
        assertEquals("Zona de Fuego Renovada", updated.get().getName());
        assertEquals(10, updated.get().getCapacity());
    }

    @Test
    void testDeleteZone_ShouldRemoveFromDatabase() {
        Zone zone = new Zone();
        zone.setName("Zona Temporal");
        zone.setDescription("Zona que se eliminará");
        zone.setCapacity(3);

        zoneService.createZone(zone);
        Long id = zone.getId();

        zoneService.deleteZone(id);

        Optional<Zone> deleted = zoneRepository.findById(id);
        assertFalse(deleted.isPresent(), "La zona debe haberse eliminado de la BD");
    }
}
