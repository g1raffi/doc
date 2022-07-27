package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.helpers.BlockingIterable;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class DataResourceTest {

    @Inject
    DataRepository dataRepository;

    @Test
    void shouldDeliverData() {
        Multi<Data> allData = dataRepository.findAllData();
        List<Data> collect = allData.subscribe().asIterable().stream().collect(Collectors.toList());
        assertEquals(collect.size(), 1);
    }

}