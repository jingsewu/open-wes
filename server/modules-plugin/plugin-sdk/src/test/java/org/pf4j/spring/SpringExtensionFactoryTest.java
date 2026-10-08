package org.pf4j.spring;

import org.junit.jupiter.api.Test;
import org.pf4j.PluginManager;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class SpringExtensionFactoryTest {

    public static class SimpleExtension {
    }

    @Test
    void create_withAutowireDisabled_returnsSameInstanceForSameClass() {
        SpringExtensionFactory factory = new SpringExtensionFactory(mock(PluginManager.class), false);

        SimpleExtension first = factory.create(SimpleExtension.class);
        SimpleExtension second = factory.create(SimpleExtension.class);

        assertSame(first, second);
    }

    @Test
    void evict_forcesNewInstance() {
        SpringExtensionFactory factory = new SpringExtensionFactory(mock(PluginManager.class), false);
        SimpleExtension first = factory.create(SimpleExtension.class);

        factory.evict(SimpleExtension.class);

        assertNotSame(first, factory.create(SimpleExtension.class));
    }

    @Test
    void create_whenWhichPluginReturnsNull_doesNotThrowNpe() {
        PluginManager pluginManager = mock(PluginManager.class);
        SpringExtensionFactory factory = new SpringExtensionFactory(pluginManager, true);

        SimpleExtension extension = factory.create(SimpleExtension.class);

        assertNotNull(extension);
    }
}
