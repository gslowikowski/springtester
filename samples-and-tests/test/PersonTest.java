import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import play.modules.springtester.SpringMockitoUnitTestCase;
import play.modules.springtester.Subject;
import springtester.AgeCalculator;
import springtester.Person;

public final class PersonTest extends SpringMockitoUnitTestCase {
    @Subject private Person subject;
    @Mock(name = "ageCalculator") AgeCalculator mockAgeCalculator;

    @Test
    public void testCanVote() throws Exception {
        Mockito.when(mockAgeCalculator.calculate("1-January-1980")).thenReturn(20);
        boolean actual = subject.canVote();
        assertEquals(true, actual);
    }
}
