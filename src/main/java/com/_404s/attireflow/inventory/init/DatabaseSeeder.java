package com._404s.attireflow.inventory.init;

import com._404s.attireflow.inventory.repo.DeliveryRepository;
import com._404s.attireflow.inventory.repo.VariantRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DatabaseSeeder implements ApplicationRunner {

    private final VariantRepository variantRepository;
    private final DeliveryRepository deliveryRepository;
    private final DataSource dataSource;

    public DatabaseSeeder(
            VariantRepository variantRepository,
            DeliveryRepository deliveryRepository,
            DataSource dataSource
    ) {
        this.variantRepository = variantRepository;
        this.deliveryRepository = deliveryRepository;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean hasVariants = variantRepository.count() > 0;
        boolean hasDeliveries = deliveryRepository.count() > 0;

        if (hasVariants && hasDeliveries) {
            return; // DB already has seeded data
        }

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("seed-data.sql"));
        populator.execute(dataSource);
    }
}
