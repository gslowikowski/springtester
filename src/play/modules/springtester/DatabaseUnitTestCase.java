package play.modules.springtester;

import java.lang.reflect.Method;

import org.junit.Before;
import play.db.jpa.Model;
import play.test.Fixtures;
import play.test.UnitTest;

public abstract class DatabaseUnitTestCase extends UnitTest {

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
    }

    protected void checkCount(Class<? extends Model> cls, long expected) {
        try {
            Method method = cls.getMethod("count");
            long actual = (Long) method.invoke(null);
            assertEquals(expected, actual);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
