package au.id.djc.rdftemplate.datatype;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.datatypes.RDFDatatype;

public class DateTimeDataTypeUnitTest {
    
    private RDFDatatype type;
    
    @Before
    public void setUp() {
        type = new DateTimeDataType();
    }
    
    @Test
    public void shouldParseDate() {
        assertThat((DateTime) type.parse("2003-05-25T10:11:12+05:00"),
                equalTo(new DateTime(2003, 5, 25, 10, 11, 12, 0, DateTimeZone.forOffsetHours(5))));
    }
    
    @Test
    public void shouldUnparseDate() {
        assertThat(type.unparse(new DateTime(2003, 5, 25, 10, 11, 12, 0, DateTimeZone.forOffsetHours(5))),
                equalTo("2003-05-25T10:11:12+05:00"));
    }

}
