package play.modules.springtester;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import play.modules.spring.Spring;
import play.modules.spring.SpringPlugin;
import play.mvc.After;
import play.mvc.Before;

/**
 * Used to test classes that require resources to be injected via spring plus the ability to inject mocked objects. Declare the test's subject by annotating a field with @Subject and this class will
 * resolve the subject and inject any mocks/real dependencies.  To declare a mock object to be injected, annotate a field with @Mock.
 * <p/>
 * Example injection of subject field: @Subject private MySubject subject;
 * <p/>
 * Example injection of mock field: @Mock(name = "optionalName") private Dependency mockDependency;
 * <p/>
 * The above will inject the mockDependency object into the optionalName field in the MySubject object.
 */
public abstract class SpringMockitoUnitTestCase extends DatabaseUnitTestCase {
    private final Map<String, BeanDefinition> definitions = new HashMap<String, BeanDefinition>();

    @Before
    public void injectMocksIntoSubject() throws IllegalAccessException {
        injectMocks();
        registerMocksInSpringContext();
        injectSubject();
    }

    @After
    public void revertMocksFromApplicationContext() {
        Set<String> beanNames = definitions.keySet();
        for (String name : beanNames) {
            BeanDefinition definition = definitions.get(name);
            SpringPlugin.applicationContext.removeBeanDefinition(name);
            SpringPlugin.applicationContext.registerBeanDefinition(name, definition);
        }
        definitions.clear();
    }

    private void injectSubject() throws IllegalAccessException {
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Subject.class)) injectSubject(field);
        }
    }

    private void injectSubject(Field field) throws IllegalAccessException {
        Class<?> cls = field.getType();
        field.setAccessible(true);
        Object value = Spring.getBeanOfType(cls);
        field.set(this, value);
    }

    private void injectMocks() {
        MockitoAnnotations.initMocks(this);
    }

    private void registerMocksInSpringContext() throws IllegalAccessException {
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Mock.class)) registerMockInSpringContext(field);
        }
    }

    private void registerMockInSpringContext(Field field) throws IllegalAccessException {
        Object object = getValue(field);
        String name = getMockName(field);
        if ("".equals(name)) registerBeanDefinitionWithFieldName(field, object);
        else registerBeanDefinition(name, object);
    }

    private String getMockName(Field field) {
        Mock mock = field.getAnnotation(Mock.class);
        return mock.name();
    }

    private Object getValue(Field field) throws IllegalAccessException {
        field.setAccessible(true);
        return field.get(this);
    }

    private void registerBeanDefinitionWithFieldName(Field field, Object object) {
        String fieldName = field.getName();
        registerBeanDefinition(fieldName, object);
    }

    private void registerBeanDefinition(String name, Object object) {
        GenericApplicationContext context = SpringPlugin.applicationContext;
        if (context.containsBeanDefinition(name)) {
            BeanDefinition beanDefinition = context.getBeanDefinition(name);
            definitions.put(name, beanDefinition);
            context.removeBeanDefinition(name);
        }
        GenericBeanDefinition definition = createMockitoFactoryBeanDefinition(object);
        context.registerBeanDefinition(name, definition);
    }

    private GenericBeanDefinition createMockitoFactoryBeanDefinition(Object object) {
        GenericBeanDefinition definition = new GenericBeanDefinition();
        definition.setBeanClass(MockitoFactory.class);
        definition.setFactoryMethodName("create");
        ConstructorArgumentValues values = new ConstructorArgumentValues();
        // Make sure all beans are created with no state to be verified.
        Mockito.reset(object);
        values.addGenericArgumentValue(object);
        definition.setConstructorArgumentValues(values);
        return definition;
    }
}
