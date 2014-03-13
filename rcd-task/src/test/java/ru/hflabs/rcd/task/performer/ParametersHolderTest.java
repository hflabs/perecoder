package ru.hflabs.rcd.task.performer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.hflabs.rcd.model.task.TaskResultStatus;

import java.util.Date;
import java.util.Iterator;

import static org.testng.Assert.assertEquals;

public class ParametersHolderTest {

    @DataProvider
    private Iterator<Object[]> createTestCases() {
        return Lists.<Object[]>newArrayList(
                new Object[]{"stringValue", "1", String.class, "1"},
                new Object[]{"longValue", 1, Long.class, 1L},
                new Object[]{"integerValue", 1, Integer.class, 1},
                new Object[]{"booleanValue", true, Boolean.class, Boolean.TRUE},
                new Object[]{"dateValue", 946674000000L, Date.class, new Date(946674000000L)},
                new Object[]{"enumValue", TaskResultStatus.UNKNOWN.name(), TaskResultStatus.class, TaskResultStatus.UNKNOWN}
        ).iterator();
    }

    @Test(dataProvider = "createTestCases")
    public <T> void testRetrieveParameter(String key, Object value, Class<T> expectedClass, T expectedValue) {
        ParametersHolder holder = new ParametersHolder(ImmutableMap.<String, Object>builder()
                .put(key, value)
                .build());
        assertEquals(holder.retrieveParameter(key, expectedClass, null), expectedValue);
    }
}
